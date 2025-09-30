# 水泥厂管理系统

## 系统概述

本系统已从污染数据管理系统重构为水泥厂管理系统，主要功能包括：

1. **水泥厂基本信息管理** - 管理水泥厂的基本信息，包括位置、状态等
2. **水泥厂识别记录管理** - 管理遥感识别水泥厂的记录
3. **用户管理系统** - 用户注册、登录、权限管理
4. **数据统计** - 提供各种统计信息

## 主要变更

### 1. 数据库结构变更

- 新增 `cement_plants` 表：存储水泥厂基本信息
- 新增 `cement_plant_identifications` 表：存储水泥厂识别记录
- 更新 `users` 表结构：适配新的用户管理需求
- 保留 `roles` 和 `user_roles` 表：用于权限管理

### 2. 后端系统重构

#### 新增组件
- `CementPlant` 实体类：水泥厂基本信息
- `CementPlantIdentification` 实体类：水泥厂识别记录
- `CementPlantMapper`：水泥厂数据访问层
- `CementPlantService` 和 `CementPlantServiceImpl`：水泥厂业务逻辑
- `CementPlantController`：水泥厂API控制器
- `ScheduledTasks`：定时任务配置

#### 修复的问题
- **拦截器bug修复**：修复了 `LoginInterceptor` 中token鉴权失败时只返回状态码而不返回JSON错误信息的问题
- **用户系统优化**：更新了用户注册登录系统，增加了最后登录时间记录
- **定时任务**：增加了每日下载计数重置的定时任务

#### 删除的冗余组件
- 删除了所有污染数据相关的实体类、服务、控制器
- 删除了分类管理相关组件
- 删除了流程管理相关组件
- 删除了文件上传相关的Worker组件

### 3. API接口

#### 水泥厂管理接口
- `GET /cement-plant/{plantId}` - 根据ID查询水泥厂
- `GET /cement-plant/list` - 查询水泥厂列表
- `GET /cement-plant/page` - 分页查询水泥厂
- `POST /cement-plant` - 新增水泥厂
- `PUT /cement-plant` - 更新水泥厂
- `DELETE /cement-plant/{plantId}` - 删除水泥厂
- `GET /cement-plant/location-range` - 按地理位置范围查询

#### 水泥厂识别记录接口
- `GET /cement-plant/identification/{identificationId}` - 根据ID查询识别记录
- `GET /cement-plant/identification/list` - 查询识别记录列表
- `GET /cement-plant/identification/page` - 分页查询识别记录
- `POST /cement-plant/identification` - 新增识别记录
- `PUT /cement-plant/identification` - 更新识别记录
- `DELETE /cement-plant/identification/{identificationId}` - 删除识别记录

#### 统计接口
- `GET /cement-plant/stats/active-count` - 获取活跃水泥厂数量
- `GET /cement-plant/stats/identification-count/{plantId}` - 获取水泥厂识别记录数量
- `GET /cement-plant/stats/identification-count-by-time` - 按时间范围获取识别记录数量

#### 用户管理接口（保持不变）
- `POST /user/register` - 用户注册
- `POST /user/login` - 用户登录
- `GET /user/userInfo` - 获取用户信息
- `PUT /user/update` - 更新用户信息
- `PATCH /user/updateAvatar` - 更新头像
- `PATCH /user/updatePwd` - 修改密码

## 技术特性

1. **JWT Token认证**：使用JWT进行用户认证，token存储在Redis中
2. **拦截器优化**：修复了token鉴权bug，现在会正确返回JSON格式的错误信息
3. **定时任务**：每日凌晨2点自动重置用户下载计数
4. **数据验证**：使用Jakarta Validation进行数据验证
5. **分页查询**：使用PageHelper实现分页功能
6. **API文档**：集成Swagger/OpenAPI文档

## 部署说明

1. 确保数据库已创建相关表结构（参考 `src/cement.sql`）
2. 配置Redis连接
3. 启动Spring Boot应用
4. 访问 `http://localhost:8080/swagger-ui.html` 查看API文档

## 注意事项

- 系统已完全移除污染数据相关功能
- 用户系统已适配新的数据库结构
- 拦截器现在会正确处理未授权访问的情况
- 定时任务会自动处理每日下载计数重置

