package com.lyz.service.impl;

import com.lyz.mapper.GeospatialMapper;
import com.lyz.pojo.GeospatialFile;
import com.lyz.service.DownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 下载服务实现类
 */
@Service
public class DownloadServiceImpl implements DownloadService {
    
    @Autowired
    private GeospatialMapper geospatialMapper;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    // 下载配额配置
    private static final int ADMIN_DAILY_QUOTA = -1; // 管理员无限制
    private static final int USER_DAILY_QUOTA = 500; // 普通用户每日500次
    private static final int GUEST_DAILY_QUOTA = 0; // 游客无下载权限
    
    @Override
    public boolean checkDownloadPermission(Integer userId, Integer roleCode) {
        if (roleCode == null) {
            return false;
        }
        
        // 管理员无限制
        if (roleCode == 0) {
            return true;
        }
        
        // 游客无下载权限
        if (roleCode == 2) {
            return false;
        }
        
        // 普通用户检查每日配额
        if (roleCode == 1) {
            return checkDailyQuota(userId, USER_DAILY_QUOTA);
        }
        
        return false;
    }
    
    /**
     * 检查每日下载配额
     */
    private boolean checkDailyQuota(Integer userId, int dailyQuota) {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String quotaKey = "download_quota:" + userId + ":" + today;
        
        String currentCountStr = stringRedisTemplate.opsForValue().get(quotaKey);
        int currentCount = currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;
        
        return currentCount < dailyQuota;
    }
    
    @Override
    public Resource downloadTifFile(Long fileId, Integer userId) {
        try {
            // 查询文件信息
            GeospatialFile file = geospatialMapper.findFileById(fileId);
            if (file == null) {
                return null;
            }
            
            // 检查文件是否存在
            Path filePath = Paths.get(file.getFilePath());
            if (!Files.exists(filePath)) {
                return null;
            }
            
            // 记录下载历史
            recordDownloadHistory(userId, fileId, file.getFileName(), "single");
            
            return new FileSystemResource(filePath.toFile());
            
        } catch (Exception e) {
            System.err.println("下载TIF文件失败: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public Resource batchDownloadTifFiles(List<Long> fileIds, Integer userId) {
        try {
            // 创建临时ZIP文件
            String tempZipName = "cement_plant_files_" + System.currentTimeMillis() + ".zip";
            Path tempZipPath = Paths.get(System.getProperty("java.io.tmpdir"), tempZipName);
            
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(tempZipPath))) {
                for (Long fileId : fileIds) {
                    GeospatialFile file = geospatialMapper.findFileById(fileId);
                    if (file != null) {
                        Path filePath = Paths.get(file.getFilePath());
                        if (Files.exists(filePath)) {
                            ZipEntry entry = new ZipEntry(file.getFileName());
                            zos.putNextEntry(entry);
                            Files.copy(filePath, zos);
                            zos.closeEntry();
                        }
                    }
                }
            }
            
            // 记录下载历史
            recordDownloadHistory(userId, null, "batch_" + fileIds.size() + "_files", "batch");
            
            return new FileSystemResource(tempZipPath.toFile());
            
        } catch (Exception e) {
            System.err.println("批量下载TIF文件失败: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public void incrementDownloadCount(Integer userId) {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String quotaKey = "download_quota:" + userId + ":" + today;
        String totalKey = "download_total:" + userId;
        
        // 使用Redis原子操作增加计数
        stringRedisTemplate.opsForValue().increment(quotaKey);
        stringRedisTemplate.expire(quotaKey, 1, TimeUnit.DAYS); // 设置过期时间为1天
        
        stringRedisTemplate.opsForValue().increment(totalKey);
    }
    
    @Override
    public Map<String, Object> getDownloadStatus(Integer userId, Integer roleCode) {
        Map<String, Object> status = new HashMap<>();
        
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String quotaKey = "download_quota:" + userId + ":" + today;
        String totalKey = "download_total:" + userId;
        
        // 获取今日下载次数
        String todayCountStr = stringRedisTemplate.opsForValue().get(quotaKey);
        int todayCount = todayCountStr != null ? Integer.parseInt(todayCountStr) : 0;
        
        // 获取总下载次数
        String totalCountStr = stringRedisTemplate.opsForValue().get(totalKey);
        int totalCount = totalCountStr != null ? Integer.parseInt(totalCountStr) : 0;
        
        // 根据角色确定配额
        int dailyQuota;
        boolean canDownload;
        
        if (roleCode == 0) {
            dailyQuota = ADMIN_DAILY_QUOTA;
            canDownload = true;
        } else if (roleCode == 1) {
            dailyQuota = USER_DAILY_QUOTA;
            canDownload = todayCount < USER_DAILY_QUOTA;
        } else {
            dailyQuota = GUEST_DAILY_QUOTA;
            canDownload = false;
        }
        
        status.put("userId", userId);
        status.put("roleCode", roleCode);
        status.put("todayCount", todayCount);
        status.put("totalCount", totalCount);
        status.put("dailyQuota", dailyQuota);
        status.put("canDownload", canDownload);
        status.put("remainingQuota", dailyQuota == -1 ? -1 : Math.max(0, dailyQuota - todayCount));
        
        return status;
    }
    
    @Override
    public List<Map<String, Object>> getDownloadHistory(Integer userId, Integer pageNum, Integer pageSize) {
        // 这里可以从数据库或Redis中获取下载历史
        // 简化实现，返回空列表
        return new ArrayList<>();
    }
    
    @Override
    public String submitAsyncDownloadTask(List<Long> fileIds, Integer userId) {
        // 生成任务ID
        String taskId = "async_download_" + userId + "_" + System.currentTimeMillis();
        
        // 将任务信息存储到Redis
        Map<String, Object> taskInfo = new HashMap<>();
        taskInfo.put("userId", userId);
        taskInfo.put("fileIds", fileIds);
        taskInfo.put("status", "queued");
        taskInfo.put("createTime", LocalDateTime.now().toString());
        
        stringRedisTemplate.opsForValue().set("async_task:" + taskId, 
            taskInfo.toString(), 24, TimeUnit.HOURS);
        
        // 这里应该将任务发送到RabbitMQ队列进行异步处理
        // 简化实现，直接返回任务ID
        
        return taskId;
    }
    
    @Override
    public Map<String, Object> getAsyncDownloadTaskStatus(String taskId, Integer userId) {
        String taskKey = "async_task:" + taskId;
        String taskInfoStr = stringRedisTemplate.opsForValue().get(taskKey);
        
        Map<String, Object> status = new HashMap<>();
        if (taskInfoStr != null) {
            // 解析任务信息
            status.put("taskId", taskId);
            status.put("status", "processing"); // 简化实现
            status.put("progress", 50);
        } else {
            status.put("taskId", taskId);
            status.put("status", "not_found");
        }
        
        return status;
    }
    
    @Override
    public Resource downloadAsyncTaskResult(String taskId, Integer userId) {
        // 简化实现，返回null
        // 实际应该从Redis或文件系统中获取处理结果
        return null;
    }
    
    /**
     * 记录下载历史
     */
    private void recordDownloadHistory(Integer userId, Long fileId, String fileName, String downloadType) {
        try {
            String historyKey = "download_history:" + userId;
            Map<String, Object> historyRecord = new HashMap<>();
            historyRecord.put("fileId", fileId);
            historyRecord.put("fileName", fileName);
            historyRecord.put("downloadType", downloadType);
            historyRecord.put("downloadTime", LocalDateTime.now().toString());
            
            // 将历史记录存储到Redis列表
            stringRedisTemplate.opsForList().leftPush(historyKey, historyRecord.toString());
            stringRedisTemplate.expire(historyKey, 30, TimeUnit.DAYS); // 保存30天
            
        } catch (Exception e) {
            System.err.println("记录下载历史失败: " + e.getMessage());
        }
    }
}
