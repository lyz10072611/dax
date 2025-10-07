package com.lyz.controller;

import com.lyz.pojo.Result;
import com.lyz.service.DownloadService;
import com.lyz.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据下载控制器
 * 提供TIF文件下载功能，支持权限控制和下载配额管理
 */
@RestController
@RequestMapping("/data-download")
@Validated
@Tag(name = "数据下载", description = "TIF文件下载和权限管理接口")
public class DataDownloadController {
    
    @Autowired
    private DownloadService downloadService;
    
    @GetMapping("/tif/{fileId}")
    @Operation(summary = "下载TIF文件", description = "根据文件ID下载TIF文件，需要权限验证")
    public ResponseEntity<Resource> downloadTifFile(@PathVariable Long fileId) {
        try {
            // 获取当前用户信息
            Map<String, Object> claims = ThreadLocalUtil.get();
            if (claims == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            Integer userId = (Integer) claims.get("id");
            Integer roleCode = (Integer) claims.get("roleCode");
            
            // 检查下载权限和配额
            boolean canDownload = downloadService.checkDownloadPermission(userId, roleCode);
            if (!canDownload) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-Reason", "Daily quota exceeded")
                    .build();
            }
            
            // 执行下载
            Resource resource = downloadService.downloadTifFile(fileId, userId);
            
            if (resource != null && resource.exists()) {
                // 增加下载计数
                downloadService.incrementDownloadCount(userId);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", resource.getFilename());
                headers.setCacheControl("no-cache");
                
                return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/batch-download")
    @Operation(summary = "批量下载TIF文件", description = "批量下载多个TIF文件，打包为ZIP格式")
    public ResponseEntity<Resource> batchDownloadTifFiles(@RequestBody List<Long> fileIds) {
        try {
            // 获取当前用户信息
            Map<String, Object> claims = ThreadLocalUtil.get();
            if (claims == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            Integer userId = (Integer) claims.get("id");
            Integer roleCode = (Integer) claims.get("roleCode");
            
            // 检查下载权限和配额
            boolean canDownload = downloadService.checkDownloadPermission(userId, roleCode);
            if (!canDownload) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-Reason", "Daily quota exceeded")
                    .build();
            }
            
            // 执行批量下载
            Resource resource = downloadService.batchDownloadTifFiles(fileIds, userId);
            
            if (resource != null && resource.exists()) {
                // 增加下载计数
                downloadService.incrementDownloadCount(userId);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", "cement_plant_files.zip");
                headers.setCacheControl("no-cache");
                
                return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/download-status")
    @Operation(summary = "获取下载状态", description = "获取当前用户的下载权限和配额信息")
    public Result<Map<String, Object>> getDownloadStatus() {
        try {
            Map<String, Object> claims = ThreadLocalUtil.get();
            if (claims == null) {
                return Result.unauthorized("用户未登录");
            }
            
            Integer userId = (Integer) claims.get("id");
            Integer roleCode = (Integer) claims.get("roleCode");
            
            Map<String, Object> status = downloadService.getDownloadStatus(userId, roleCode);
            return Result.success(status);
            
        } catch (Exception e) {
            return Result.internalServerError("获取下载状态失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/download-history")
    @Operation(summary = "获取下载历史", description = "获取当前用户的下载历史记录")
    public Result<List<Map<String, Object>>> getDownloadHistory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Map<String, Object> claims = ThreadLocalUtil.get();
            if (claims == null) {
                return Result.unauthorized("用户未登录");
            }
            
            Integer userId = (Integer) claims.get("id");
            List<Map<String, Object>> history = downloadService.getDownloadHistory(userId, pageNum, pageSize);
            return Result.success(history);
            
        } catch (Exception e) {
            return Result.internalServerError("获取下载历史失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/async-download")
    @Operation(summary = "异步下载", description = "提交异步下载任务，支持大文件下载")
    public Result<String> submitAsyncDownloadTask(@RequestBody List<Long> fileIds) {
        try {
            Map<String, Object> claims = ThreadLocalUtil.get();
            if (claims == null) {
                return Result.unauthorized("用户未登录");
            }
            
            Integer userId = (Integer) claims.get("id");
            Integer roleCode = (Integer) claims.get("roleCode");
            
            // 检查下载权限
            boolean canDownload = downloadService.checkDownloadPermission(userId, roleCode);
            if (!canDownload) {
                return Result.tooManyRequests("今日下载次数已达上限");
            }
            
            String taskId = downloadService.submitAsyncDownloadTask(fileIds, userId);
            return Result.success(taskId);
            
        } catch (Exception e) {
            return Result.internalServerError("提交下载任务失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/async-download/{taskId}/status")
    @Operation(summary = "查询异步下载任务状态", description = "查询异步下载任务的执行状态")
    public Result<Map<String, Object>> getAsyncDownloadTaskStatus(@PathVariable String taskId) {
        try {
            Map<String, Object> claims = ThreadLocalUtil.get();
            if (claims == null) {
                return Result.unauthorized("用户未登录");
            }
            
            Integer userId = (Integer) claims.get("id");
            Map<String, Object> status = downloadService.getAsyncDownloadTaskStatus(taskId, userId);
            return Result.success(status);
            
        } catch (Exception e) {
            return Result.internalServerError("查询任务状态失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/async-download/{taskId}/result")
    @Operation(summary = "下载异步任务结果", description = "下载异步任务的处理结果")
    public ResponseEntity<Resource> downloadAsyncTaskResult(@PathVariable String taskId) {
        try {
            Map<String, Object> claims = ThreadLocalUtil.get();
            if (claims == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            Integer userId = (Integer) claims.get("id");
            Resource resource = downloadService.downloadAsyncTaskResult(taskId, userId);
            
            if (resource != null && resource.exists()) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", "async_download_result.zip");
                headers.setCacheControl("no-cache");
                
                return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
