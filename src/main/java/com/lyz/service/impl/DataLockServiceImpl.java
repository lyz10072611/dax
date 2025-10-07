package com.lyz.service.impl;

import com.lyz.service.CacheService;
import com.lyz.service.DataLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 数据加锁服务实现类
 */
@Service
public class DataLockServiceImpl implements DataLockService {
    
    @Autowired
    private CacheService cacheService;
    
    // 锁键前缀
    private static final String CEMENT_PLANT_LOCK_PREFIX = "lock:cement_plant:";
    private static final String FILE_PROCESS_LOCK_PREFIX = "lock:file_process:";
    private static final String USER_DOWNLOAD_LOCK_PREFIX = "lock:user_download:";
    private static final String DATABASE_LOCK_PREFIX = "lock:database:";
    
    @Override
    public String acquireCementPlantLock(String plantName, int timeoutSeconds) {
        String lockKey = CEMENT_PLANT_LOCK_PREFIX + plantName;
        String lockValue = UUID.randomUUID().toString();
        
        boolean acquired = cacheService.tryLock(lockKey, lockValue, timeoutSeconds, TimeUnit.SECONDS);
        return acquired ? lockValue : null;
    }
    
    @Override
    public boolean releaseCementPlantLock(String plantName, String lockValue) {
        String lockKey = CEMENT_PLANT_LOCK_PREFIX + plantName;
        return cacheService.releaseLock(lockKey, lockValue);
    }
    
    @Override
    public String acquireFileProcessLock(Long fileId, int timeoutSeconds) {
        String lockKey = FILE_PROCESS_LOCK_PREFIX + fileId;
        String lockValue = UUID.randomUUID().toString();
        
        boolean acquired = cacheService.tryLock(lockKey, lockValue, timeoutSeconds, TimeUnit.SECONDS);
        return acquired ? lockValue : null;
    }
    
    @Override
    public boolean releaseFileProcessLock(Long fileId, String lockValue) {
        String lockKey = FILE_PROCESS_LOCK_PREFIX + fileId;
        return cacheService.releaseLock(lockKey, lockValue);
    }
    
    @Override
    public String acquireUserDownloadLock(Integer userId, int timeoutSeconds) {
        String lockKey = USER_DOWNLOAD_LOCK_PREFIX + userId;
        String lockValue = UUID.randomUUID().toString();
        
        boolean acquired = cacheService.tryLock(lockKey, lockValue, timeoutSeconds, TimeUnit.SECONDS);
        return acquired ? lockValue : null;
    }
    
    @Override
    public boolean releaseUserDownloadLock(Integer userId, String lockValue) {
        String lockKey = USER_DOWNLOAD_LOCK_PREFIX + userId;
        return cacheService.releaseLock(lockKey, lockValue);
    }
    
    @Override
    public String acquireDatabaseLock(String operationType, String resourceId, int timeoutSeconds) {
        String lockKey = DATABASE_LOCK_PREFIX + operationType + ":" + resourceId;
        String lockValue = UUID.randomUUID().toString();
        
        boolean acquired = cacheService.tryLock(lockKey, lockValue, timeoutSeconds, TimeUnit.SECONDS);
        return acquired ? lockValue : null;
    }
    
    @Override
    public boolean releaseDatabaseLock(String operationType, String resourceId, String lockValue) {
        String lockKey = DATABASE_LOCK_PREFIX + operationType + ":" + resourceId;
        return cacheService.releaseLock(lockKey, lockValue);
    }
    
    @Override
    public <T> T executeWithLock(String lockKey, int timeoutSeconds, LockedOperation<T> operation) {
        String lockValue = UUID.randomUUID().toString();
        
        try {
            // 尝试获取锁
            boolean acquired = cacheService.tryLock(lockKey, lockValue, timeoutSeconds, TimeUnit.SECONDS);
            if (!acquired) {
                throw new RuntimeException("获取锁失败: " + lockKey);
            }
            
            // 执行操作
            return operation.execute();
            
        } catch (Exception e) {
            throw new RuntimeException("执行带锁操作失败: " + e.getMessage(), e);
        } finally {
            // 释放锁
            cacheService.releaseLock(lockKey, lockValue);
        }
    }
}
