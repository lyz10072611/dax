package com.lyz.controller;

import com.lyz.pojo.CementPlantDataDTO;
import com.lyz.pojo.Result;
import com.lyz.service.CementPlantService;
import com.lyz.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据上传控制器
 * 专门处理水泥厂识别数据的JSON格式上传
 */
@RestController
@RequestMapping("/data-upload")
@Validated
@Tag(name = "数据上传", description = "水泥厂识别数据上传接口")
public class DataUploadController {
    
    @Autowired
    private CementPlantService cementPlantService;
    
    @PostMapping("/cement-plant-data")
    @Operation(summary = "上传水泥厂识别数据", description = "接收JSON格式的水泥厂识别数据，自动判断水泥厂是否存在并处理数据存储")
    public Result<String> uploadCementPlantData(@RequestBody @Valid CementPlantDataDTO dataDTO) {
        try {
            // 检查用户权限（只有管理员可以上传数据）
            Map<String, Object> claims = ThreadLocalUtil.get();
            if (claims == null) {
                return Result.unauthorized("用户未登录");
            }
            
            Integer roleCode = (Integer) claims.get("roleCode");
            if (roleCode == null || roleCode != 0) {
                return Result.forbidden("权限不足，只有管理员可以上传数据");
            }
            
            // 调用服务层处理数据
            cementPlantService.addCementPlantData(dataDTO);
            
            return Result.success("水泥厂识别数据上传成功");
            
        } catch (Exception e) {
            return Result.internalServerError("数据上传失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/batch-cement-plant-data")
    @Operation(summary = "批量上传水泥厂识别数据", description = "批量接收JSON格式的水泥厂识别数据")
    public Result<String> batchUploadCementPlantData(@RequestBody @Valid List<CementPlantDataDTO> dataList) {
        try {
            // 检查用户权限
            Map<String, Object> claims = ThreadLocalUtil.get();
            if (claims == null) {
                return Result.unauthorized("用户未登录");
            }
            
            Integer roleCode = (Integer) claims.get("roleCode");
            if (roleCode == null || roleCode != 0) {
                return Result.forbidden("权限不足，只有管理员可以上传数据");
            }
            
            // 批量处理数据
            int successCount = 0;
            int failCount = 0;
            
            for (CementPlantDataDTO dataDTO : dataList) {
                try {
                    cementPlantService.addCementPlantData(dataDTO);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    System.err.println("处理数据失败: " + e.getMessage());
                }
            }
            
            String message = String.format("批量上传完成，成功: %d条，失败: %d条", successCount, failCount);
            return Result.success(message);
            
        } catch (Exception e) {
            return Result.internalServerError("批量数据上传失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/upload-status")
    @Operation(summary = "获取上传状态", description = "获取当前用户的上传权限和状态")
    public Result<Map<String, Object>> getUploadStatus() {
        try {
            Map<String, Object> claims = ThreadLocalUtil.get();
            if (claims == null) {
                return Result.unauthorized("用户未登录");
            }
            
            Integer roleCode = (Integer) claims.get("roleCode");
            boolean canUpload = roleCode != null && roleCode == 0;
            
            Map<String, Object> status = Map.of(
                "canUpload", canUpload,
                "roleCode", roleCode != null ? roleCode : -1,
                "username", claims.get("username")
            );
            
            return Result.success(status);
            
        } catch (Exception e) {
            return Result.internalServerError("获取上传状态失败: " + e.getMessage());
        }
    }
}
