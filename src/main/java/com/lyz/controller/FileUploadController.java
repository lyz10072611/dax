package com.lyz.controller;

import com.lyz.pojo.Result;
import com.lyz.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
@Validated
@Tag(name = "文件管理", description = "文件增删改查，除查询外需管理员")
public class FileUploadController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private boolean isAdminByDb() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims == null) return false;
        Object id = claims.get("id");
        Integer uid = (id instanceof Number) ? ((Number) id).intValue() : null;
        if (uid == null) return false;
        // 简易：与污染物一致，可注入UserService做DB校验；此处先保留返回false避免循环依赖
        return false;
    }

    @Data
    public static class FileInfo {
        private String name;
        private Long size;
        private Long lastModified;
        private String path;
    }

    @GetMapping
    @Operation(summary = "查询文件列表", description = "无需管理员")
    public Result<List<FileInfo>> list() throws IOException {
        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        try (var stream = Files.list(dir)) {
            List<FileInfo> files = stream
                    .filter(Files::isRegularFile)
                    .map(p -> {
                        File f = p.toFile();
                        FileInfo info = new FileInfo();
                        info.setName(f.getName());
                        info.setSize(f.length());
                        info.setLastModified(f.lastModified());
                        info.setPath(p.toString());
                        return info;
                    })
                    .collect(Collectors.toList());
            return Result.success(files);
        }
    }

    @GetMapping("/{name}")
    @Operation(summary = "查询单个文件信息", description = "无需管理员")
    public Result<FileInfo> get(@PathVariable String name) throws IOException {
        Path p = Paths.get(uploadDir, name);
        if (!Files.exists(p) || !Files.isRegularFile(p)) {
            return Result.error("文件不存在");
        }
        File f = p.toFile();
        FileInfo info = new FileInfo();
        info.setName(f.getName());
        info.setSize(f.length());
        info.setLastModified(f.lastModified());
        info.setPath(p.toString());
        return Result.success(info);
    }

    @PostMapping
    @Operation(summary = "上传文件", description = "仅管理员")
    public Result<String> upload(@RequestPart("file") MultipartFile file) throws IOException {
        if (!isAdminByDb()) return Result.error("无权限");
        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        String filename = Objects.requireNonNullElse(file.getOriginalFilename(), UUID.randomUUID()+".dat");
        Path target = dir.resolve(filename);
        file.transferTo(target.toFile());
        return Result.success(filename);
    }

    @PutMapping("/{name}")
    @Operation(summary = "替换文件", description = "仅管理员")
    public Result replace(@PathVariable String name, @RequestPart("file") MultipartFile file) throws IOException {
        if (!isAdminByDb()) return Result.error("无权限");
        Path target = Paths.get(uploadDir, name);
        if (!Files.exists(target)) return Result.error("文件不存在");
        file.transferTo(target.toFile());
        return Result.success();
    }

    @DeleteMapping("/{name}")
    @Operation(summary = "删除文件", description = "仅管理员")
    public Result delete(@PathVariable String name) throws IOException {
        if (!isAdminByDb()) return Result.error("无权限");
        Path target = Paths.get(uploadDir, name);
        if (!Files.exists(target)) return Result.error("文件不存在");
        Files.delete(target);
        return Result.success();
    }
}

