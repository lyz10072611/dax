package com.lyz.service;

/**
 * 数据加锁服务接口
 * 提供分布式锁功能，确保数据一致性
 */
public interface DataLockService {
    
    /**
     * 获取水泥厂数据锁
     * @param plantName 水泥厂名称
     * @param timeoutSeconds 超时时间（秒）
     * @return 锁值，如果获取失败返回null
     */
    String acquireCementPlantLock(String plantName, int timeoutSeconds);
    
    /**
     * 释放水泥厂数据锁
     * @param plantName 水泥厂名称
     * @param lockValue 锁值
     * @return 是否释放成功
     */
    boolean releaseCementPlantLock(String plantName, String lockValue);
    
    /**
     * 获取文件处理锁
     * @param fileId 文件ID
     * @param timeoutSeconds 超时时间（秒）
     * @return 锁值，如果获取失败返回null
     */
    String acquireFileProcessLock(Long fileId, int timeoutSeconds);
    
    /**
     * 释放文件处理锁
     * @param fileId 文件ID
     * @param lockValue 锁值
     * @return 是否释放成功
     */
    boolean releaseFileProcessLock(Long fileId, String lockValue);
    
    /**
     * 获取用户下载锁
     * @param userId 用户ID
     * @param timeoutSeconds 超时时间（秒）
     * @return 锁值，如果获取失败返回null
     */
    String acquireUserDownloadLock(Integer userId, int timeoutSeconds);
    
    /**
     * 释放用户下载锁
     * @param userId 用户ID
     * @param lockValue 锁值
     * @return 是否释放成功
     */
    boolean releaseUserDownloadLock(Integer userId, String lockValue);
    
    /**
     * 获取数据库操作锁
     * @param operationType 操作类型
     * @param resourceId 资源ID
     * @param timeoutSeconds 超时时间（秒）
     * @return 锁值，如果获取失败返回null
     */
    String acquireDatabaseLock(String operationType, String resourceId, int timeoutSeconds);
    
    /**
     * 释放数据库操作锁
     * @param operationType 操作类型
     * @param resourceId 资源ID
     * @param lockValue 锁值
     * @return 是否释放成功
     */
    boolean releaseDatabaseLock(String operationType, String resourceId, String lockValue);
    
    /**
     * 执行带锁的操作
     * @param lockKey 锁键
     * @param timeoutSeconds 超时时间（秒）
     * @param operation 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     */
    <T> T executeWithLock(String lockKey, int timeoutSeconds, LockedOperation<T> operation);
    
    /**
     * 带锁操作接口
     */
    @FunctionalInterface
    interface LockedOperation<T> {
        T execute() throws Exception;
    }
}
