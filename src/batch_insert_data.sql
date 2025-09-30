-- ========================================
-- 多表格数据插入脚本
-- 一次性插入所有测试数据
-- ========================================

-- 1. 插入角色数据
INSERT INTO roles (role_id, role_name, description) VALUES 
(1, 'admin', '系统管理员'),
(2, 'user', '普通用户'),
(3, 'operator', '操作员')
ON CONFLICT (role_id) DO UPDATE SET
    role_name = EXCLUDED.role_name,
    description = EXCLUDED.description;

-- 2. 插入用户数据
INSERT INTO users (username, password_hash, email, create_time, status) VALUES 
('zhangsan', MD5('123456'), 'zhangsan@example.com', NOW(), 1),
('lisi', MD5('123456'), 'lisi@example.com', NOW(), 1),
('wangwu', MD5('123456'), 'wangwu@example.com', NOW(), 1),
('zhaoliu', MD5('123456'), 'zhaoliu@example.com', NOW(), 1)
ON CONFLICT (username) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    email = EXCLUDED.email,
    status = EXCLUDED.status;

-- 3. 插入用户角色关联
INSERT INTO user_roles (user_id, role_id) VALUES 
((SELECT user_id FROM users WHERE username = 'zhangsan' LIMIT 1), 1),
((SELECT user_id FROM users WHERE username = 'lisi' LIMIT 1), 2),
((SELECT user_id FROM users WHERE username = 'wangwu' LIMIT 1), 2),
((SELECT user_id FROM users WHERE username = 'zhaoliu' LIMIT 1), 3)
ON CONFLICT (user_id, role_id) DO NOTHING;

-- 4. 插入水泥厂基本信息
INSERT INTO cement_plants (plant_name, longitude, latitude, province, city, district, status) VALUES 
('河北冀东水泥厂', 114.3456, 38.1234, '河北省', '石家庄市', '鹿泉区', 'active'),
('华新水泥厂', 114.1234, 30.5678, '湖北省', '武汉市', '江夏区', 'active'),
('海螺水泥厂', 118.7890, 31.2345, '江苏省', '南京市', '江宁区', 'active'),
('中联水泥厂', 113.4567, 34.7890, '河南省', '郑州市', '中原区', 'inactive'),
('金隅水泥厂', 116.2345, 39.5678, '北京市', '北京市', '房山区', 'active');

-- 5. 插入地理数据文件信息（TIF文件）
INSERT INTO geospatial_files (
    file_name, 
    file_path, 
    file_type, 
    coordinate_system, 
    bounds_west, 
    bounds_east, 
    bounds_south, 
    bounds_north, 
    resolution_x, 
    resolution_y, 
    bands_count, 
    data_type, 
    upload_time, 
    upload_user_id, 
    status
) VALUES 
('GF02_PM1_052255_20240328_KS5K1_01_034_L1A_01_2.tif', 
 'X:\yzLV\GF\test\jjj\m\normalize\GF02_PM1_052255_20240328_KS5K1_01_034_L1A_01_2.tif',
 'TIF', 'EPSG:4326', 114.3000, 114.4000, 38.1000, 38.2000, 0.0001, 0.0001, 1, 'FLOAT32', NOW(),
 (SELECT user_id FROM users WHERE username = 'zhangsan' LIMIT 1), 'active'),
 
('GF02_PM1_052255_20240329_KS5K1_01_035_L1A_01_2.tif',
 'X:\yzLV\GF\test\jjj\m\normalize\GF02_PM1_052255_20240329_KS5K1_01_035_L1A_01_2.tif',
 'TIF', 'EPSG:4326', 114.1000, 114.2000, 30.5000, 30.6000, 0.0001, 0.0001, 1, 'FLOAT32', NOW(),
 (SELECT user_id FROM users WHERE username = 'lisi' LIMIT 1), 'active'),
 
('GF02_PM1_052255_20240330_KS5K1_01_036_L1A_01_2.tif',
 'X:\yzLV\GF\test\jjj\m\normalize\GF02_PM1_052255_20240330_KS5K1_01_036_L1A_01_2.tif',
 'TIF', 'EPSG:4326', 118.7000, 118.9000, 31.2000, 31.3000, 0.0001, 0.0001, 1, 'FLOAT32', NOW(),
 (SELECT user_id FROM users WHERE username = 'wangwu' LIMIT 1), 'active'),

('LC08_L1TP_113034_20240331_20240401_01_T1.tif',
 'X:\yzLV\GF\test\jjj\m\normalize\LC08_L1TP_113034_20240331_20240401_01_T1.tif',
 'TIF', 'EPSG:4326', 113.4000, 113.5000, 34.7000, 34.8000, 0.0001, 0.0001, 1, 'FLOAT32', NOW(),
 (SELECT user_id FROM users WHERE username = 'zhaoliu' LIMIT 1), 'active'),

('S2A_MSIL1C_20240401T023541_N0400_R089_T50TMK_20240401T060551.tif',
 'X:\yzLV\GF\test\jjj\m\normalize\S2A_MSIL1C_20240401T023541_N0400_R089_T50TMK_20240401T060551.tif',
 'TIF', 'EPSG:4326', 116.2000, 116.3000, 39.5000, 39.6000, 0.0001, 0.0001, 1, 'FLOAT32', NOW(),
 (SELECT user_id FROM users WHERE username = 'zhaoliu' LIMIT 1), 'active');

-- 6. 插入水泥厂识别记录
INSERT INTO cement_plant_identifications (
    plant_id,
    longitude,
    latitude,
    identification_time,
    data_source,
    image_uuid,
    ndvi_index,
    province,
    city,
    district,
    created_at
) VALUES 
((SELECT plant_id FROM cement_plants WHERE plant_name = '河北冀东水泥厂' LIMIT 1), 114.3456, 38.1234, '2024-03-28 10:00:00', 'GF-2', 'GF02_PM1_052255_20240328_KS5K1_01_034_L1A_01_2', 0.666, '河北省', '石家庄市', '鹿泉区', NOW()),
((SELECT plant_id FROM cement_plants WHERE plant_name = '华新水泥厂' LIMIT 1), 114.1234, 30.5678, '2024-03-29 11:00:00', 'GF-2', 'GF02_PM1_052255_20240329_KS5K1_01_035_L1A_01_2', 0.723, '湖北省', '武汉市', '江夏区', NOW()),
((SELECT plant_id FROM cement_plants WHERE plant_name = '海螺水泥厂' LIMIT 1), 118.7890, 31.2345, '2024-03-30 12:00:00', 'GF-2', 'GF02_PM1_052255_20240330_KS5K1_01_036_L1A_01_2', 0.589, '江苏省', '南京市', '江宁区', NOW()),
((SELECT plant_id FROM cement_plants WHERE plant_name = '中联水泥厂' LIMIT 1), 113.4567, 34.7890, '2024-03-31 13:00:00', 'Landsat-8', 'LC08_L1TP_113034_20240331_20240401_01_T1', 0.445, '河南省', '郑州市', '中原区', NOW()),
((SELECT plant_id FROM cement_plants WHERE plant_name = '金隅水泥厂' LIMIT 1), 116.2345, 39.5678, '2024-04-01 14:00:00', 'Sentinel-2', 'S2A_MSIL1C_20240401T023541_N0400_R089_T50TMK_20240401T060551', 0.812, '北京市', '北京市', '房山区', NOW());

-- 7. 插入水泥厂与地理数据关联
INSERT INTO cement_plant_geodata (
    plant_id,
    file_id,
    identification_id,
    data_type,
    acquisition_date,
    processing_status,
    created_at
) VALUES 
((SELECT plant_id FROM cement_plants WHERE plant_name = '河北冀东水泥厂' LIMIT 1), 
 (SELECT file_id FROM geospatial_files WHERE file_name = 'GF02_PM1_052255_20240328_KS5K1_01_034_L1A_01_2.tif' LIMIT 1), 
 (SELECT identification_id FROM cement_plant_identifications WHERE image_uuid = 'GF02_PM1_052255_20240328_KS5K1_01_034_L1A_01_2' LIMIT 1), 
 'NDVI', '2024-03-28', 'processed', NOW()),

((SELECT plant_id FROM cement_plants WHERE plant_name = '华新水泥厂' LIMIT 1), 
 (SELECT file_id FROM geospatial_files WHERE file_name = 'GF02_PM1_052255_20240329_KS5K1_01_035_L1A_01_2.tif' LIMIT 1), 
 (SELECT identification_id FROM cement_plant_identifications WHERE image_uuid = 'GF02_PM1_052255_20240329_KS5K1_01_035_L1A_01_2' LIMIT 1), 
 'NDVI', '2024-03-29', 'processed', NOW()),

((SELECT plant_id FROM cement_plants WHERE plant_name = '海螺水泥厂' LIMIT 1), 
 (SELECT file_id FROM geospatial_files WHERE file_name = 'GF02_PM1_052255_20240330_KS5K1_01_036_L1A_01_2.tif' LIMIT 1), 
 (SELECT identification_id FROM cement_plant_identifications WHERE image_uuid = 'GF02_PM1_052255_20240330_KS5K1_01_036_L1A_01_2' LIMIT 1), 
 'NDVI', '2024-03-30', 'processed', NOW()),

((SELECT plant_id FROM cement_plants WHERE plant_name = '中联水泥厂' LIMIT 1), 
 (SELECT file_id FROM geospatial_files WHERE file_name = 'LC08_L1TP_113034_20240331_20240401_01_T1.tif' LIMIT 1), 
 (SELECT identification_id FROM cement_plant_identifications WHERE image_uuid = 'LC08_L1TP_113034_20240331_20240401_01_T1' LIMIT 1), 
 'NDVI', '2024-03-31', 'pending', NOW()),

((SELECT plant_id FROM cement_plants WHERE plant_name = '金隅水泥厂' LIMIT 1), 
 (SELECT file_id FROM geospatial_files WHERE file_name = 'S2A_MSIL1C_20240401T023541_N0400_R089_T50TMK_20240401T060551.tif' LIMIT 1), 
 (SELECT identification_id FROM cement_plant_identifications WHERE image_uuid = 'S2A_MSIL1C_20240401T023541_N0400_R089_T50TMK_20240401T060551' LIMIT 1), 
 'NDVI', '2024-04-01', 'pending', NOW())
ON CONFLICT (plant_id, file_id, data_type, acquisition_date) DO UPDATE SET
    identification_id = EXCLUDED.identification_id,
    processing_status = EXCLUDED.processing_status,
    created_at = NOW();

-- 8. 插入瓦片数据（为前三个TIF文件生成瓦片记录）
-- 河北冀东水泥厂TIF文件的瓦片
INSERT INTO geospatial_tiles (file_id, zoom_level, tile_x, tile_y, tile_size, format)
SELECT 
    gf.file_id,
    0 as zoom_level,
    x.tile_x,
    y.tile_y,
    256 as tile_size,
    'PNG' as format
FROM geospatial_files gf
CROSS JOIN generate_series(0, 1) as x(tile_x)
CROSS JOIN generate_series(0, 1) as y(tile_y)
WHERE gf.file_name = 'GF02_PM1_052255_20240328_KS5K1_01_034_L1A_01_2.tif'
ON CONFLICT (file_id, zoom_level, tile_x, tile_y) DO NOTHING;

-- 华新水泥厂TIF文件的瓦片
INSERT INTO geospatial_tiles (file_id, zoom_level, tile_x, tile_y, tile_size, format)
SELECT 
    gf.file_id,
    0 as zoom_level,
    x.tile_x,
    y.tile_y,
    256 as tile_size,
    'PNG' as format
FROM geospatial_files gf
CROSS JOIN generate_series(0, 1) as x(tile_x)
CROSS JOIN generate_series(0, 1) as y(tile_y)
WHERE gf.file_name = 'GF02_PM1_052255_20240329_KS5K1_01_035_L1A_01_2.tif'
ON CONFLICT (file_id, zoom_level, tile_x, tile_y) DO NOTHING;

-- 海螺水泥厂TIF文件的瓦片
INSERT INTO geospatial_tiles (file_id, zoom_level, tile_x, tile_y, tile_size, format)
SELECT 
    gf.file_id,
    0 as zoom_level,
    x.tile_x,
    y.tile_y,
    256 as tile_size,
    'PNG' as format
FROM geospatial_files gf
CROSS JOIN generate_series(0, 1) as x(tile_x)
CROSS JOIN generate_series(0, 1) as y(tile_y)
WHERE gf.file_name = 'GF02_PM1_052255_20240330_KS5K1_01_036_L1A_01_2.tif'
ON CONFLICT (file_id, zoom_level, tile_x, tile_y) DO NOTHING;

-- 中联水泥厂TIF文件的瓦片
INSERT INTO geospatial_tiles (file_id, zoom_level, tile_x, tile_y, tile_size, format)
SELECT 
    gf.file_id,
    0 as zoom_level,
    x.tile_x,
    y.tile_y,
    256 as tile_size,
    'PNG' as format
FROM geospatial_files gf
CROSS JOIN generate_series(0, 1) as x(tile_x)
CROSS JOIN generate_series(0, 1) as y(tile_y)
WHERE gf.file_name = 'LC08_L1TP_113034_20240331_20240401_01_T1.tif'
ON CONFLICT (file_id, zoom_level, tile_x, tile_y) DO NOTHING;

-- 金隅水泥厂TIF文件的瓦片
INSERT INTO geospatial_tiles (file_id, zoom_level, tile_x, tile_y, tile_size, format)
SELECT 
    gf.file_id,
    0 as zoom_level,
    x.tile_x,
    y.tile_y,
    256 as tile_size,
    'PNG' as format
FROM geospatial_files gf
CROSS JOIN generate_series(0, 1) as x(tile_x)
CROSS JOIN generate_series(0, 1) as y(tile_y)
WHERE gf.file_name = 'S2A_MSIL1C_20240401T023541_N0400_R089_T50TMK_20240401T060551.tif'
ON CONFLICT (file_id, zoom_level, tile_x, tile_y) DO NOTHING;

-- ========================================
-- 数据验证查询
-- ========================================

-- 显示插入结果统计
SELECT '=== 数据插入统计 ===' as section;

SELECT '用户数量' as item, COUNT(*) as count FROM users
UNION ALL
SELECT '角色数量' as item, COUNT(*) as count FROM roles
UNION ALL
SELECT '用户角色关联数量' as item, COUNT(*) as count FROM user_roles
UNION ALL
SELECT '水泥厂数量' as item, COUNT(*) as count FROM cement_plants
UNION ALL
SELECT '地理文件数量' as item, COUNT(*) as count FROM geospatial_files
UNION ALL
SELECT '识别记录数量' as item, COUNT(*) as count FROM cement_plant_identifications
UNION ALL
SELECT '关联关系数量' as item, COUNT(*) as count FROM cement_plant_geodata
UNION ALL
SELECT '瓦片数量' as item, COUNT(*) as count FROM geospatial_tiles;

-- 显示水泥厂详细信息
SELECT '=== 水泥厂详细信息 ===' as section;
SELECT 
    plant_name,
    CONCAT(province, city, district) as address,
    longitude,
    latitude,
    status,
    COUNT(cpi.identification_id) as identification_count,
    COUNT(gf.file_id) as file_count
FROM cement_plants cp
LEFT JOIN cement_plant_identifications cpi ON cp.plant_id = cpi.plant_id
LEFT JOIN cement_plant_geodata cpg ON cp.plant_id = cpg.plant_id
LEFT JOIN geospatial_files gf ON cpg.file_id = gf.file_id
GROUP BY cp.plant_id, cp.plant_name, cp.province, cp.city, cp.district, cp.longitude, cp.latitude, cp.status
ORDER BY cp.plant_name;

-- 显示TIF文件信息
SELECT '=== TIF文件信息 ===' as section;
SELECT 
    gf.file_name,
    gf.file_path,
    gf.file_size,
    CONCAT(gf.bounds_west, ',', gf.bounds_south, ' to ', gf.bounds_east, ',', gf.bounds_north) as bounds,
    gf.status,
    COUNT(gt.tile_id) as tile_count
FROM geospatial_files gf
LEFT JOIN geospatial_tiles gt ON gf.file_id = gt.file_id
GROUP BY gf.file_id, gf.file_name, gf.file_path, gf.file_size, gf.bounds_west, gf.bounds_east, gf.bounds_south, gf.bounds_north, gf.status
ORDER BY gf.file_name;

-- 显示识别记录信息
SELECT '=== 识别记录信息 ===' as section;
SELECT 
    cp.plant_name,
    cpi.image_uuid,
    cpi.data_source,
    cpi.ndvi_index,
    cpi.identification_time,
    cpg.processing_status
FROM cement_plant_identifications cpi
LEFT JOIN cement_plants cp ON cpi.plant_id = cp.plant_id
LEFT JOIN cement_plant_geodata cpg ON cpi.identification_id = cpg.identification_id
ORDER BY cpi.identification_time;

-- 显示完成信息
SELECT '=== 所有数据插入完成 ===' as final_status;
