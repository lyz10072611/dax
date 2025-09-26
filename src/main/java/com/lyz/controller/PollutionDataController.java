package com.lyz.controller;

import com.lyz.pojo.PageBean;
import com.lyz.pojo.PollutionData;
import com.lyz.pojo.Result;
import com.lyz.service.PollutionDataService;
import com.lyz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import com.lyz.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/pollution")
@Validated
@Tag(name = "污染物数据", description = "除查询外仅管理员可写")
@CrossOrigin(origins = "http://localhost:5173")
public class PollutionDataController {
    @Autowired
    private PollutionDataService service;

    @Autowired
    private UserService userService;

    // 通过数据库查询用户role_id判断是否为管理员
    private boolean isAdminByDb() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims == null) return false;
        Object id = claims.get("id");
        Integer uid = (id instanceof Number) ? ((Number) id).intValue() : null;
        if (uid == null) return false;
        Integer roleId = userService.findRoleIdByUserId(uid);
        return roleId != null && roleId == 1;
    }
    private Integer currentUserId() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims == null) return null;
        Object id = claims.get("id");
        if (id instanceof Integer) return (Integer) id;
        if (id instanceof Number) return ((Number) id).intValue();
        return null;
    }

    @GetMapping
    @Operation(summary = "分页查询", description = "按 dataFormat、pollutantType、生产时间(年/月/日/时)过滤，按produce_time倒序")
    public Result<PageBean<PollutionData>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "10") Integer pageSize,
                                                @RequestParam(required = false) Integer dataFormat,
                                                @RequestParam(required = false) String pollutantType,
                                                @RequestParam(required = false) Integer year,
                                                @RequestParam(required = false) Integer month,
                                                @RequestParam(required = false) Integer day,
                                                @RequestParam(required = false) Integer hour) {
        return Result.success(service.page(pageNum, pageSize, dataFormat, pollutantType, year, month, day, hour));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询")
    public Result<PollutionData> get(@PathVariable Long id) {
        return Result.success(service.findById(id));
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // Redis Lua脚本：原子扣减日配额并累加总下载量
    private static final DefaultRedisScript<Long> QUOTA_DECR_LUA;
    static {
        String script = "local cost=tonumber(ARGV[1]); local ttl=tonumber(ARGV[2]); local init=tonumber(ARGV[3]); "
                + "if redis.call('EXISTS', KEYS[1])==0 then redis.call('SET', KEYS[1], init, 'EX', ttl) end " // 初始化日配额
                + "local remain=tonumber(redis.call('GET', KEYS[1]) or '0'); " // 获取剩余配额
                + "if remain < cost then return -1 end " // 配额不足返回-1
                + "redis.call('DECRBY', KEYS[1], cost); " // 扣减日配额
                + "redis.call('INCRBY', KEYS[2], cost); " // 累加总下载量
                + "if redis.call('TTL', KEYS[1])==-1 then redis.call('EXPIRE', KEYS[1], ttl) end " // 确保TTL存在
                + "return remain - cost"; // 返回扣减后的剩余配额
        QUOTA_DECR_LUA = new DefaultRedisScript<>();
        QUOTA_DECR_LUA.setResultType(Long.class);
        QUOTA_DECR_LUA.setScriptText(script);
    }

    @GetMapping("/download")
    @Operation(summary = "按ID下载单个或多个文件", description = "多个ID将打包为zip；非管理员按天限额500")
    public ResponseEntity<byte[]> download(@RequestParam List<Long> ids) throws IOException {
        boolean admin = isAdminByDb();
        Integer uid = currentUserId();
        if (uid == null) return ResponseEntity.status(401).build();

        String dayKey = "download:daily:" + uid;
        String sumKey = "download:sum:" + uid;

        if (!admin) {
            // 非管理员：使用Lua脚本原子扣减配额
            int cost = Math.max(ids.size(), 1);
            Long res = stringRedisTemplate.execute(QUOTA_DECR_LUA,
                    java.util.Arrays.asList(dayKey, sumKey),
                    String.valueOf(cost), String.valueOf(Duration.ofHours(24).toSeconds()), "500");
            if (res == null || res < 0) {
                return ResponseEntity.status(429).header("X-Reason", "Daily quota exceeded").build();
            }
        }

        List<PollutionData> list = service.findByIds(ids);
        if (list == null || list.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (list.size() == 1) {
            // 单文件：直接返回原文件
            PollutionData pd = list.get(0);
            Path path = Paths.get(pd.getFilePath());
            if (!Files.exists(path)) return ResponseEntity.notFound().build();
            byte[] bytes = Files.readAllBytes(path);
            String filename = path.getFileName().toString();
            if (!admin) userService.incrUserSumDownloadBy(uid, 1);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(bytes);
        }

        // 多文件：打包为ZIP返回
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (PollutionData pd : list) {
                Path path = Paths.get(pd.getFilePath());
                if (!Files.exists(path)) continue;
                zos.putNextEntry(new ZipEntry(path.getFileName().toString()));
                try (InputStream in = Files.newInputStream(path)) {
                    in.transferTo(zos);
                }
                zos.closeEntry();
            }
        }
        byte[] zip = baos.toByteArray();
        if (!admin) userService.incrUserSumDownloadBy(uid, list.size());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pollution-data.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zip);
    }

    @PostMapping
    @Operation(summary = "新增", description = "仅管理员")
    public Result add(@RequestBody @Validated PollutionData data) {
        if (!isAdminByDb()) return Result.error("无权限");
        service.add(data);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "更新", description = "仅管理员")
    public Result update(@RequestBody @Validated PollutionData data) {
        if (!isAdminByDb()) return Result.error("无权限");
        service.update(data);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除", description = "仅管理员")
    public Result delete(@PathVariable Long id) {
        if (!isAdminByDb()) return Result.error("无权限");
        service.delete(id);
        return Result.success();
    }

    @GetMapping("/stream")
    @Operation(summary = "流式传输大数据", description = "从数据库流式传输数据，支持大数据量")
    public ResponseEntity<StreamingResponseBody> streamLargeData() {
        StreamingResponseBody responseBody = outputStream -> {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
                // 从数据库中流式获取数据
                service.streamAllData(result -> {
                    try {
                        writer.write(result.toString()); // 将每条记录写入输出流
                        writer.newLine();
                        writer.flush();
                    } catch (IOException e) {
                        throw new RuntimeException("Error writing data to output stream", e);
                    }
                });
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=large_data.txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(responseBody);
    }
}


