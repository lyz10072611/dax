package com.lyz.pojo;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 水泥厂地理数据关联实体类
 */
@Data
public class CementPlantGeodata {
    private Long relationId;
    
    @NotNull(message = "水泥厂ID不能为空")
    private Long plantId;
    
    @NotNull(message = "文件ID不能为空")
    private Long fileId;
    
    private Long identificationId; // 关联的识别记录ID
    
    private String dataType; // 数据类型
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime acquisitionDate; // 数据获取日期
    
    private String processingStatus = "pending"; // 处理状态
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime createdAt;
    
    // 关联查询字段（用于查询结果）
    private String plantName;
    private String fileName;
    private String filePath;
    
    // 验证分组接口
    public interface Add {}
    public interface Update {}
}