# 水泥厂识别Webgis管理系统

## 项目概述

本项目是一个基于Spring Boot 3和Vue 3的水泥厂管理系统，主要用于管理水泥厂的基本信息、识别记录、地理数据处理和文件管理。系统采用前后端分离架构，提供完整的RESTful API和现代化的Web界面。

## 系统架构

### 整体架构
- **后端**: Spring Boot 3 + Java 21
- **前端**: Vue 3 + Vite + Pinia
- **数据库**: PostgreSQL
- **缓存**: Redis
- **消息队列**: RabbitMQ
- **API文档**: Swagger/OpenAPI 3

### 技术栈详情

#### 后端技术栈
| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.1.3 | 主框架 |
| Java | 21 | 开发语言 |
| MyBatis | 3.0.0 | ORM框架 |
| PostgreSQL | - | 主数据库 |
| Redis | - | 缓存和会话存储 |
| RabbitMQ | - | 消息队列 |
| JWT | 4.4.0 | 身份认证 |
| Lombok | 1.18.30 | 代码简化 |
| SpringDoc OpenAPI | 2.5.0 | API文档 |
| PageHelper | 1.4.6 | 分页插件 |

#### 前端技术栈
| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5.13 | 前端框架 |
| Vite | 6.3.6 | 构建工具 |
| Pinia | 2.2.6 | 状态管理 |
| Vue Router | 4.5.0 | 路由管理 |
| Axios | 1.7.7 | HTTP客户端 |
| Playwright | 1.51.0 | E2E测试 |

## 核心功能模块

### 1. 用户管理模块

#### 功能描述
提供用户注册、登录、权限管理、密码修改等基础功能，支持JWT Token认证和Redis会话管理。

#### 主要接口
- `POST /user/register` - 用户注册
- `POST /user/login` - 用户登录
- `GET /user/userInfo` - 获取用户信息
- `PUT /user/update` - 更新用户信息
- `PATCH /user/updatePwd` - 修改密码
- `POST /user/logout` - 用户登出
- `GET /user/permissions` - 获取用户权限

#### 技术实现
- **认证方式**: JWT Token + Redis存储
- **密码加密**: MD5加密
- **权限控制**: 基于角色的访问控制(RBAC)
- **登录限制**: Redis实现登录失败次数限制
- **会话管理**: Redis存储用户会话信息

#### 用户角色
- **管理员** (roleCode: 0): 拥有所有权限，可上传数据
- **普通用户** (roleCode: 1): 基础权限，可查看和下载数据

### 2. 水泥厂管理模块

#### 功能描述
管理水泥厂的基本信息，包括地理位置、状态、识别记录等，支持CRUD操作和多种查询方式。

#### 主要接口
- `GET /cement-plant/{plantId}` - 根据ID查询水泥厂
- `GET /cement-plant/list` - 查询水泥厂列表
- `GET /cement-plant/page` - 分页查询水泥厂
- `POST /cement-plant` - 新增水泥厂
- `PUT /cement-plant` - 更新水泥厂
- `DELETE /cement-plant/{plantId}` - 删除水泥厂
- `GET /cement-plant/location-range` - 按地理位置范围查询

#### 数据模型
```java
public class CementPlant {
    private Long plantId;           // 水泥厂ID
    private String plantName;       // 水泥厂名称
    private BigDecimal longitude;   // 经度
    private BigDecimal latitude;    // 纬度
    private String province;        // 省份
    private String city;           // 城市
    private String district;       // 区县
    private String status;         // 状态(active/inactive/under_construction)
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

#### 技术实现
- **数据验证**: Jakarta Validation注解验证
- **分页查询**: PageHelper插件
- **地理位置**: 支持经纬度范围查询
- **状态管理**: 支持多种水泥厂状态

### 3. 水泥厂识别记录管理

#### 功能描述
管理水泥厂的识别记录，包括NDVI指数、数据源、识别时间等信息。

#### 主要接口
- `GET /cement-plant/identification/{identificationId}` - 根据ID查询识别记录
- `GET /cement-plant/identification/list` - 查询识别记录列表
- `GET /cement-plant/identification/page` - 分页查询识别记录
- `POST /cement-plant/identification` - 新增识别记录
- `PUT /cement-plant/identification` - 更新识别记录
- `DELETE /cement-plant/identification/{identificationId}` - 删除识别记录

#### 数据模型
```java
public class CementPlantIdentification {
    private Long identificationId;     // 识别记录ID
    private Long plantId;             // 关联水泥厂ID
    private BigDecimal longitude;     // 经度
    private BigDecimal latitude;      // 纬度
    private LocalDateTime identificationTime; // 识别时间
    private String dataSource;         // 数据源
    private String imageUuid;         // 图像UUID
    private Float ndviIndex;         // NDVI指数
    private String province;          // 省份
    private String city;             // 城市
    private String district;         // 区县
    private LocalDateTime createdAt;  // 创建时间
}
```

### 4. 数据查询模块

#### 功能描述
提供多种查询方式，支持按名称、地区、地理位置、时间范围等条件查询水泥厂和识别记录。

#### 主要接口
- `GET /data-query/cement-plants/search` - 水泥厂模糊搜索
- `GET /data-query/cement-plants/by-region` - 按行政区划查询
- `GET /data-query/cement-plants/by-location` - 按地理位置查询
- `GET /data-query/cement-plants/by-status` - 按状态查询
- `GET /data-query/identifications/by-plant` - 查询水泥厂的识别记录
- `GET /data-query/identifications/latest` - 获取最新识别记录
- `GET /data-query/identifications/by-time-range` - 按时间范围查询
- `GET /data-query/identifications/by-ndvi-range` - 按NDVI范围查询
- `GET /data-query/comprehensive-search` - 综合搜索

#### 技术实现
- **模糊搜索**: SQL LIKE查询
- **地理位置**: 经纬度范围查询
- **时间范围**: 时间区间查询
- **NDVI范围**: 数值范围查询
- **综合查询**: 多条件组合查询

### 5. 数据上传模块

#### 功能描述
提供水泥厂识别数据的上传功能，支持单条和批量上传，仅管理员可操作。

#### 主要接口
- `POST /data-upload/cement-plant-data` - 上传水泥厂识别数据
- `POST /data-upload/batch-cement-plant-data` - 批量上传数据
- `GET /data-upload/upload-status` - 获取上传状态

#### 技术实现
- **权限控制**: 仅管理员可上传
- **数据验证**: JSON格式验证
- **批量处理**: 支持批量上传和错误处理
- **自动判断**: 根据水泥厂名称自动判断是否存在

### 6. 文件管理模块

#### 功能描述
管理上传的文件，支持文件的上传、查询、替换、删除等操作。

#### 主要接口
- `GET /files` - 查询文件列表
- `GET /files/{name}` - 查询单个文件信息
- `POST /files` - 上传文件
- `PUT /files/{name}` - 替换文件
- `DELETE /files/{name}` - 删除文件

#### 技术实现
- **文件存储**: 本地文件系统存储
- **权限控制**: 仅管理员可操作
- **文件类型**: 支持多种文件格式
- **文件信息**: 记录文件元数据

### 7. 数据下载模块

#### 功能描述
提供TIF文件的下载功能，支持单文件和批量下载，包含权限控制和下载配额管理。

#### 主要接口
- `GET /data-download/tif/{fileId}` - 下载TIF文件
- `POST /data-download/batch-download` - 批量下载
- `GET /data-download/download-status` - 获取下载状态
- `GET /data-download/download-history` - 获取下载历史
- `POST /data-download/async-download` - 异步下载
- `GET /data-download/async-download/{taskId}/status` - 查询异步任务状态
- `GET /data-download/async-download/{taskId}/result` - 下载异步任务结果

#### 技术实现
- **权限控制**: 基于用户角色的下载权限
- **配额管理**: 每日下载次数限制
- **异步处理**: RabbitMQ实现异步下载
- **批量下载**: ZIP格式打包下载
- **下载记录**: 记录下载历史

### 8. 地理数据管理模块

#### 功能描述
处理TIF格式的地理数据文件，提供瓦片服务和文件处理功能。

#### 主要接口
- `POST /geo/upload-tif` - 上传TIF文件
- `GET /geo/tile/{fileId}/{z}/{x}/{y}` - 获取瓦片数据
- `GET /geo/tif-info/{fileId}` - 获取TIF文件信息
- `POST /geo/process-tif/{fileId}` - 处理TIF文件
- `GET /geo/files` - 获取文件列表

#### 技术实现
- **TIF处理**: 专业的地理数据处理
- **瓦片服务**: 支持地图瓦片服务
- **文件格式**: 支持TIF格式文件
- **异步处理**: 大文件异步处理
- **缓存机制**: 瓦片数据缓存

### 9. 前端服务管理模块

#### 功能描述
管理前端Vue项目的启动、停止和状态监控。

#### 主要接口
- `GET /frontend/status` - 获取前端服务状态
- `POST /frontend/start` - 启动前端服务
- `POST /frontend/stop` - 停止前端服务
- `GET /frontend/info` - 获取前端项目信息

#### 技术实现
- **服务管理**: 自动启动前端开发服务器
- **状态监控**: 实时监控前端服务状态
- **进程控制**: 启动和停止前端进程
- **配置管理**: 前端服务配置管理

## 数据库设计

### 主要数据表

#### 用户表 (user)
```sql
CREATE TABLE user (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_time TIMESTAMP,
    status INTEGER DEFAULT 1,
    role_code INTEGER DEFAULT 1
);
```

#### 水泥厂表 (cement_plants)
```sql
CREATE TABLE cement_plants (
    plant_id SERIAL PRIMARY KEY,
    plant_name VARCHAR(200) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    province VARCHAR(50),
    city VARCHAR(50),
    district VARCHAR(50),
    status VARCHAR(20) DEFAULT 'active',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 水泥厂识别记录表 (cement_plant_identifications)
```sql
CREATE TABLE cement_plant_identifications (
    identification_id SERIAL PRIMARY KEY,
    plant_id BIGINT REFERENCES cement_plants(plant_id),
    longitude DECIMAL(10, 7),
    latitude DECIMAL(10, 7),
    identification_time TIMESTAMP,
    data_source VARCHAR(100),
    image_uuid VARCHAR(100),
    ndvi_index FLOAT,
    province VARCHAR(50),
    city VARCHAR(50),
    district VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 地理数据文件表 (geospatial_files)
```sql
CREATE TABLE geospatial_files (
    file_id SERIAL PRIMARY KEY,
    file_name VARCHAR(200) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(20) NOT NULL,
    file_size BIGINT,
    status VARCHAR(20) DEFAULT 'processing',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 系统特性

### 1. 安全性
- **JWT认证**: 无状态的身份认证
- **权限控制**: 基于角色的访问控制
- **密码加密**: MD5密码加密
- **登录限制**: 防止暴力破解
- **输入验证**: 全面的数据验证

### 2. 性能优化
- **Redis缓存**: 会话和热点数据缓存
- **分页查询**: 大数据量分页处理
- **异步处理**: 大文件异步处理
- **连接池**: 数据库连接池优化
- **瓦片缓存**: 地理数据瓦片缓存

### 3. 可扩展性
- **微服务架构**: 模块化设计
- **消息队列**: RabbitMQ异步处理
- **RESTful API**: 标准化接口设计
- **前后端分离**: 独立部署和扩展

### 4. 监控和日志
- **API文档**: Swagger自动生成文档
- **异常处理**: 全局异常处理机制
- **日志记录**: 详细的操作日志
- **状态监控**: 服务状态实时监控

## 部署架构

### 开发环境
- **后端**: Spring Boot内嵌Tomcat (端口8085)
- **前端**: Vite开发服务器 (端口5173)
- **数据库**: PostgreSQL (端口5432)
- **缓存**: Redis (端口6379)
- **消息队列**: RabbitMQ (端口5672)

### 生产环境建议
- **应用服务器**: Tomcat/Nginx
- **数据库**: PostgreSQL集群
- **缓存**: Redis集群
- **消息队列**: RabbitMQ集群
- **负载均衡**: Nginx
- **监控**: Prometheus + Grafana

## 总结

本水泥厂管理系统是一个功能完整、技术先进的企业级应用系统，具有以下特点：

1. **技术栈现代化**: 采用最新的Spring Boot 3和Vue 3技术栈
2. **功能模块化**: 清晰的模块划分，便于维护和扩展
3. **安全性高**: 完善的认证授权机制
4. **性能优秀**: 多种性能优化手段
5. **可扩展性强**: 支持水平扩展和功能扩展
6. **用户体验好**: 现代化的前端界面和API设计

系统适用于水泥厂数据管理、地理信息处理、文件管理等场景，是一个典型的企业级Web应用系统。
