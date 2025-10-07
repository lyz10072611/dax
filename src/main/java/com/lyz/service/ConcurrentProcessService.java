package com.lyz.service;

import com.lyz.pojo.CementPlantDataDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 高并发处理服务接口
 * 提供异步处理、批量操作等功能
 */
public interface ConcurrentProcessService {
    
    /**
     * 异步处理水泥厂数据上传
     * @param dataDTO 数据DTO
     * @return 异步处理结果
     */
    CompletableFuture<Void> asyncProcessCementPlantData(CementPlantDataDTO dataDTO);
    
    /**
     * 批量异步处理水泥厂数据上传
     * @param dataList 数据列表
     * @return 异步处理结果
     */
    CompletableFuture<Void> asyncBatchProcessCementPlantData(List<CementPlantDataDTO> dataList);
    
    /**
     * 异步处理TIF文件
     * @param fileId 文件ID
     * @param filePath 文件路径
     * @return 异步处理结果
     */
    CompletableFuture<Void> asyncProcessTifFile(Long fileId, String filePath);
    
    /**
     * 异步批量插入数据库
     * @param dataList 数据列表
     * @param batchSize 批次大小
     * @return 异步处理结果
     */
    CompletableFuture<Void> asyncBatchInsert(List<?> dataList, int batchSize);
    
    /**
     * 异步更新缓存
     * @param cacheKey 缓存键
     * @param data 数据
     * @return 异步处理结果
     */
    CompletableFuture<Void> asyncUpdateCache(String cacheKey, Object data);
    
    /**
     * 异步清理过期缓存
     * @return 异步处理结果
     */
    CompletableFuture<Void> asyncCleanExpiredCache();
    
    /**
     * 异步生成统计报告
     * @return 异步处理结果
     */
    CompletableFuture<Void> asyncGenerateStatisticsReport();
    
    /**
     * 异步备份数据
     * @param tableName 表名
     * @return 异步处理结果
     */
    CompletableFuture<Void> asyncBackupData(String tableName);
}
