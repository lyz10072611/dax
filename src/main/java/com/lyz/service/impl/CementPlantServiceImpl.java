package com.lyz.service.impl;

import com.lyz.mapper.CementPlantMapper;
import com.lyz.mapper.GeospatialMapper;
import com.lyz.pojo.CementPlant;
import com.lyz.pojo.CementPlantIdentification;
import com.lyz.pojo.CementPlantDataDTO;
import com.lyz.pojo.GeospatialFile;
import com.lyz.pojo.CementPlantGeodata;
import com.lyz.pojo.PageBean;
import com.lyz.service.CementPlantService;
import com.lyz.service.DataLockService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.HashMap;

/**
 * 水泥厂业务服务实现类
 */
@Service
public class CementPlantServiceImpl implements CementPlantService {
    
    @Autowired
    private CementPlantMapper cementPlantMapper;
    
    @Autowired
    private GeospatialMapper geospatialMapper;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Autowired
    private DataLockService dataLockService;
    
    @Override
    public CementPlant findById(Long plantId) {
        return cementPlantMapper.findById(plantId);
    }
    
    @Override
    public CementPlant findByName(String plantName) {
        return cementPlantMapper.findByName(plantName);
    }
    
    @Override
    public PageBean<CementPlant> pagePlants(Integer pageNum, Integer pageSize, String plantName, String province, String city, String status) {
        PageHelper.startPage(pageNum, pageSize);
        List<CementPlant> list = cementPlantMapper.listPlants();
        PageInfo<CementPlant> pageInfo = new PageInfo<>(list);
        return new PageBean<>(pageInfo.getTotal(), pageInfo.getList());
    }
    
    @Override
    public List<CementPlant> listPlants() {
        return cementPlantMapper.listPlants();
    }
    
    @Override
    public void addPlant(CementPlant cementPlant) {
        // 设置默认状态
        if (cementPlant.getStatus() == null || cementPlant.getStatus().isEmpty()) {
            cementPlant.setStatus("active");
        }
        cementPlantMapper.insert(cementPlant);
    }
    
    @Override
    public void updatePlant(CementPlant cementPlant) {
        cementPlantMapper.update(cementPlant);
    }
    
    @Override
    public void deletePlant(Long plantId) {
        cementPlantMapper.deleteById(plantId);
    }
    
    @Override
    public List<CementPlant> findPlantsByLocationRange(Double minLng, Double maxLng, Double minLat, Double maxLat) {
        return cementPlantMapper.findByLocationRange(minLng, maxLng, minLat, maxLat);
    }
    
    @Override
    public CementPlantIdentification findIdentificationById(Long identificationId) {
        return cementPlantMapper.findIdentificationById(identificationId);
    }
    
    @Override
    public PageBean<CementPlantIdentification> pageIdentifications(Integer pageNum, Integer pageSize, Long plantId, String dataSource, String startTime, String endTime) {
        PageHelper.startPage(pageNum, pageSize);
        List<CementPlantIdentification> list = cementPlantMapper.listIdentifications();
        PageInfo<CementPlantIdentification> pageInfo = new PageInfo<>(list);
        return new PageBean<>(pageInfo.getTotal(), pageInfo.getList());
    }
    
    @Override
    public List<CementPlantIdentification> listIdentifications() {
        return cementPlantMapper.listIdentifications();
    }
    
    @Override
    public void addIdentification(CementPlantIdentification identification) {
        // 设置默认数据源
        if (identification.getDataSource() == null || identification.getDataSource().isEmpty()) {
            identification.setDataSource("GF-2");
        }
        cementPlantMapper.insertIdentification(identification);
    }
    
    @Override
    public void updateIdentification(CementPlantIdentification identification) {
        cementPlantMapper.updateIdentification(identification);
    }
    
    @Override
    public void deleteIdentification(Long identificationId) {
        cementPlantMapper.deleteIdentificationById(identificationId);
    }
    
    @Override
    public Integer countActivePlants() {
        return cementPlantMapper.countActivePlants();
    }
    
    @Override
    public Integer countIdentificationsByPlantId(Long plantId) {
        return cementPlantMapper.countIdentificationsByPlantId(plantId);
    }
    
    @Override
    public Integer countIdentificationsByTimeRange(String startTime, String endTime) {
        return cementPlantMapper.countIdentificationsByTimeRange(startTime, endTime);
    }
    
    @Override
    @Transactional
    public void addCementPlantData(CementPlantDataDTO dataDTO) {
        // 使用分布式锁防止并发问题
        String lockValue = dataLockService.acquireCementPlantLock(dataDTO.getPlantName(), 30);
        
        if (lockValue == null) {
            throw new RuntimeException("系统繁忙，请稍后重试");
        }
        
        try {
            
            // 1. 根据名称判断水泥厂是否存在（先查Redis缓存）
            CementPlant existingPlant = getCementPlantFromCacheOrDB(dataDTO.getPlantName());
            
            Long plantId;
            if (existingPlant == null) {
                // 2. 如果不存在，先插入到cement_plants表
                CementPlant newPlant = new CementPlant();
                newPlant.setPlantName(dataDTO.getPlantName());
                newPlant.setLongitude(dataDTO.getLongitude());
                newPlant.setLatitude(dataDTO.getLatitude());
                newPlant.setProvince(dataDTO.getProvince());
                newPlant.setCity(dataDTO.getCity());
                newPlant.setDistrict(dataDTO.getDistrict());
                newPlant.setStatus(dataDTO.getStatus() != null ? dataDTO.getStatus() : "active");
                newPlant.setCreateTime(LocalDateTime.now());
                
                cementPlantMapper.insert(newPlant);
                plantId = newPlant.getPlantId();
                
                // 缓存新创建的水泥厂信息
                cacheCementPlant(newPlant);
                
            } else {
                // 3. 如果存在，直接使用现有的plantId
                plantId = existingPlant.getPlantId();
            }
            
            // 4. 插入识别记录到cement_plant_identifications表
            CementPlantIdentification identification = new CementPlantIdentification();
            identification.setPlantId(plantId);
            identification.setLongitude(dataDTO.getLongitude());
            identification.setLatitude(dataDTO.getLatitude());
            identification.setIdentificationTime(dataDTO.getIdentificationTime() != null ? 
                dataDTO.getIdentificationTime() : LocalDateTime.now());
            identification.setDataSource(dataDTO.getDataSource() != null ? 
                dataDTO.getDataSource() : "GF-2");
            identification.setImageUuid(dataDTO.getImageUuid());
            identification.setNdviIndex(dataDTO.getNdviIndex());
            identification.setProvince(dataDTO.getProvince());
            identification.setCity(dataDTO.getCity());
            identification.setDistrict(dataDTO.getDistrict());
            identification.setCreatedAt(LocalDateTime.now());
            
            cementPlantMapper.insertIdentification(identification);
            
            // 5. 如果有TIF文件路径，处理地理数据
            if (dataDTO.getTifFilePath() != null && !dataDTO.getTifFilePath().isEmpty()) {
                processTifFileForPlant(plantId, identification.getIdentificationId(), dataDTO.getTifFilePath());
            }
            
            // 6. 更新Redis中的统计数据
            updateStatisticsCache();
            
        } finally {
            // 释放锁
            dataLockService.releaseCementPlantLock(dataDTO.getPlantName(), lockValue);
        }
    }
    
    /**
     * 处理水泥厂的TIF文件
     */
    private void processTifFileForPlant(Long plantId, Long identificationId, String tifFilePath) {
        try {
            // 检查TIF文件是否已存在
            GeospatialFile existingFile = geospatialMapper.findFileByPath(tifFilePath);
            Long fileId;
            
            if (existingFile == null) {
                // 创建新的地理数据文件记录
                GeospatialFile geospatialFile = new GeospatialFile();
                geospatialFile.setFileName(extractFileName(tifFilePath));
                geospatialFile.setFilePath(tifFilePath);
                geospatialFile.setFileType("TIF");
                geospatialFile.setStatus("processing");
                
                geospatialMapper.insertFile(geospatialFile);
                fileId = geospatialFile.getFileId();
                
                // TODO: 异步处理TIF文件，提取元数据
                // 这里可以调用异步服务来处理TIF文件
            } else {
                fileId = existingFile.getFileId();
            }
            
            // 创建水泥厂与地理数据的关联
            CementPlantGeodata geodata = new CementPlantGeodata();
            geodata.setPlantId(plantId);
            geodata.setFileId(fileId);
            geodata.setIdentificationId(identificationId);
            geodata.setDataType("NDVI"); // 根据实际情况设置
            geodata.setProcessingStatus("pending");
            
            geospatialMapper.insertGeodataRelation(geodata);
            
        } catch (Exception e) {
            // 记录错误日志，但不影响主流程
            System.err.println("处理TIF文件时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 从文件路径中提取文件名
     */
    private String extractFileName(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "unknown.tif";
        }
        int lastSlash = filePath.lastIndexOf('/');
        int lastBackslash = filePath.lastIndexOf('\\');
        int lastSeparator = Math.max(lastSlash, lastBackslash);
        return lastSeparator >= 0 ? filePath.substring(lastSeparator + 1) : filePath;
    }
    
    /**
     * 从缓存或数据库中获取水泥厂信息
     */
    private CementPlant getCementPlantFromCacheOrDB(String plantName) {
        String cacheKey = "cement_plant:" + plantName;
        
        // 先查Redis缓存
        String cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            try {
                // 这里可以使用JSON序列化，简化起见直接查数据库
                return cementPlantMapper.findByName(plantName);
            } catch (Exception e) {
                // 缓存数据异常，清除缓存
                stringRedisTemplate.delete(cacheKey);
            }
        }
        
        // 缓存不存在或异常，查数据库
        CementPlant plant = cementPlantMapper.findByName(plantName);
        if (plant != null) {
            cacheCementPlant(plant);
        }
        
        return plant;
    }
    
    /**
     * 缓存水泥厂信息
     */
    private void cacheCementPlant(CementPlant plant) {
        String cacheKey = "cement_plant:" + plant.getPlantName();
        // 设置缓存过期时间为1小时
        stringRedisTemplate.opsForValue().set(cacheKey, "cached", 1, TimeUnit.HOURS);
    }
    
    /**
     * 更新统计数据缓存
     */
    private void updateStatisticsCache() {
        // 更新活跃水泥厂数量缓存
        Integer activeCount = cementPlantMapper.countActivePlants();
        stringRedisTemplate.opsForValue().set("stats:active_plants_count", 
            String.valueOf(activeCount), 30, TimeUnit.MINUTES);
        
        // 更新总识别记录数量缓存
        Integer totalIdentifications = cementPlantMapper.countIdentificationsByTimeRange(null, null);
        stringRedisTemplate.opsForValue().set("stats:total_identifications_count", 
            String.valueOf(totalIdentifications), 30, TimeUnit.MINUTES);
    }
    
    // ==================== 新增的高级查询方法实现 ====================
    
    @Override
    public List<CementPlant> searchPlantsByName(String plantName, Integer limit) {
        return cementPlantMapper.searchPlantsByName(plantName, limit);
    }
    
    @Override
    public List<CementPlant> findPlantsByRegion(String province, String city, String district) {
        return cementPlantMapper.findPlantsByRegion(province, city, district);
    }
    
    @Override
    public List<CementPlant> findPlantsByLocation(Double longitude, Double latitude, Double radiusKm) {
        // 将公里转换为经纬度范围（简化计算）
        double latRange = radiusKm / 111.0; // 1度纬度约等于111公里
        double lngRange = radiusKm / (111.0 * Math.cos(Math.toRadians(latitude)));
        
        return cementPlantMapper.findByLocationRange(
            longitude - lngRange, longitude + lngRange,
            latitude - latRange, latitude + latRange);
    }
    
    @Override
    public List<CementPlant> findPlantsByStatus(String status) {
        return cementPlantMapper.findPlantsByStatus(status);
    }
    
    @Override
    public List<CementPlantIdentification> findIdentificationsByPlantId(Long plantId) {
        return cementPlantMapper.findIdentificationsByPlantId(plantId);
    }
    
    @Override
    public CementPlantIdentification findLatestIdentificationByPlantId(Long plantId) {
        return cementPlantMapper.findLatestIdentificationByPlantId(plantId);
    }
    
    @Override
    public List<CementPlantIdentification> findIdentificationsByTimeRange(String startTime, String endTime, Long plantId) {
        return cementPlantMapper.findIdentificationsByTimeRangeWithPlantId(startTime, endTime, plantId);
    }
    
    @Override
    public List<CementPlantIdentification> findIdentificationsByNdviRange(Float minNdvi, Float maxNdvi) {
        return cementPlantMapper.findIdentificationsByNdviRange(minNdvi, maxNdvi);
    }
    
    @Override
    public PageBean<CementPlantIdentification> comprehensiveSearch(Integer pageNum, Integer pageSize, 
        String plantName, String province, String city, String district, String status,
        String startTime, String endTime, Float minNdvi, Float maxNdvi, 
        Double longitude, Double latitude, Double radiusKm) {
        
        PageHelper.startPage(pageNum, pageSize);
        List<CementPlantIdentification> list = cementPlantMapper.comprehensiveSearch(
            plantName, province, city, district, status, startTime, endTime, 
            minNdvi, maxNdvi, longitude, latitude, radiusKm);
        PageInfo<CementPlantIdentification> pageInfo = new PageInfo<>(list);
        return new PageBean<>(pageInfo.getTotal(), pageInfo.getList());
    }
    
    @Override
    public Map<String, Object> getStatisticsOverview() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 从缓存获取统计数据
        String activeCountStr = stringRedisTemplate.opsForValue().get("stats:active_plants_count");
        String totalIdentificationsStr = stringRedisTemplate.opsForValue().get("stats:total_identifications_count");
        
        if (activeCountStr != null) {
            statistics.put("activePlantsCount", Integer.parseInt(activeCountStr));
        } else {
            statistics.put("activePlantsCount", cementPlantMapper.countActivePlants());
        }
        
        if (totalIdentificationsStr != null) {
            statistics.put("totalIdentificationsCount", Integer.parseInt(totalIdentificationsStr));
        } else {
            statistics.put("totalIdentificationsCount", cementPlantMapper.countIdentificationsByTimeRange(null, null));
        }
        
        // 其他统计数据
        statistics.put("totalPlantsCount", cementPlantMapper.countTotalPlants());
        statistics.put("inactivePlantsCount", cementPlantMapper.countInactivePlants());
        statistics.put("underConstructionPlantsCount", cementPlantMapper.countUnderConstructionPlants());
        
        return statistics;
    }
    
    @Override
    public Map<String, Object> getStatisticsByRegion() {
        return cementPlantMapper.getStatisticsByRegion();
    }
    
    @Override
    public Map<String, Object> getStatisticsByTime(String timeUnit) {
        return cementPlantMapper.getStatisticsByTime(timeUnit);
    }
}

