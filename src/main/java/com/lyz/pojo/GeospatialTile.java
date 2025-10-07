package com.lyz.pojo;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 地理数据瓦片实体类
 */
@Data
public class GeospatialTile {
    private Long tileId;
    
    @NotNull(message = "文件ID不能为空")
    private Long fileId;
    
    @NotNull(message = "缩放级别不能为空")
    private Integer zoomLevel;
    
    @NotNull(message = "瓦片X坐标不能为空")
    private Integer tileX;
    
    @NotNull(message = "瓦片Y坐标不能为空")
    private Integer tileY;
    
    private byte[] tileData; // 瓦片数据
    
    private Integer tileSize; // 瓦片大小
    
    private String format = "PNG"; // 瓦片格式
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime createdAt;
    
    // 验证分组接口
    public interface Add {}
    public interface Update {}
}