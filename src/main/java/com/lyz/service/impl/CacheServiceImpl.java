package com.lyz.service.impl;

import com.lyz.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 缓存服务实现类
 * 提供统一的Redis缓存管理功能
 */
@Service
public class CacheServiceImpl implements CacheService {
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public void set(String key, String value, long timeout, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }
    
    @Override
    public void setObject(String key, Object value, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }
    
    @Override
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }
    
    @Override
    public <T> T getObject(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null && clazz.isInstance(value)) {
            return clazz.cast(value);
        }
        return null;
    }
    
    @Override
    public boolean delete(String key) {
        Boolean result = stringRedisTemplate.delete(key);
        return result != null && result;
    }
    
    @Override
    public boolean exists(String key) {
        Boolean result = stringRedisTemplate.hasKey(key);
        return result != null && result;
    }
    
    @Override
    public boolean expire(String key, long timeout, TimeUnit timeUnit) {
        Boolean result = stringRedisTemplate.expire(key, timeout, timeUnit);
        return result != null && result;
    }
    
    @Override
    public Long increment(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }
    
    @Override
    public Long increment(String key, long delta) {
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }
    
    @Override
    public Long decrement(String key) {
        return stringRedisTemplate.opsForValue().decrement(key);
    }
    
    @Override
    public boolean tryLock(String lockKey, String lockValue, long timeout, TimeUnit timeUnit) {
        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, timeout, timeUnit);
        return result != null && result;
    }
    
    @Override
    public boolean releaseLock(String lockKey, String lockValue) {
        // 使用Lua脚本确保原子性
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                       "return redis.call('del', KEYS[1]) " +
                       "else return 0 end";
        
        Long result = stringRedisTemplate.execute(
            (org.springframework.data.redis.core.RedisCallback<Long>) connection -> 
                connection.eval(script.getBytes(), 
                    org.springframework.data.redis.connection.ReturnType.INTEGER, 
                    1, 
                    lockKey.getBytes(), 
                    lockValue.getBytes())
        );
        
        return result != null && result == 1;
    }
}
