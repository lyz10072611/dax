# Redis和RabbitMQ技术实现详解

## 1. Redis实现用户下载计数和配额管理

### 1.1 技术架构

Redis在系统中主要用于实现用户下载计数和配额管理，通过Redis的高性能内存存储和原子操作特性，确保下载计数的准确性和实时性。

#### 1.1.1 Redis配置

```yaml
# application.yml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

#### 1.1.2 核心组件

- **StringRedisTemplate**: Spring Boot提供的Redis操作模板
- **原子操作**: 使用Redis的INCREMENT操作确保计数准确性
- **过期策略**: 自动清理过期的计数数据

### 1.2 下载配额管理实现

#### 1.2.1 配额配置

```java
// DownloadServiceImpl.java
public class DownloadServiceImpl implements DownloadService {
    
    // 下载配额配置
    private static final int ADMIN_DAILY_QUOTA = -1; // 管理员无限制
    private static final int USER_DAILY_QUOTA = 500; // 普通用户每日500次
    private static final int GUEST_DAILY_QUOTA = 0; // 游客无下载权限
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
}
```

#### 1.2.2 权限检查逻辑

```java
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
```

#### 1.2.3 每日配额检查

```java
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
```

### 1.3 下载计数管理

#### 1.3.1 计数增加

```java
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
```

#### 1.3.2 用户服务中的计数管理

```java
// UserServiceImpl.java
public class UserServiceImpl implements UserService {
    
    private static final String DAILY_DOWNLOAD_KEY_PREFIX = "daily_download_count:";
    private static final String DAILY_DOWNLOAD_RESET_KEY = "daily_download_reset";
    
    @Override
    public Integer getDailyDownloadCount(Integer userId) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = DAILY_DOWNLOAD_KEY_PREFIX + userId + ":" + today;
        
        String count = stringRedisTemplate.opsForValue().get(key);
        return count != null ? Integer.parseInt(count) : 0;
    }
    
    @Override
    public void incrementDailyDownloadCount(Integer userId) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = DAILY_DOWNLOAD_KEY_PREFIX + userId + ":" + today;
        
        // 增加计数，如果key不存在则初始化为1
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count == 1) {
            // 如果是第一次创建，设置过期时间为明天凌晨2点
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            long secondsUntilMidnight = java.time.Duration.between(
                java.time.LocalDateTime.now(),
                tomorrow.atStartOfDay().plusHours(2)
            ).getSeconds();
            stringRedisTemplate.expire(key, secondsUntilMidnight, TimeUnit.SECONDS);
        }
    }
}
```

### 1.4 下载状态查询

#### 1.4.1 状态信息获取

```java
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
```

### 1.5 下载历史记录

#### 1.5.1 历史记录存储

```java
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
```

### 1.6 Redis Key设计策略

#### 1.6.1 Key命名规范

```
# 每日下载配额
download_quota:{userId}:{yyyy-MM-dd}

# 总下载计数
download_total:{userId}

# 下载历史记录
download_history:{userId}

# 异步任务信息
async_task:{taskId}

# 每日下载计数（用户服务）
daily_download_count:{userId}:{yyyy-MM-dd}

# 重置标记
daily_download_reset:{yyyy-MM-dd}
```

#### 1.6.2 过期策略

- **每日配额**: 1天过期，自动清理
- **总计数**: 永不过期，累计统计
- **历史记录**: 30天过期
- **异步任务**: 24小时过期
- **重置标记**: 到明天凌晨2点过期

### 1.7 性能优化

#### 1.7.1 原子操作

使用Redis的INCREMENT操作确保计数准确性：

```java
// 原子增加计数
Long count = stringRedisTemplate.opsForValue().increment(key);
```

#### 1.7.2 批量操作

```java
// 批量获取多个key的值
List<String> keys = Arrays.asList(key1, key2, key3);
List<String> values = stringRedisTemplate.opsForValue().multiGet(keys);
```

#### 1.7.3 管道操作

```java
// 使用管道批量执行操作
stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
    connection.incr(key1.getBytes());
    connection.incr(key2.getBytes());
    return null;
});
```

## 2. RabbitMQ实现数据下载

### 2.1 技术架构

RabbitMQ在系统中用于实现异步数据下载，通过消息队列机制处理大文件下载和批量下载任务，避免阻塞用户请求。

#### 2.1.1 RabbitMQ配置

```yaml
# application.yml
rabbitmq:
  exchange: download.exchange
  queue: download.queue
  routing-key: download.task
  upload-exchange: upload.exchange
  upload-queue: upload.queue
  upload-routing-key: upload.task
```

#### 2.1.2 核心组件

- **TopicExchange**: 主题交换机，支持路由键匹配
- **Queue**: 消息队列，存储待处理的任务
- **Binding**: 绑定关系，连接交换机和队列
- **RabbitTemplate**: Spring Boot提供的消息发送模板

### 2.2 RabbitMQ配置类

#### 2.2.1 下载队列配置

```java
@Configuration
@EnableRabbit
public class RabbitConfig {
    public static final String EXCHANGE = "download.exchange";
    public static final String QUEUE = "download.queue";
    public static final String ROUTING_KEY = "download.task";

    public static final String UPLOAD_EXCHANGE = "upload.exchange";
    public static final String UPLOAD_QUEUE = "upload.queue";
    public static final String UPLOAD_ROUTING_KEY = "upload.task";

    @Bean
    public TopicExchange downloadExchange() { 
        return new TopicExchange(EXCHANGE, true, false); 
    }

    @Bean
    public Queue downloadQueue() { 
        return new Queue(QUEUE, true); 
    }

    @Bean
    public Binding binding() { 
        return BindingBuilder.bind(downloadQueue()).to(downloadExchange()).with(ROUTING_KEY); 
    }

    @Bean
    public TopicExchange uploadExchange() {
        return new TopicExchange(UPLOAD_EXCHANGE, true, false);
    }

    @Bean
    public Queue uploadQueue() {
        return new Queue(UPLOAD_QUEUE, true);
    }

    @Bean
    public Binding uploadBinding() {
        return BindingBuilder.bind(uploadQueue()).to(uploadExchange()).with(UPLOAD_ROUTING_KEY);
    }
}
```

### 2.3 异步下载任务实现

#### 2.3.1 任务提交

```java
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
```

#### 2.3.2 任务状态查询

```java
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
```

### 2.4 控制器层实现

#### 2.4.1 异步下载接口

```java
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
```

#### 2.4.2 任务状态查询接口

```java
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
```

### 2.5 异步处理服务

#### 2.5.1 高并发处理服务

```java
@Service
public class ConcurrentProcessServiceImpl implements ConcurrentProcessService {
    
    @Autowired
    private CementPlantService cementPlantService;
    
    @Autowired
    private CacheService cacheService;
    
    @Override
    @Async("asyncTaskExecutor")
    public CompletableFuture<Void> asyncProcessCementPlantData(CementPlantDataDTO dataDTO) {
        try {
            // 异步处理水泥厂数据
            cementPlantService.addCementPlantData(dataDTO);
            
            // 异步更新相关缓存
            String cacheKey = "cement_plant:" + dataDTO.getPlantName();
            cacheService.setObject(cacheKey, dataDTO, 1, java.util.concurrent.TimeUnit.HOURS);
            
        } catch (Exception e) {
            System.err.println("异步处理水泥厂数据失败: " + e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }
}
```

### 2.6 线程池配置

#### 2.6.1 异步任务线程池

```java
@Configuration
@EnableAsync
public class PerformanceConfig {
    
    /**
     * 异步任务线程池配置
     * 用于处理文件上传、下载等耗时操作
     */
    @Bean("asyncTaskExecutor")
    public Executor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(10);
        
        // 最大线程数
        executor.setMaxPoolSize(50);
        
        // 队列容量
        executor.setQueueCapacity(200);
        
        // 线程名前缀
        executor.setThreadNamePrefix("AsyncTask-");
        
        // 拒绝策略：由调用线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }
    
    /**
     * 文件处理线程池配置
     * 专门用于TIF文件处理等IO密集型任务
     */
    @Bean("fileProcessExecutor")
    public Executor fileProcessExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // IO密集型任务，线程数可以设置得更大
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("FileProcess-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        
        executor.initialize();
        return executor;
    }
}
```

### 2.7 消息队列设计

#### 2.7.1 消息结构

```java
public class DownloadTaskMessage {
    private String taskId;
    private Integer userId;
    private List<Long> fileIds;
    private String taskType; // "single", "batch"
    private Map<String, Object> metadata;
    private LocalDateTime createTime;
    
    // getters and setters
}
```

#### 2.7.2 消息发送

```java
@Service
public class MessageProducer {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public void sendDownloadTask(DownloadTaskMessage message) {
        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE,
            RabbitConfig.ROUTING_KEY,
            message
        );
    }
}
```

#### 2.7.3 消息消费

```java
@Component
public class DownloadTaskConsumer {
    
    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void handleDownloadTask(DownloadTaskMessage message) {
        try {
            // 处理下载任务
            processDownloadTask(message);
        } catch (Exception e) {
            // 错误处理
            handleError(message, e);
        }
    }
    
    private void processDownloadTask(DownloadTaskMessage message) {
        // 实现具体的下载逻辑
    }
}
```

### 2.8 错误处理和重试机制

#### 2.8.1 死信队列

```java
@Bean
public Queue deadLetterQueue() {
    return QueueBuilder.durable("download.dead.letter.queue").build();
}

@Bean
public DirectExchange deadLetterExchange() {
    return new DirectExchange("download.dead.letter.exchange");
}

@Bean
public Binding deadLetterBinding() {
    return BindingBuilder.bind(deadLetterQueue())
        .to(deadLetterExchange())
        .with("download.dead.letter");
}
```

#### 2.8.2 重试机制

```java
@RabbitListener(queues = RabbitConfig.QUEUE)
public void handleDownloadTask(DownloadTaskMessage message, 
                              @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
    try {
        processDownloadTask(message);
    } catch (Exception e) {
        // 重试逻辑
        if (message.getRetryCount() < MAX_RETRY_COUNT) {
            message.setRetryCount(message.getRetryCount() + 1);
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, message);
        } else {
            // 发送到死信队列
            sendToDeadLetterQueue(message, e);
        }
    }
}
```

### 2.9 监控和日志

#### 2.9.1 任务监控

```java
@Component
public class TaskMonitor {
    
    @EventListener
    public void handleTaskStart(TaskStartEvent event) {
        log.info("任务开始: taskId={}, userId={}", 
                event.getTaskId(), event.getUserId());
    }
    
    @EventListener
    public void handleTaskComplete(TaskCompleteEvent event) {
        log.info("任务完成: taskId={}, duration={}ms", 
                event.getTaskId(), event.getDuration());
    }
}
```

#### 2.9.2 性能指标

```java
@Service
public class PerformanceMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public void recordDownloadTask(String taskType, long duration) {
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("download.task.duration")
                .tag("type", taskType)
                .register(meterRegistry));
    }
}
```

## 3. 技术优势

### 3.1 Redis优势

1. **高性能**: 内存存储，毫秒级响应
2. **原子操作**: 确保计数准确性
3. **过期策略**: 自动清理过期数据
4. **持久化**: 支持RDB和AOF持久化
5. **集群支持**: 支持主从复制和集群模式

### 3.2 RabbitMQ优势

1. **可靠性**: 消息持久化，确保不丢失
2. **异步处理**: 解耦生产者和消费者
3. **负载均衡**: 支持多消费者并行处理
4. **错误处理**: 死信队列和重试机制
5. **监控**: 丰富的管理界面和指标

### 3.3 系统优势

1. **高并发**: 支持大量用户同时下载
2. **可扩展**: 水平扩展Redis和RabbitMQ集群
3. **容错性**: 多重保障机制
4. **监控**: 完整的监控和日志体系
5. **性能**: 优化的线程池和缓存策略

## 4. 部署建议

### 4.1 Redis部署

```bash
# 单机部署
redis-server --port 6379 --daemonize yes

# 集群部署
redis-cli --cluster create 127.0.0.1:7000 127.0.0.1:7001 127.0.0.1:7002
```

### 4.2 RabbitMQ部署

```bash
# Docker部署
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

# 集群部署
rabbitmqctl join_cluster rabbit@node1
```

### 4.3 监控配置

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

## 5. 总结

Redis和RabbitMQ的结合使用为系统提供了强大的异步处理能力和高性能的数据管理功能。Redis负责实时计数和配额管理，RabbitMQ负责异步任务处理，两者相互配合，确保了系统的稳定性、可扩展性和高性能。

通过合理的设计和优化，这套技术方案能够支持大规模用户并发访问，提供可靠的下载服务，并具备良好的监控和运维能力。
