-- 测试数据库初始化脚本
-- 创建测试用的数据库表结构

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    nickname VARCHAR(50),
    user_pic VARCHAR(255),
    role_id INT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_time TIMESTAMP NULL,
    status INT DEFAULT 1,
    download_count INT DEFAULT 0
);

-- 创建角色表
CREATE TABLE IF NOT EXISTS roles (
    role_id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    role_code INT NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- 插入默认角色
INSERT INTO roles (role_name, role_code, description) VALUES 
('admin', 1, '管理员角色'),
('user', 2, '普通用户角色')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

-- 创建水泥厂表
CREATE TABLE IF NOT EXISTS cement_plants (
    plant_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plant_name VARCHAR(100) NOT NULL UNIQUE,
    longitude DECIMAL(10, 7),
    latitude DECIMAL(10, 7),
    province VARCHAR(50),
    city VARCHAR(50),
    district VARCHAR(50),
    status VARCHAR(20) DEFAULT 'active',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建水泥厂识别记录表
CREATE TABLE IF NOT EXISTS cement_plant_identifications (
    identification_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plant_id BIGINT NOT NULL,
    longitude DECIMAL(10, 7),
    latitude DECIMAL(10, 7),
    identification_time TIMESTAMP,
    data_source VARCHAR(50),
    image_uuid VARCHAR(100),
    ndvi_index DECIMAL(5, 3),
    province VARCHAR(50),
    city VARCHAR(50),
    district VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (plant_id) REFERENCES cement_plants(plant_id) ON DELETE CASCADE
);

-- 创建地理数据文件表
CREATE TABLE IF NOT EXISTS geospatial_files (
    file_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(20),
    file_size BIGINT,
    status VARCHAR(20) DEFAULT 'pending',
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建水泥厂地理数据关联表
CREATE TABLE IF NOT EXISTS cement_plant_geodata (
    relation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plant_id BIGINT NOT NULL,
    file_id BIGINT NOT NULL,
    identification_id BIGINT,
    data_type VARCHAR(50),
    processing_status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (plant_id) REFERENCES cement_plants(plant_id) ON DELETE CASCADE,
    FOREIGN KEY (file_id) REFERENCES geospatial_files(file_id) ON DELETE CASCADE,
    FOREIGN KEY (identification_id) REFERENCES cement_plant_identifications(identification_id) ON DELETE SET NULL
);

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);
CREATE INDEX IF NOT EXISTS idx_cement_plants_name ON cement_plants(plant_name);
CREATE INDEX IF NOT EXISTS idx_cement_plants_location ON cement_plants(longitude, latitude);
CREATE INDEX IF NOT EXISTS idx_cement_plants_status ON cement_plants(status);
CREATE INDEX IF NOT EXISTS idx_identifications_plant_id ON cement_plant_identifications(plant_id);
CREATE INDEX IF NOT EXISTS idx_identifications_time ON cement_plant_identifications(identification_time);
CREATE INDEX IF NOT EXISTS idx_identifications_location ON cement_plant_identifications(longitude, latitude);
CREATE INDEX IF NOT EXISTS idx_geodata_plant_id ON cement_plant_geodata(plant_id);
CREATE INDEX IF NOT EXISTS idx_geodata_file_id ON cement_plant_geodata(file_id);

-- 插入测试数据
-- 插入测试用户
INSERT INTO users (username, password_hash, nickname, email, role_id, status) VALUES 
('testadmin', '5d41402abc4b2a76b9719d911017c592', '测试管理员', 'admin@test.com', 1, 1),
('testuser', '5d41402abc4b2a76b9719d911017c592', '测试用户', 'user@test.com', 2, 1)
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- 插入测试水泥厂数据
INSERT INTO cement_plants (plant_name, longitude, latitude, province, city, district, status) VALUES 
('测试水泥厂1', 118.1234567, 32.1234567, '江苏省', '南京市', '鼓楼区', 'active'),
('测试水泥厂2', 119.1234567, 33.1234567, '浙江省', '杭州市', '西湖区', 'active'),
('测试水泥厂3', 120.1234567, 34.1234567, '山东省', '济南市', '历下区', 'inactive')
ON DUPLICATE KEY UPDATE plant_name = VALUES(plant_name);

-- 插入测试识别记录
INSERT INTO cement_plant_identifications (plant_id, longitude, latitude, identification_time, data_source, image_uuid, ndvi_index, province, city, district) VALUES 
(1, 118.1234567, 32.1234567, '2023-01-01 10:00:00', 'GF-2', 'uuid-001', 0.5, '江苏省', '南京市', '鼓楼区'),
(1, 118.1234567, 32.1234567, '2023-01-02 10:00:00', 'Landsat-8', 'uuid-002', 0.6, '江苏省', '南京市', '鼓楼区'),
(2, 119.1234567, 33.1234567, '2023-01-03 10:00:00', 'GF-2', 'uuid-003', 0.7, '浙江省', '杭州市', '西湖区')
ON DUPLICATE KEY UPDATE image_uuid = VALUES(image_uuid);
