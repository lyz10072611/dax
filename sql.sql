-- ===============================
-- 1. 角色表
-- ===============================
DROP TABLE IF EXISTS role;
CREATE TABLE role (
                      id INT PRIMARY KEY AUTO_INCREMENT,
                      role_code INT NOT NULL UNIQUE,    -- 0=管理员,1=用户,2=游客
                      role_name VARCHAR(20) NOT NULL,   -- 角色名称
                      description VARCHAR(100)
);

-- 插入三种角色
INSERT INTO role (role_code, role_name, description) VALUES
                                                         (0, 'admin', '管理员，具备所有功能'),
                                                         (1, 'user', '普通用户，每日有限下载'),
                                                         (2, 'guest', '游客，只能查看');

-- ===============================
-- 2. 用户表
-- ===============================
DROP TABLE IF EXISTS users;
CREATE TABLE users (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(20) NOT NULL UNIQUE,     -- 用户名
                       password VARCHAR(64) NOT NULL,            -- 密码（建议加密存储）
                       nickname VARCHAR(20),                     -- 昵称
                       email VARCHAR(128),                       -- 邮箱
                       user_pic VARCHAR(128),                    -- 头像
                       role_id INT NOT NULL,                     -- 外键：角色id
                       daily_download INT NOT NULL DEFAULT 500,  -- 每日下载数量，默认500
                       sum_download INT NOT NULL DEFAULT 0,      -- 总下载数量，默认0
                       create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                       update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       FOREIGN KEY (role_id) REFERENCES role(id)
);

-- ===============================
-- 3. 权限表
-- ===============================
DROP TABLE IF EXISTS permission;
CREATE TABLE permission (
                            id INT PRIMARY KEY AUTO_INCREMENT,
                            role_id INT NOT NULL,                -- 外键：对应角色
                            perm_code VARCHAR(50) NOT NULL,      -- 权限标识
                            perm_name VARCHAR(50) NOT NULL,      -- 权限名称
                            description VARCHAR(100),
                            FOREIGN KEY (role_id) REFERENCES role(id)
);

-- 污染物数据
DROP TABLE IF EXISTS pollution_data;

CREATE TABLE pollution_data (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                pollutant_type ENUM('NO2','PM25','PM10','O3') NOT NULL,
                                data_format TINYINT NOT NULL,             -- 1=original_tif, 2=day_tif, 3=hour_tif, 4=day_png, 5=hour_png
                                produce_time DATETIME NOT NULL,           -- 数据生产时间
                                upload_time DATETIME NOT NULL,            -- 上传时间
                                avg_concentration DECIMAL(12,7) NOT NULL, -- 平均浓度（保留 7 位小数）
                                max_concentration DECIMAL(12,7) NOT NULL, -- 最高浓度（保留 7 位小数）
                                warning_location VARCHAR(100) DEFAULT NULL, -- 经纬度信息，可为空
                                file_path VARCHAR(255) NOT NULL           -- 文件路径
);

-- 插入管理员用户
INSERT INTO users (username, password, nickname, email, user_pic, role_id, daily_download, sum_download)
VALUES ('admin01', MD5('admin123'), '系统管理员', 'admin@example.com', NULL,
        (SELECT id FROM role WHERE role_code = 0), 500, 0);

-- 插入普通用户
INSERT INTO users (username, password, nickname, email, user_pic, role_id, daily_download, sum_download)
VALUES ('user01', MD5('user123'), '普通用户', 'user01@example.com', NULL,
        (SELECT id FROM role WHERE role_code = 1), 500, 0);

-- 插入游客用户
INSERT INTO users (username, password, nickname, email, user_pic, role_id, daily_download, sum_download)
VALUES ('guest01', MD5('guest123'), '游客', 'guest01@example.com', NULL,
        (SELECT id FROM role WHERE role_code = 2), 500, 0);


-- 管理员权限
INSERT INTO permission (role_id, perm_code, perm_name, description) VALUES
                                                                        ((SELECT id FROM role WHERE role_code=0), 'data_crud', '数据管理', '对数据的增删改查'),
                                                                        ((SELECT id FROM role WHERE role_code=0), 'user_crud', '用户管理', '对用户的增删改查'),
                                                                        ((SELECT id FROM role WHERE role_code=0), 'set_download', '设置下载数量', '设置用户的每日下载数量');

-- 普通用户权限
INSERT INTO permission (role_id, perm_code, perm_name, description) VALUES
                                                                        ((SELECT id FROM role WHERE role_code=1), 'download', '下载', '每日有限制下载'),
                                                                        ((SELECT id FROM role WHERE role_code=1), 'view', '查看', '查看数据');

-- 游客权限
INSERT INTO permission (role_id, perm_code, perm_name, description) VALUES
    ((SELECT id FROM role WHERE role_code=2), 'view', '查看', '只能查看数据');


--
INSERT INTO pollution_data
(pollutant_type, data_format, produce_time, upload_time, avg_concentration, max_concentration, warning_location, file_path) VALUES
                                                                                                                                ('NO2', 1, '2025-09-25 08:00:00', NOW(), 35.1234567, 78.7654321, '116.40,39.90', '/data/NO2/original_tif/no2_20250925_0800.tif'),
                                                                                                                                ('NO2', 2, '2025-09-25 09:00:00', NOW(), 30.9876543, 70.1234567, NULL, '/data/NO2/day_tif/no2_20250925_0900.tif'),
                                                                                                                                ('NO2', 3, '2025-09-25 10:00:00', NOW(), 42.5432109, 88.1111111, '121.47,31.23', '/data/NO2/hour_tif/no2_20250925_1000.tif'),
                                                                                                                                ('NO2', 4, '2025-09-25 11:00:00', NOW(), 28.2222222, 65.3333333, NULL, '/data/NO2/day_png/no2_20250925_1100.png'),
                                                                                                                                ('NO2', 5, '2025-09-25 12:00:00', NOW(), 39.8765432, 92.4567890, '113.26,23.13', '/data/NO2/hour_png/no2_20250925_1200.png'),

                                                                                                                                ('PM25', 1, '2025-09-25 08:10:00', NOW(), 55.1111111, 110.2222222, '118.78,32.04', '/data/PM25/original_tif/pm25_20250925_0810.tif'),
                                                                                                                                ('PM25', 2, '2025-09-25 09:10:00', NOW(), 60.3333333, 120.4444444, NULL, '/data/PM25/day_tif/pm25_20250925_0910.tif'),
                                                                                                                                ('PM25', 3, '2025-09-25 10:10:00', NOW(), 72.5555555, 135.6666666, '114.05,22.55', '/data/PM25/hour_tif/pm25_20250925_1010.tif'),
                                                                                                                                ('PM25', 4, '2025-09-25 11:10:00', NOW(), 49.7777777, 95.8888888, NULL, '/data/PM25/day_png/pm25_20250925_1110.png'),
                                                                                                                                ('PM25', 5, '2025-09-25 12:10:00', NOW(), 68.9999999, 140.1111111, '104.06,30.67', '/data/PM25/hour_png/pm25_20250925_1210.png'),

                                                                                                                                ('PM10', 1, '2025-09-25 08:20:00', NOW(), 80.2222222, 160.3333333, '106.55,29.56', '/data/PM10/original_tif/pm10_20250925_0820.tif'),
                                                                                                                                ('PM10', 2, '2025-09-25 09:20:00', NOW(), 90.4444444, 175.5555555, NULL, '/data/PM10/day_tif/pm10_20250925_0920.tif'),
                                                                                                                                ('PM10', 3, '2025-09-25 10:20:00', NOW(), 85.6666666, 170.7777777, '108.95,34.27', '/data/PM10/hour_tif/pm10_20250925_1020.tif'),
                                                                                                                                ('PM10', 4, '2025-09-25 11:20:00', NOW(), 78.8888888, 155.9999999, NULL, '/data/PM10/day_png/pm10_20250925_1120.png'),
                                                                                                                                ('PM10', 5, '2025-09-25 12:20:00', NOW(), 92.1111111, 180.2222222, '117.20,39.13', '/data/PM10/hour_png/pm10_20250925_1220.png'),

                                                                                                                                ('O3', 1, '2025-09-25 08:30:00', NOW(), 20.1234567, 65.2345678, '120.15,30.28', '/data/O3/original_tif/o3_20250925_0830.tif'),
                                                                                                                                ('O3', 2, '2025-09-25 09:30:00', NOW(), 25.3456789, 72.4567890, NULL, '/data/O3/day_tif/o3_20250925_0930.tif'),
                                                                                                                                ('O3', 3, '2025-09-25 10:30:00', NOW(), 22.5678901, 68.6789012, '118.10,24.46', '/data/O3/hour_tif/o3_20250925_1030.tif'),
                                                                                                                                ('O3', 4, '2025-09-25 11:30:00', NOW(), 30.7890123, 80.8901234, NULL, '/data/O3/day_png/o3_20250925_1130.png'),
                                                                                                                                ('O3', 5, '2025-09-25 12:30:00', NOW(), 28.9012345, 75.0123456, '112.93,28.23', '/data/O3/hour_png/o3_20250925_1230.png');
