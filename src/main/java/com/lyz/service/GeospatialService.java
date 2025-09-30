package com.lyz.service;

import com.lyz.pojo.GeospatialFile;
import com.lyz.pojo.CementPlantGeodata;
import com.lyz.pojo.CementPlantDataDTO;

import java.util.List;

/**
 * 地理数据服务接口
 */
public interface GeospatialService {
    
    // 地理数据文件管理
    GeospatialFile findFileById(Long fileId);
    GeospatialFile findFileByPath(String filePath);
    List<GeospatialFile> listFiles(String fileType, String status);
    void addFile(GeospatialFile geospatialFile);
    void updateFile(GeospatialFile geospatialFile);
    void deleteFile(Long fileId);
    
    // 水泥厂地理数据关联管理
    CementPlantGeodata findGeodataById(Long relationId);
    List<CementPlantGeodata> listGeodataByPlantId(Long plantId);
    List<CementPlantGeodata> listGeodataByFileId(Long fileId);
    void addGeodataRelation(CementPlantGeodata geodata);
    void updateGeodataRelation(CementPlantGeodata geodata);
    void deleteGeodataRelation(Long relationId);
    
    // TIF文件处理
    GeospatialFile processTifFile(String tifFilePath, Long uploadUserId);
    void generateTiles(Long fileId);
    boolean validateTifFile(String filePath);
    
    // 批量处理水泥厂数据（包含TIF文件）
    void processCementPlantDataWithTif(CementPlantDataDTO dataDTO, Long uploadUserId);
    
    // 获取瓦片数据
    byte[] getTileData(Long fileId, Integer zoom, Integer x, Integer y);
    String getTileUrl(Long fileId, Integer zoom, Integer x, Integer y);
}
