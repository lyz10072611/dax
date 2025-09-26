package com.lyz.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lyz.mapper.PollutionDataMapper;
import com.lyz.pojo.PageBean;
import com.lyz.pojo.PollutionData;
import com.lyz.service.PollutionDataService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
public class PollutionDataServiceImpl implements PollutionDataService {
    @Autowired
    private PollutionDataMapper mapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String LOCK_KEY = "pollution_data_lock";
    private static final long LOCK_EXPIRE_TIME = 10; // 锁过期时间，单位：秒

    private ThreadLocal<String> lockValueHolder = new ThreadLocal<>();

    @Override
    public PageBean<PollutionData> page(Integer pageNum, Integer pageSize, Integer dataFormat, String pollutantType, Integer year, Integer month, Integer day, Integer hour) {
        PageHelper.startPage(pageNum, pageSize);
        List<PollutionData> list = mapper.list(dataFormat, pollutantType, year, month, day, hour);
        PageInfo<PollutionData> info = new PageInfo<>(list);
        return new PageBean<>(info.getTotal(), info.getList());
    }

    @Override
    public PollutionData findById(Long id) { return mapper.findById(id); }

    @Override
    public void add(PollutionData data) { mapper.add(data); }

    @Override
    public synchronized void update(PollutionData data) {
        if (!acquireLock()) {
            throw new IllegalStateException("Another operation is in progress. Please try again later.");
        }

        try {
            // 执行数据更新逻辑
            mapper.update(data);
        } finally {
            releaseLock();
        }
    }

    @Override
    public void delete(Long id) { mapper.delete(id); }

    @Override
    public java.util.List<PollutionData> findByIds(java.util.List<Long> ids) {
        return mapper.findByIds(ids);
    }

    @Override
    public synchronized ResponseEntity<String> download(List<Long> ids) {
        if (!acquireLock()) {
            return ResponseEntity.status(423).body("Another download is in progress. Please try again later.");
        }

        try {
            // 将下载任务发送到消息队列
            rabbitTemplate.convertAndSend("downloadQueue", ids);
            return ResponseEntity.ok("Download task has been submitted successfully.");
        } finally {
            releaseLock();
        }
    }

    @Override
    public List<String> getImagesForDay(String date) {
        String cacheKey = "pollution:images:" + date;
        List<String> images = redisTemplate.opsForList().range(cacheKey, 0, -1);
        if (images == null || images.isEmpty()) {
            images = mapper.getImagesForDay(date); // 从数据库加载影像路径
            redisTemplate.opsForList().rightPushAll(cacheKey, images);
            redisTemplate.expire(cacheKey, 1, TimeUnit.DAYS); // 缓存 1 天
        }
        return images;
    }

    @Override
    public void streamAllData(Consumer<PollutionData> consumer) {
        mapper.streamAllData(consumer);
    }

    private boolean acquireLock() {
        String lockValue = UUID.randomUUID().toString();
        lockValueHolder.set(lockValue);
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, lockValue, LOCK_EXPIRE_TIME, TimeUnit.SECONDS));
    }

    private void releaseLock() {
        String lockValue = lockValueHolder.get();
        if (lockValue != null && lockValue.equals(redisTemplate.opsForValue().get(LOCK_KEY))) {
            redisTemplate.delete(LOCK_KEY);
        }
        lockValueHolder.remove();
    }

    @Async
    @Override
    public CompletableFuture<Void> asyncUploadData(PollutionData data) {
        return CompletableFuture.runAsync(() -> {
            mapper.add(data);
        });
    }
}


