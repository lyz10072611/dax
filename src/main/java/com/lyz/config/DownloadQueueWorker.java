package com.lyz.config;

import com.lyz.pojo.PollutionData;
import com.lyz.service.PollutionDataService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Configuration
public class DownloadQueueWorker {
    @Autowired
    private StringRedisTemplate redis;
    @Autowired
    private PollutionDataService service;
     
    // RabbitMQ消费者：处理下载任务并打包ZIP
    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onMessage(Map<String, Object> msg) {
        String taskId = (String) msg.get("taskId");
        @SuppressWarnings("unchecked")
        List<Integer> idsInt = (List<Integer>) msg.get("ids");
        List<Long> ids = idsInt.stream().map(Integer::longValue).map(Long::valueOf).toList();
        String taskKey = "download:task:" + taskId;
        redis.opsForHash().put(taskKey, "status", "processing");
        try {
            List<PollutionData> list = service.findByIds(ids);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                // 遍历文件列表，将每个文件添加到ZIP包中
                for (PollutionData pd : list) {
                    Path path = Paths.get(pd.getFilePath());
                    if (!Files.exists(path)) continue;
                    zos.putNextEntry(new ZipEntry(path.getFileName().toString()));
                    try (InputStream in = Files.newInputStream(path)) {
                        in.transferTo(zos);
                    }
                    zos.closeEntry();
                }
            }
            // 将ZIP结果Base64编码后存储到Redis
            String zipKey = "download:task:" + taskId + ":zip";
            String b64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            int baseExpire = 2 * 60 * 60; // 基础过期时间，单位：秒
            int randomExpire = new Random().nextInt(30 * 60); // 随机时间，单位：秒
            redis.opsForValue().set(zipKey, b64, Duration.ofSeconds(baseExpire + randomExpire));
            redis.opsForHash().put(taskKey, "status", "done");
            redis.expire(taskKey, Duration.ofSeconds(baseExpire + randomExpire));
        } catch (Exception e) {
            redis.opsForHash().put(taskKey, "status", "error");
        }
    }
}


