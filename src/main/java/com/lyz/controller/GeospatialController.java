package com.lyz.controller;

import com.lyz.pojo.Result;
import com.lyz.service.impl.TifProcessingService;
import com.lyz.mapper.GeospatialMapper;
import com.lyz.pojo.GeospatialFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 地理数据管理控制器
 */
@RestController
@RequestMapping("/geo")
@Tag(name = "地理数据管理", description = "TIF文件上传、处理和瓦片服务")
public class GeospatialController {
    
    @Autowired
    private TifProcessingService tifProcessingService;
    
    @Autowired
    private GeospatialMapper geospatialMapper;
    
    // 文件上传目录
    private static final String UPLOAD_DIR = "D:/tif-files/";
    
    @PostMapping("/upload-tif")
    @Operation(summary = "上传TIF文件", description = "上传TIF文件并开始处理")
    public Result<Map<String, Object>> uploadTifFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "plantId", required = false) Long plantId,
            @RequestParam(value = "dataType", defaultValue = "NDVI") String dataType) {
        
        try {
            // 1. 保存文件
            String fileName = file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(filePath.getParent());
            file.transferTo(filePath.toFile());
            
            // 2. 创建地理数据文件记录
            GeospatialFile geospatialFile = new GeospatialFile();
            geospatialFile.setFileName(fileName);
            geospatialFile.setFilePath(filePath.toString());
            geospatialFile.setFileType("TIF");
            geospatialFile.setFileSize(file.getSize());
            geospatialFile.setStatus("processing");
            
            geospatialMapper.insertFile(geospatialFile);
            
            // 3. 异步处理TIF文件
            // 这里应该使用异步处理，避免阻塞请求
            try {
                tifProcessingService.processTifFile(geospatialFile.getFileId(), filePath.toString());
            } catch (Exception e) {
                // 处理失败，更新状态
                geospatialFile.setStatus("error");
                geospatialMapper.updateFile(geospatialFile);
                throw e;
            }
            
            // 4. 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("fileId", geospatialFile.getFileId());
            result.put("fileName", fileName);
            result.put("filePath", filePath.toString());
            result.put("status", geospatialFile.getStatus());
            
            return Result.success(result);
            
        } catch (IOException e) {
            return Result.fileSystemError("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.geoserverProcessingError("文件处理失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/tile/{fileId}/{z}/{x}/{y}")
    @Operation(summary = "获取瓦片数据", description = "根据文件ID和瓦片坐标获取瓦片数据")
    public ResponseEntity<byte[]> getTile(
            @PathVariable Long fileId,
            @PathVariable Integer z,
            @PathVariable Integer x,
            @PathVariable Integer y) {
        
        try {
            // 检查文件是否存在
            GeospatialFile file = geospatialMapper.findFileById(fileId);
            if (file == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // 获取瓦片数据
            byte[] tileData = tifProcessingService.getTileData(fileId, z, x, y);
            
            if (tileData != null && tileData.length > 0) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_PNG);
                headers.setContentLength(tileData.length);
                headers.setCacheControl("public, max-age=3600"); // 缓存1小时
                headers.set("Access-Control-Allow-Origin", "*"); // 允许跨域
                
                return new ResponseEntity<>(tileData, headers, HttpStatus.OK);
            } else {
                // 生成空瓦片
                byte[] emptyTile = generateEmptyTile();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_PNG);
                headers.setContentLength(emptyTile.length);
                headers.setCacheControl("public, max-age=300"); // 缓存5分钟
                headers.set("Access-Control-Allow-Origin", "*");
                
                return new ResponseEntity<>(emptyTile, headers, HttpStatus.OK);
            }
            
        } catch (Exception e) {
            System.err.println("获取瓦片失败: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 生成空瓦片
     */
    private byte[] generateEmptyTile() {
        try {
            BufferedImage emptyImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(emptyImage, "PNG", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            // 如果生成空瓦片失败，返回一个简单的1x1像素PNG
            return new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D,
                0x49, 0x48, 0x44, 0x52, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4, (byte) 0x89, 0x00, 0x00, 0x00,
                0x0A, 0x49, 0x44, 0x41, 0x54, 0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
                0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00, 0x00, 0x00, 0x00, 0x49,
                0x45, 0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
            };
        }
    }
    
    @GetMapping("/tif-info/{fileId}")
    @Operation(summary = "获取TIF文件信息", description = "获取TIF文件的详细信息")
    public Result<GeospatialFile> getTifInfo(@PathVariable Long fileId) {
        try {
            GeospatialFile file = geospatialMapper.findFileById(fileId);
            if (file != null) {
                return Result.success(file);
            } else {
                return Result.geoserverResourceNotFound("文件不存在");
            }
        } catch (Exception e) {
            return Result.internalServerError("获取文件信息失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/process-tif/{fileId}")
    @Operation(summary = "处理TIF文件", description = "重新处理指定的TIF文件")
    public Result<Void> processTif(@PathVariable Long fileId) {
        try {
            GeospatialFile file = geospatialMapper.findFileById(fileId);
            if (file == null) {
                return Result.geoserverResourceNotFound("文件不存在");
            }
            
            tifProcessingService.processTifFile(fileId, file.getFilePath());
            return Result.<Void>success();
            
        } catch (Exception e) {
            return Result.geoserverProcessingError("处理TIF文件失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/files")
    @Operation(summary = "获取文件列表", description = "获取所有地理数据文件列表")
    public Result<java.util.List<GeospatialFile>> getFiles(
            @RequestParam(value = "fileType", required = false) String fileType,
            @RequestParam(value = "status", required = false) String status) {
        try {
            return Result.success(geospatialMapper.listFiles(fileType, status));
        } catch (Exception e) {
            return Result.internalServerError("获取文件列表失败: " + e.getMessage());
        }
    }
}
