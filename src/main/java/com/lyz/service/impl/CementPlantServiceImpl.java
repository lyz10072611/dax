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
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 水泥厂业务服务实现类
 */
@Service
public class CementPlantServiceImpl implements CementPlantService {
    
    @Autowired
    private CementPlantMapper cementPlantMapper;
    
    @Autowired
    private GeospatialMapper geospatialMapper;
    
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
        // 1. 根据名称判断水泥厂是否存在
        CementPlant existingPlant = cementPlantMapper.findByName(dataDTO.getPlantName());
        
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
            
            cementPlantMapper.insert(newPlant);
            plantId = newPlant.getPlantId();
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
        
        cementPlantMapper.insertIdentification(identification);
        
        // 5. 如果有TIF文件路径，处理地理数据
        if (dataDTO.getTifFilePath() != null && !dataDTO.getTifFilePath().isEmpty()) {
            processTifFileForPlant(plantId, identification.getIdentificationId(), dataDTO.getTifFilePath());
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
}

