package com.lyz.controller;

import com.lyz.pojo.PollutionData;
import com.lyz.pojo.Result;
import com.lyz.service.PollutionDataService;
import com.lyz.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/pollution/async")
@Validated
@Tag(name = "污染物数据-异步下载", description = "通过Redis队列进行削峰填谷")
public class PollutionDataAsyncController {
    @Autowired
    private StringRedisTemplate redis;
    @Autowired
    private PollutionDataService service;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private Integer currentUserId() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims == null) return null;
        Object id = claims.get("id");
        if (id instanceof Integer) return (Integer) id;
        if (id instanceof Number) return ((Number) id).intValue();
        return null;
    }

    // 提交下载任务到MQ队列
    @PostMapping("/enqueue")
    @Operation(summary = "提交下载任务", description = "返回任务ID，客户端轮询进度")
    public Result<String> enqueue(@RequestBody List<Long> ids) {
        Integer uid = currentUserId();
        if (uid == null) return Result.error("未登录");
        String taskId = UUID.randomUUID().toString();
        String taskKey = "download:task:" + taskId;
        redis.opsForHash().put(taskKey, "status", "queued");
        redis.opsForHash().put(taskKey, "uid", String.valueOf(uid));
        redis.expire(taskKey, Duration.ofHours(2));
        // 发送MQ消息到下载队列
        java.util.HashMap<String, Object> msg = new java.util.HashMap<>();
        msg.put("taskId", taskId);
        msg.put("uid", uid);
        msg.put("ids", ids);
        rabbitTemplate.convertAndSend(com.lyz.config.RabbitConfig.EXCHANGE, com.lyz.config.RabbitConfig.ROUTING_KEY, msg);
        return Result.success(taskId);
    }

    @GetMapping("/status/{taskId}")
    @Operation(summary = "查询任务状态", description = "queued/processing/done/error")
    public Result<Map<String, String>> status(@PathVariable String taskId) {
        String taskKey = "download:task:" + taskId;
        Map<Object, Object> map = redis.opsForHash().entries(taskKey);
        if (map == null || map.isEmpty()) return Result.error("任务不存在或过期");
        Map<String, String> ret = new HashMap<>();
        map.forEach((k, v) -> ret.put(String.valueOf(k), String.valueOf(v)));
        return Result.success(ret);
    }

    // 从Redis获取Base64编码的ZIP结果并解码返回
    @GetMapping("/result/{taskId}")
    @Operation(summary = "下载打包结果", description = "任务完成后下载zip")
    public ResponseEntity<byte[]> result(@PathVariable String taskId) throws IOException {
        String key = "download:task:" + taskId + ":zip";
        String b64 = redis.opsForValue().get(key);
        byte[] bytes = (b64 == null) ? null : java.util.Base64.getDecoder().decode(b64);
        if (bytes == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=async-pollution-data.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }
}


