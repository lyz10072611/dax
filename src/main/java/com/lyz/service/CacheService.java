package com.lyz.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口
 * 提供统一的缓存管理功能
 */
public interface CacheService {
    
    /**
     * 设置缓存
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间
     * @param timeUnit 时间单位
     */
    void set(String key, String value, long timeout, TimeUnit timeUnit);
    
    /**
     * 设置缓存（对象）
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间
     * @param timeUnit 时间单位
     */
    void setObject(String key, Object value, long timeout, TimeUnit timeUnit);
    
    /**
     * 获取缓存
     * @param key 缓存键
     * @return 缓存值
     */
    String get(String key);
    
    /**
     * 获取缓存（对象）
     * @param key 缓存键
     * @param clazz 对象类型
     * @return 缓存对象
     */
    <T> T getObject(String key, Class<T> clazz);
    
    /**
     * 删除缓存
     * @param key 缓存键
     * @return 是否删除成功
     */
    boolean delete(String key);
    
    /**
     * 检查缓存是否存在
     * @param key 缓存键
     * @return 是否存在
     */
    boolean exists(String key);
    
    /**
     * 设置过期时间
     * @param key 缓存键
     * @param timeout 过期时间
     * @param timeUnit 时间单位
     * @return 是否设置成功
     */
    boolean expire(String key, long timeout, TimeUnit timeUnit);
    
    /**
     * 原子递增
     * @param key 缓存键
     * @return 递增后的值
     */
    Long increment(String key);
    
    /**
     * 原子递增（指定步长）
     * @param key 缓存键
     * @param delta 步长
     * @return 递增后的值
     */
    Long increment(String key, long delta);
    
    /**
     * 原子递减
     * @param key 缓存键
     * @return 递减后的值
     */
    Long decrement(String key);
    
    /**
     * 分布式锁
     * @param lockKey 锁键
     * @param lockValue 锁值
     * @param timeout 超时时间
     * @param timeUnit 时间单位
     * @return 是否获取锁成功
     */
    boolean tryLock(String lockKey, String lockValue, long timeout, TimeUnit timeUnit);
    
    /**
     * 释放分布式锁
     * @param lockKey 锁键
     * @param lockValue 锁值
     * @return 是否释放成功
     */
    boolean releaseLock(String lockKey, String lockValue);
}
