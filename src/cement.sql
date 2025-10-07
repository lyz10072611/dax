-- 启用 PostGIS 扩展（必须）
CREATE EXTENSION IF NOT EXISTS postgis;

-- 1. 用户表 (users)
CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_time TIMESTAMP,
    status SMALLINT NOT NULL DEFAULT 1 -- 0:禁用, 1:启用
);
COMMENT ON TABLE users IS '系统用户表，存储用户基本信息';
COMMENT ON COLUMN users.status IS '账户状态 (0:禁用, 1:启用)';
COMMENT ON COLUMN users.last_login_time IS '最后登录时间';

-- 2. 角色表 (roles)
CREATE TABLE roles (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);
COMMENT ON TABLE roles IS '系统角色表';

-- 3. 用户角色关联表 (user_roles)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    role_id INTEGER NOT NULL REFERENCES roles(role_id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);
COMMENT ON TABLE user_roles IS '用户和角色的多对多关联表';

-- 补充索引
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- 4. 水泥厂表 (cement_plants)
CREATE TABLE cement_plants (
    plant_id BIGSERIAL PRIMARY KEY,
    plant_name VARCHAR(100) NOT NULL,
    longitude DOUBLE PRECISION NOT NULL, -- 经度
    latitude DOUBLE PRECISION NOT NULL,  -- 纬度
    geom geometry(Point, 4326),          -- 空间字段
    province VARCHAR(50),               -- 省
    city VARCHAR(50),                   -- 市
    district VARCHAR(50),               -- 区
    status VARCHAR(20) DEFAULT 'active' -- 状态: active, inactive, under_construction
);
COMMENT ON TABLE cement_plants IS '水泥厂基本信息表';
COMMENT ON COLUMN cement_plants.status IS '工厂状态: active(运营中), inactive(关闭)';

-- 空间索引
CREATE INDEX idx_cement_plants_geom ON cement_plants USING GIST (geom);

-- B-tree索引
CREATE INDEX idx_cement_plants_name ON cement_plants (plant_name);

-- 5. 水泥厂识别记录表 (cement_plant_identifications)
CREATE TABLE cement_plant_identifications (
    identification_id BIGSERIAL PRIMARY KEY,
    plant_id BIGINT NOT NULL REFERENCES cement_plants(plant_id) ON DELETE CASCADE,
    longitude DOUBLE PRECISION NOT NULL, -- 识别经度
    latitude DOUBLE PRECISION NOT NULL,  -- 识别纬度
    geom geometry(Point, 4326),          -- 空间字段
    identification_time TIMESTAMP NOT NULL,
    data_source VARCHAR(50) DEFAULT 'GF-2',
    image_uuid VARCHAR(255),
    ndvi_index FLOAT,
    province VARCHAR(50),
    city VARCHAR(50),
    district VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE cement_plant_identifications IS '水泥厂识别记录表，存储每次遥感识别的结果';

-- 索引
CREATE INDEX idx_identifications_plant_time ON cement_plant_identifications (plant_id, identification_time);
CREATE INDEX idx_identifications_time ON cement_plant_identifications (identification_time);
CREATE INDEX idx_identifications_plant_id ON cement_plant_identifications (plant_id);
CREATE INDEX idx_identifications_geom ON cement_plant_identifications USING GIST (geom);

-- 6. 地理数据文件表 (geospatial_files)
CREATE TABLE geospatial_files (
    file_id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(20) NOT NULL DEFAULT 'TIF',
    file_size BIGINT,
    coordinate_system VARCHAR(50) DEFAULT 'EPSG:4326',
    bounds_west DOUBLE PRECISION,
    bounds_east DOUBLE PRECISION,
    bounds_south DOUBLE PRECISION,
    bounds_north DOUBLE PRECISION,
    bounds_geom geometry(Polygon, 4326), -- 边界多边形
    resolution_x DOUBLE PRECISION,
    resolution_y DOUBLE PRECISION,
    bands_count INTEGER DEFAULT 1,
    data_type VARCHAR(20) DEFAULT 'FLOAT32',
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    upload_user_id BIGINT REFERENCES users(user_id),
    status VARCHAR(20) DEFAULT 'active'
);
COMMENT ON TABLE geospatial_files IS '地理数据文件信息表';

-- 空间索引
CREATE INDEX idx_geospatial_files_bounds_geom ON geospatial_files USING GIST (bounds_geom);

-- 7. 水泥厂地理数据关联表 (cement_plant_geodata)
CREATE TABLE cement_plant_geodata (
    relation_id BIGSERIAL PRIMARY KEY,
    plant_id BIGINT NOT NULL REFERENCES cement_plants(plant_id) ON DELETE CASCADE,
    file_id BIGINT NOT NULL REFERENCES geospatial_files(file_id) ON DELETE CASCADE,
    identification_id BIGINT REFERENCES cement_plant_identifications(identification_id) ON DELETE SET NULL,
    data_type VARCHAR(50) NOT NULL,
    acquisition_date DATE,
    processing_status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(plant_id, file_id, data_type, acquisition_date) -- 避免时间序列冲突
);
COMMENT ON TABLE cement_plant_geodata IS '水泥厂与地理数据关联表';

-- 索引
CREATE INDEX idx_cement_plant_geodata_plant ON cement_plant_geodata (plant_id);
CREATE INDEX idx_cement_plant_geodata_file ON cement_plant_geodata (file_id);
CREATE INDEX idx_cement_plant_geodata_identification ON cement_plant_geodata (identification_id);

-- 8. 地理数据瓦片表 (geospatial_tiles)
CREATE TABLE geospatial_tiles (
    tile_id BIGSERIAL PRIMARY KEY,
    file_id BIGINT NOT NULL REFERENCES geospatial_files(file_id) ON DELETE CASCADE,
    zoom_level INTEGER NOT NULL,
    tile_x INTEGER NOT NULL,
    tile_y INTEGER NOT NULL,
    tile_data BYTEA,
    tile_size INTEGER DEFAULT 256,
    format VARCHAR(10) DEFAULT 'PNG',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(file_id, zoom_level, tile_x, tile_y)
);
COMMENT ON TABLE geospatial_tiles IS '地理数据瓦片表';

-- 索引
CREATE INDEX idx_geospatial_tiles_file_zoom ON geospatial_tiles (file_id, zoom_level);

-- 9. 视图：水泥厂完整信息
CREATE VIEW cement_plant_full_info AS
SELECT 
    cp.plant_id,
    cp.plant_name,
    cp.longitude,
    cp.latitude,
    cp.province,
    cp.city,
    cp.district,
    cp.status,
    gf.file_id,
    gf.file_name,
    gf.file_path,
    gf.file_type,
    gf.coordinate_system,
    gf.bounds_west,
    gf.bounds_east,
    gf.bounds_south,
    gf.bounds_north,
    cpg.data_type,
    cpg.acquisition_date,
    cpg.processing_status
FROM cement_plants cp
LEFT JOIN cement_plant_geodata cpg ON cp.plant_id = cpg.plant_id
LEFT JOIN geospatial_files gf ON cpg.file_id = gf.file_id;

COMMENT ON VIEW cement_plant_full_info IS '水泥厂完整信息视图，包含地理数据信息';