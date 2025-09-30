package com.lyz.pojo;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 水泥厂数据接收DTO
 * 用于接收包含坐标、水泥厂名称、省市区、运行状态、NDVI指数、image_uuid的JSON数据
 */
@Data
public class CementPlantDataDTO {
    
    @NotBlank(message = "水泥厂名称不能为空")
    private String plantName;
    
    @NotNull(message = "经度不能为空")
    @DecimalMin(value = "-180.0", message = "经度范围应在-180到180之间")
    @DecimalMax(value = "180.0", message = "经度范围应在-180到180之间")
    private BigDecimal longitude;
    
    @NotNull(message = "纬度不能为空")
    @DecimalMin(value = "-90.0", message = "纬度范围应在-90到90之间")
    @DecimalMax(value = "90.0", message = "纬度范围应在-90到90之间")
    private BigDecimal latitude;
    
    private String province;
    private String city;
    private String district;
    
    private String status; // 运行状态: active, inactive, under_construction
    
    private Float ndviIndex; // NDVI指数
    
    private String imageUuid; // 影像文件标识
    
    private String tifFilePath; // TIF文件路径
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime identificationTime; // 识别时间，默认为当前时间
    
    private String dataSource; // 数据来源，默认为GF-2
    
    // 验证分组接口
    public interface Add {}
}
