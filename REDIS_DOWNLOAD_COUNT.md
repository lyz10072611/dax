# Redis每日下载计数管理说明

## 概述

系统已将每日下载数量管理从数据库改为Redis实现，这样可以提供更好的性能和灵活性。

## Redis Key设计

### 用户每日下载计数
- **Key格式**: `daily_download:{userId}:{yyyy-MM-dd}`
- **示例**: `daily_download:123:2024-01-15`
- **Value**: 下载次数（整数）
- **过期时间**: 自动过期（明天凌晨2点）

### 重置标记
- **Key格式**: `daily_download_reset_date:{yyyy-MM-dd}`
- **示例**: `daily_download_reset_date:2024-01-15`
- **Value**: "1"
- **过期时间**: 明天凌晨2点

## 主要功能

### 1. 获取每日下载计数
```java
Integer count = userService.getDailyDownloadCount(userId);
```

### 2. 增加下载计数
```java
userService.incrementDailyDownloadCount(userId);
```

### 3. 检查下载限制
```java
boolean canDownload = userService.checkDailyDownloadLimit(userId, 10);
```

### 4. 重置每日计数
```java
userService.resetDailyDownloadCount();
```

## API接口

### 获取用户每日下载计数
- **URL**: `GET /user/daily-download-count`
- **说明**: 获取当前用户今日已下载次数
- **返回**: `{"code": 200, "message": "success", "data": 5}`

### 增加下载计数
- **URL**: `POST /user/increment-download`
- **说明**: 用户下载文件时调用，增加下载计数
- **返回**: `{"code": 200, "message": "下载计数已更新", "data": null}`

### 检查下载限制
- **URL**: `GET /user/download-limit-check`
- **说明**: 检查用户是否还可以下载
- **返回**: `{"code": 200, "message": "success", "data": true}`

## 定时任务

系统配置了定时任务，每日凌晨2点自动重置所有用户的下载计数：

```java
@Scheduled(cron = "0 0 2 * * ?")
public void resetDailyDownloadCount() {
    userService.resetDailyDownloadCount();
}
```

## 优势

1. **性能更好**: Redis内存操作比数据库查询更快
2. **自动过期**: 使用Redis的过期机制，无需手动清理
3. **原子操作**: Redis的increment操作是原子的，避免并发问题
4. **灵活性**: 可以轻松调整每日限制，无需修改数据库结构
5. **可扩展**: 可以轻松添加更多统计功能

## 注意事项

1. 确保Redis服务正常运行
2. 系统重启不会影响已设置的过期时间
3. 定时任务会检查是否已重置，避免重复重置
4. 默认每日下载限制为10次，可在代码中调整

