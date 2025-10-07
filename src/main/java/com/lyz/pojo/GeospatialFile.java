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
 * 地理数据文件实体类
 */
@Data
public class GeospatialFile {
    private Long fileId;
    
    @NotBlank(message = "文件名不能为空")
    private String fileName;
    
    @NotBlank(message = "文件路径不能为空")
    private String filePath;
    
    private String fileType = "TIF"; // 文件类型
    
    private Long fileSize; // 文件大小（字节）
    
    private String coordinateSystem = "EPSG:4326"; // 坐标系
    
    @DecimalMin(value = "-180.0", message = "西边界经度范围应在-180到180之间")
    @DecimalMax(value = "180.0", message = "西边界经度范围应在-180到180之间")
    private BigDecimal boundsWest; // 西边界经度
    
    @DecimalMin(value = "-180.0", message = "东边界经度范围应在-180到180之间")
    @DecimalMax(value = "180.0", message = "东边界经度范围应在-180到180之间")
    private BigDecimal boundsEast; // 东边界经度
    
    @DecimalMin(value = "-90.0", message = "南边界纬度范围应在-90到90之间")
    @DecimalMax(value = "90.0", message = "南边界纬度范围应在-90到90之间")
    private BigDecimal boundsSouth; // 南边界纬度
    
    @DecimalMin(value = "-90.0", message = "北边界纬度范围应在-90到90之间")
    @DecimalMax(value = "90.0", message = "北边界纬度范围应在-90到90之间")
    private BigDecimal boundsNorth; // 北边界纬度
    
    private BigDecimal resolutionX; // X方向分辨率
    private BigDecimal resolutionY; // Y方向分辨率
    
    private Integer bandsCount = 1; // 波段数量
    
    private String dataType = "FLOAT32"; // 数据类型
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime uploadTime;
    
    private Long uploadUserId; // 上传用户ID
    
    private String status = "active"; // 状态
    
    // 验证分组接口
    public interface Add {}
    public interface Update {}
}
