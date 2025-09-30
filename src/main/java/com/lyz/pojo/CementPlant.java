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
 * 水泥厂基本信息实体类
 */
@Data
public class CementPlant {
    private Long plantId;
    
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
    
    private String status; // active, inactive, under_construction
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    // 验证分组接口
    public interface Add {}
    public interface Update {}
}
