package com.lyz.service;

import com.lyz.pojo.CementPlant;
import com.lyz.pojo.CementPlantIdentification;
import com.lyz.pojo.CementPlantDataDTO;
import com.lyz.pojo.PageBean;

import java.util.List;

/**
 * 水泥厂业务服务接口
 */
public interface CementPlantService {
    
    // 水泥厂管理
    CementPlant findById(Long plantId);
    CementPlant findByName(String plantName);
    PageBean<CementPlant> pagePlants(Integer pageNum, Integer pageSize, String plantName, String province, String city, String status);
    List<CementPlant> listPlants();
    void addPlant(CementPlant cementPlant);
    void updatePlant(CementPlant cementPlant);
    void deletePlant(Long plantId);
    List<CementPlant> findPlantsByLocationRange(Double minLng, Double maxLng, Double minLat, Double maxLat);
    
    // 水泥厂识别记录管理
    CementPlantIdentification findIdentificationById(Long identificationId);
    PageBean<CementPlantIdentification> pageIdentifications(Integer pageNum, Integer pageSize, Long plantId, String dataSource, String startTime, String endTime);
    List<CementPlantIdentification> listIdentifications();
    void addIdentification(CementPlantIdentification identification);
    void updateIdentification(CementPlantIdentification identification);
    void deleteIdentification(Long identificationId);
    
    // 统计信息
    Integer countActivePlants();
    Integer countIdentificationsByPlantId(Long plantId);
    Integer countIdentificationsByTimeRange(String startTime, String endTime);
    
    // 批量新增水泥厂数据
    void addCementPlantData(CementPlantDataDTO dataDTO);
}

