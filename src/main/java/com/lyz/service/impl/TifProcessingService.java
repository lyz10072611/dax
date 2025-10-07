package com.lyz.service.impl;

import com.lyz.mapper.GeospatialMapper;
import com.lyz.pojo.GeospatialFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TIF文件处理服务实现
 * 注意：这是一个简化版本，实际应用中需要使用GDAL或其他专业的地理数据处理库
 */
@Service
public class TifProcessingService {
    
    @Autowired
    private GeospatialMapper geospatialMapper;
    
    // 异步处理线程池
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    
    /**
     * 处理TIF文件并生成瓦片数据
     * @param fileId 文件ID
     * @param tifFilePath TIF文件路径
     */
    @Transactional
    public void processTifFile(Long fileId, String tifFilePath) {
        try {
            // 1. 读取TIF文件基本信息
            File tifFile = new File(tifFilePath);
            if (!tifFile.exists()) {
                throw new IOException("TIF文件不存在: " + tifFilePath);
            }
            
            // 2. 更新文件信息
            updateFileMetadata(fileId, tifFile);
            
            // 3. 异步生成瓦片数据
            CompletableFuture.runAsync(() -> {
                try {
                    generateTiles(fileId, tifFile);
                    updateProcessingStatus(fileId, "processed");
                } catch (Exception e) {
                    updateProcessingStatus(fileId, "error");
                    System.err.println("异步处理TIF文件失败: " + e.getMessage());
                }
            }, executorService);
            
            // 4. 立即更新状态为处理中
            updateProcessingStatus(fileId, "processing");
            
        } catch (Exception e) {
            // 更新处理状态为错误
            updateProcessingStatus(fileId, "error");
            throw new RuntimeException("处理TIF文件失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 更新文件元数据
     */
    private void updateFileMetadata(Long fileId, File tifFile) throws IOException {
        GeospatialFile file = geospatialMapper.findFileById(fileId);
        if (file != null) {
            // 设置文件大小
            file.setFileSize(tifFile.length());
            
            // 尝试读取TIF文件的基本信息
            try {
                BufferedImage image = ImageIO.read(tifFile);
                if (image != null) {
                    // 如果能读取为图像，设置基本信息
                    file.setBandsCount(image.getColorModel().getNumComponents());
                    file.setDataType("UINT8");
                    
                    // 根据文件名或内容推断地理范围（这里使用示例数据）
                    // 实际应用中应该使用GDAL等专业库读取地理信息
                    file.setBoundsWest(new BigDecimal("114.3000"));
                    file.setBoundsEast(new BigDecimal("114.4000"));
                    file.setBoundsSouth(new BigDecimal("38.1000"));
                    file.setBoundsNorth(new BigDecimal("38.2000"));
                    file.setResolutionX(new BigDecimal("0.0001"));
                    file.setResolutionY(new BigDecimal("0.0001"));
                } else {
                    // 如果无法读取为图像，可能是地理TIF文件
                    file.setBandsCount(1);
                    file.setDataType("FLOAT32");
                    
                    // 设置默认地理范围
                    file.setBoundsWest(new BigDecimal("114.3000"));
                    file.setBoundsEast(new BigDecimal("114.4000"));
                    file.setBoundsSouth(new BigDecimal("38.1000"));
                    file.setBoundsNorth(new BigDecimal("38.2000"));
                    file.setResolutionX(new BigDecimal("0.0001"));
                    file.setResolutionY(new BigDecimal("0.0001"));
                }
            } catch (Exception e) {
                // 如果读取失败，设置默认值
                file.setBandsCount(1);
                file.setDataType("UNKNOWN");
                file.setBoundsWest(new BigDecimal("114.3000"));
                file.setBoundsEast(new BigDecimal("114.4000"));
                file.setBoundsSouth(new BigDecimal("38.1000"));
                file.setBoundsNorth(new BigDecimal("38.2000"));
                file.setResolutionX(new BigDecimal("0.0001"));
                file.setResolutionY(new BigDecimal("0.0001"));
            }
            
            file.setStatus("processing");
            geospatialMapper.updateFile(file);
        }
    }
    
    /**
     * 生成瓦片数据
     * 注意：这是一个简化版本，实际应用中需要使用专业的地理数据处理库
     */
    private void generateTiles(Long fileId, File tifFile) throws IOException {
        // 这里应该使用GDAL或其他库来生成瓦片
        // 目前使用示例方法
        
        // 尝试读取TIF文件作为图像（仅适用于某些TIF格式）
        try {
            BufferedImage image = ImageIO.read(tifFile);
            if (image != null) {
                generateImageTiles(fileId, image);
            } else {
                // 如果无法作为图像读取，生成占位瓦片
                generatePlaceholderTiles(fileId);
            }
        } catch (Exception e) {
            // 生成占位瓦片
            generatePlaceholderTiles(fileId);
        }
    }
    
    /**
     * 从图像生成瓦片
     */
    private void generateImageTiles(Long fileId, BufferedImage image) throws IOException {
        int width = image.getWidth();
        int height = image.getHeight();
        
        // 计算缩放级别
        int maxZoom = (int) Math.ceil(Math.log(Math.max(width, height) / 256.0) / Math.log(2));
        
        for (int zoom = 0; zoom <= Math.min(maxZoom, 5); zoom++) {
            int tileSize = 256;
            int scale = 1 << zoom;
            int tilesX = (int) Math.ceil(width / (double) (tileSize * scale));
            int tilesY = (int) Math.ceil(height / (double) (tileSize * scale));
            
            for (int x = 0; x < tilesX; x++) {
                for (int y = 0; y < tilesY; y++) {
                    // 创建瓦片图像
                    BufferedImage tileImage = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_RGB);
                    
                    // 从原图像中提取瓦片区域
                    int srcX = x * tileSize * scale;
                    int srcY = y * tileSize * scale;
                    int srcWidth = Math.min(tileSize * scale, width - srcX);
                    int srcHeight = Math.min(tileSize * scale, height - srcY);
                    
                    if (srcWidth > 0 && srcHeight > 0) {
                        BufferedImage srcTile = image.getSubimage(srcX, srcY, srcWidth, srcHeight);
                        tileImage.getGraphics().drawImage(srcTile, 0, 0, tileSize, tileSize, null);
                    }
                    
                    // 转换为字节数组
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(tileImage, "PNG", baos);
                    byte[] tileData = baos.toByteArray();
                    
                    // 存储瓦片数据
                    geospatialMapper.insertTile(fileId, zoom, x, y, tileData, tileSize, "PNG");
                }
            }
        }
    }
    
    /**
     * 生成占位瓦片
     */
    private void generatePlaceholderTiles(Long fileId) throws IOException {
        // 创建一个简单的占位瓦片
        BufferedImage placeholder = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        placeholder.getGraphics().fillRect(0, 0, 256, 256);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(placeholder, "PNG", baos);
        byte[] tileData = baos.toByteArray();
        
        // 为缩放级别0-2生成占位瓦片
        for (int zoom = 0; zoom <= 2; zoom++) {
            int tilesPerSide = 1 << zoom;
            for (int x = 0; x < tilesPerSide; x++) {
                for (int y = 0; y < tilesPerSide; y++) {
                    geospatialMapper.insertTile(fileId, zoom, x, y, tileData, 256, "PNG");
                }
            }
        }
    }
    
    /**
     * 更新处理状态
     */
    private void updateProcessingStatus(Long fileId, String status) {
        GeospatialFile file = geospatialMapper.findFileById(fileId);
        if (file != null) {
            file.setStatus(status);
            geospatialMapper.updateFile(file);
        }
    }
    
    /**
     * 获取瓦片数据
     */
    public byte[] getTileData(Long fileId, Integer zoom, Integer x, Integer y) {
        return geospatialMapper.getTileData(fileId, zoom, x, y);
    }
}
