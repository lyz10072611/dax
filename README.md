# 项目名称：Pollute-DW

## 项目简介
Pollute-DW 是一个基于 Spring Boot 和 Vue.js 的全栈项目，主要用于管理污染物数据。项目支持用户权限控制、文件上传与下载、异步任务处理以及性能优化等功能。

---

## 功能概述

### 1. 用户权限控制
- **角色划分**：
  - 管理员（`role_code=0`）：拥有所有权限。
  - 普通用户（`role_code=1`）：每日下载受限。
  - 游客（`role_code=2`）：仅能查看数据。
- **权限管理**：
  - 数据管理（增删改查）。
  - 用户管理（增删改查）。
  - 下载配额设置。
- **实现方式**：
  - 使用 `ThreadLocal` 存储当前用户信息。
  - 数据库中通过 `role` 和 `permission` 表定义角色和权限。
  - 通过拦截器和注解实现权限校验。

### 2. 文件管理
- **功能**：
  - 文件上传、下载、删除。
  - 支持单文件和多文件打包下载。
- **权限要求**：
  - 文件上传和删除仅限管理员。
  - 文件下载对普通用户有每日配额限制。
- **实现方式**：
  - 使用 Spring Boot 的 `MultipartFile` 处理文件上传。
  - 文件存储路径通过 `application.yml` 配置。
  - Redis 用于存储每日下载配额。

### 3. 数据下载
- **同步下载**：
  - 支持单文件和多文件打包为 ZIP 下载。
  - 普通用户每日下载配额为 500。
- **异步下载**：
  - 用户提交下载任务后，任务进入 RabbitMQ 队列。
  - 后台处理完成后，用户可通过任务 ID 下载结果。
- **实现方式**：
  - 使用 Redis 脚本实现配额扣减和总下载量累加。
  - RabbitMQ 用于异步任务队列。
  - 下载结果以 Base64 编码存储在 Redis 中。

### 4. 污染物数据表（`pollution_data`）
| 字段名             | 类型             | 描述                       |
|--------------------|------------------|----------------------------|
| id                 | BIGINT           | 主键                       |
| pollutant_type     | ENUM('NO2','PM25','PM10','O3') | 污染物类型            |
| data_format        | TINYINT          | 数据格式（1=原始TIF，2=日TIF，3=小时TIF，4=日PNG，5=小时PNG）|
| produce_time       | DATETIME         | 数据生产时间               |
| upload_time        | DATETIME         | 数据上传时间               |
| avg_concentration  | DECIMAL(12,7)    | 平均浓度                   |
| max_concentration  | DECIMAL(12,7)    | 最大浓度                   |
| warning_location   | VARCHAR(100)     | 警告位置（经纬度信息，可为空）|
| file_path          | VARCHAR(255)     | 文件路径                   |

---

### 下载细节说明

#### 同步下载
- **单文件下载**：
  - 用户选择单个文件时，直接返回文件的二进制流。
  - 响应头中包含 `Content-Disposition`，用于指定下载文件名。
- **多文件打包下载**：
  - 用户选择多个文件时，后端将文件打包为 ZIP 格式后返回。
  - 使用 `ZipOutputStream` 实现文件打包。

#### 异步下载
- **任务提交**：
  - 用户通过 `/pollution/async/enqueue` 接口提交下载任务。
  - 后端将任务信息存入 Redis，并发送到 RabbitMQ 队列。
- **任务状态查询**：
  - 用户通过任务 ID 查询任务状态（`queued`、`processing`、`done`、`error`）。
  - 状态存储在 Redis 中，任务完成后可下载结果。
- **结果下载**：
  - 任务完成后，用户通过 `/pollution/async/result/{taskId}` 接口下载结果。
  - 结果以 Base64 编码存储在 Redis 中，下载时解码并返回。

#### 下载配额限制
- **普通用户**：
  - 每日下载配额为 500，超出配额后将返回 HTTP 429 状态码。
  - 使用 Redis 脚本实现配额扣减和总下载量累加。
- **管理员**：
  - 不受下载配额限制，可无限制下载。

#### 错误处理
- 文件不存在：返回 HTTP 404 状态码。
- 配额不足：返回 HTTP 429 状态码，并在响应头中附加 `X-Reason: Daily quota exceeded`。
- 异步任务失败：返回任务状态为 `error`，用户可重新提交任务。

---

## 技术栈
- **后端**：
  - Spring Boot
  - MyBatis
  - Redis
  - RabbitMQ
- **前端**：
  - Vue.js
  - Pinia（状态管理）
  - Axios（HTTP 请求）
- **数据库**：
  - MySQL

---

## 项目结构
```
pollute-dw/
├── src/
│   ├── main/
│   │   ├── java/com/lyz/
│   │   │   ├── controller/       # 控制器层
│   │   │   ├── service/          # 服务层
│   │   │   ├── mapper/           # 数据库操作
│   │   │   ├── pojo/             # 数据模型
│   │   │   ├── config/           # 配置类
│   │   ├── resources/
│   │       ├── application.yml   # 配置文件
│   ├── test/                     # 测试代码
├── static/myVue/                 # 前端代码
├── pom.xml                       # Maven 配置文件
```

---

## 安装与运行

### 1. 环境要求
- JDK 11+
- Node.js 16+
- MySQL 8+
- Redis
- RabbitMQ

### 2. 后端运行
```bash
# 进入项目根目录
cd pollute-dw

# 启动后端服务
mvn spring-boot:run
```

### 3. 前端运行
```bash
# 进入前端目录
cd static/myVue

# 安装依赖
npm install

# 启动前端服务
npm run dev
```

---

## API 文档
项目集成了 Swagger，访问以下地址查看 API 文档：
```
http://localhost:8083/swagger-ui.html
```

---

## 数据库设计
### 1. 角色表（`role`）
| 字段名       | 类型       | 描述           |
|--------------|------------|----------------|
| id           | INT        | 主键           |
| role_code    | INT        | 角色代码       |
| role_name    | VARCHAR(20)| 角色名称       |
| description  | VARCHAR(100)| 描述          |

### 2. 用户表（`users`）
| 字段名         | 类型         | 描述           |
|----------------|--------------|----------------|
| id             | INT          | 主键           |
| username       | VARCHAR(20)  | 用户名         |
| password       | VARCHAR(64)  | 密码（MD5 加密）|
| role_id        | INT          | 角色 ID        |
| daily_download | INT          | 每日下载配额   |
| sum_download   | INT          | 总下载量       |

### 3. 权限表（`permission`）
| 字段名       | 类型         | 描述           |
|--------------|--------------|----------------|
| id           | INT          | 主键           |
| role_id      | INT          | 角色 ID        |
| perm_code    | VARCHAR(50)  | 权限标识       |
| perm_name    | VARCHAR(50)  | 权限名称       |
| description  | VARCHAR(100) | 描述           |

---

## 常见问题
1. **如何修改每日下载配额？**
   - 管理员可通过 `/admin/users/{id}/quota` 接口设置用户的每日下载配额。

2. **异步下载任务失败怎么办？**
   - 检查 RabbitMQ 和 Redis 服务是否正常运行。

3. **如何新增管理员？**
   - 在数据库 `users` 表中插入一条 `role_id=0` 的记录。

---

## 贡献
欢迎提交 Issue 或 Pull Request 来改进本项目。

---

## 许可证
本项目采用 MIT 许可证。

---

## Redis 性能优化

#### 缓存雪崩
- **问题**：大量缓存同时过期，导致请求直接打到数据库，可能引发数据库崩溃。
- **解决方案**：
  - 设置随机过期时间，避免大量缓存同时过期。
  - 示例代码：
    ```java
    public void setDataWithRandomExpire(String key, String value) {
        int baseExpire = 3600; // 基础过期时间，单位：秒
        int randomExpire = new Random().nextInt(600); // 随机时间，单位：秒
        redisTemplate.opsForValue().set(key, value, baseExpire + randomExpire, TimeUnit.SECONDS);
    }
    ```

#### 缓存击穿
- **问题**：热点数据在缓存过期后，大量请求直接打到数据库。
- **解决方案**：
  - 使用分布式锁控制缓存重建。
  - 示例代码：
    ```java
    public String getDataWithLock(String key) {
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            String lockKey = "lock:" + key;
            boolean isLock = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
            if (isLock) {
                try {
                    value = loadDataFromDB(key);
                    redisTemplate.opsForValue().set(key, value, 60, TimeUnit.SECONDS);
                } finally {
                    redisTemplate.delete(lockKey);
                }
            } else {
                Thread.sleep(100);
                return getDataWithLock(key);
            }
        }
        return value;
    }
    ```

---

## 数据下载性能优化

#### 异步下载
- **问题**：大文件下载可能导致阻塞，影响服务器性能。
- **优化方案**：
  - 使用 RabbitMQ 实现异步任务队列，后台处理下载任务。
  - 下载结果以 Base64 编码存储在 Redis 中，用户通过任务 ID 获取结果。

#### 多文件打包下载
- **问题**：多文件打包可能占用大量内存。
- **优化方案**：
  - 使用 `ZipOutputStream` 流式处理文件，避免一次性加载所有文件。
  - 示例代码：
    ```java
    try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
        for (File file : files) {
            zos.putNextEntry(new ZipEntry(file.getName()));
            Files.copy(file.toPath(), zos);
            zos.closeEntry();
        }
    }
    ```

#### 缓存下载结果
- **问题**：重复下载相同数据会增加服务器负担。
- **优化方案**：
  - 使用 Redis 缓存下载结果，设置合理的过期时间。

---

## 数据上传性能优化

#### 异步上传
- **问题**：大文件上传可能阻塞主线程，影响并发性能。
- **优化方案**：
  - 使用 `@Async` 注解实现异步上传。
  - 示例代码：
    ```java
    @Async
    public CompletableFuture<Void> asyncUploadData(PollutionData data) {
        return CompletableFuture.runAsync(() -> {
            mapper.add(data);
        });
    }
    ```

#### 分批写入
- **问题**：一次性写入大量数据可能导致数据库压力过大。
- **优化方案**：
  - 将数据分批写入数据库，每批次控制在 1000 条以内。
  - 示例代码：
    ```java
    public void batchInsert(List<PollutionData> dataList) {
        int batchSize = 1000;
        for (int i = 0; i < dataList.size(); i += batchSize) {
            List<PollutionData> batch = dataList.subList(i, Math.min(i + batchSize, dataList.size()));
            mapper.batchInsert(batch);
        }
    }
    ```

#### 数据校验
- **问题**：上传数据格式错误可能导致数据库异常。
- **优化方案**：
  - 在上传前进行数据校验，确保数据格式和约束条件正确。
  - 使用 `@Validated` 注解和自定义校验器。