package com.lyz.service;

import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

/**
 * 下载服务接口
 * 提供TIF文件下载、权限控制、配额管理等功能
 */
public interface DownloadService {
    
    /**
     * 检查用户下载权限
     * @param userId 用户ID
     * @param roleCode 角色代码
     * @return 是否可以下载
     */
    boolean checkDownloadPermission(Integer userId, Integer roleCode);
    
    /**
     * 下载单个TIF文件
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 文件资源
     */
    Resource downloadTifFile(Long fileId, Integer userId);
    
    /**
     * 批量下载TIF文件
     * @param fileIds 文件ID列表
     * @param userId 用户ID
     * @return ZIP文件资源
     */
    Resource batchDownloadTifFiles(List<Long> fileIds, Integer userId);
    
    /**
     * 增加下载计数
     * @param userId 用户ID
     */
    void incrementDownloadCount(Integer userId);
    
    /**
     * 获取下载状态
     * @param userId 用户ID
     * @param roleCode 角色代码
     * @return 下载状态信息
     */
    Map<String, Object> getDownloadStatus(Integer userId, Integer roleCode);
    
    /**
     * 获取下载历史
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 下载历史记录
     */
    List<Map<String, Object>> getDownloadHistory(Integer userId, Integer pageNum, Integer pageSize);
    
    /**
     * 提交异步下载任务
     * @param fileIds 文件ID列表
     * @param userId 用户ID
     * @return 任务ID
     */
    String submitAsyncDownloadTask(List<Long> fileIds, Integer userId);
    
    /**
     * 获取异步下载任务状态
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 任务状态
     */
    Map<String, Object> getAsyncDownloadTaskStatus(String taskId, Integer userId);
    
    /**
     * 下载异步任务结果
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 结果文件资源
     */
    Resource downloadAsyncTaskResult(String taskId, Integer userId);
}
