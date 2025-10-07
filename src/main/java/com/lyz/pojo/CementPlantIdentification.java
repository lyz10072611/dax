package com.lyz.pojo;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 水泥厂识别记录实体类
 */
@Data
public class CementPlantIdentification {
    private Long identificationId;
    
    @NotNull(message = "水泥厂ID不能为空")
    private Long plantId;
    
    @NotNull(message = "识别经度不能为空")
    @DecimalMin(value = "-180.0", message = "经度范围应在-180到180之间")
    @DecimalMax(value = "180.0", message = "经度范围应在-180到180之间")
    private BigDecimal longitude;
    
    @NotNull(message = "识别纬度不能为空")
    @DecimalMin(value = "-90.0", message = "纬度范围应在-90到90之间")
    @DecimalMax(value = "90.0", message = "纬度范围应在-90到90之间")
    private BigDecimal latitude;
    
    @NotNull(message = "识别时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime identificationTime;
    
    private String dataSource; // 数据来源，如 GF-2, Landsat-8, Sentinel-2
    private String imageUuid; // 关联的原始影像文件标识
    private Float ndviIndex; // NDVI指数
    private String province;
    private String city;
    private String district;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime createdAt;
    
    // 关联的水泥厂信息（用于查询结果）
    private String plantName;
    
    // 验证分组接口
    public interface Add {}
    public interface Update {}
}

