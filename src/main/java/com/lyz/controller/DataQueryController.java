package com.lyz.controller;

import com.lyz.pojo.CementPlant;
import com.lyz.pojo.CementPlantIdentification;
import com.lyz.pojo.PageBean;
import com.lyz.pojo.Result;
import com.lyz.service.CementPlantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据查询控制器
 * 提供高级查询功能：模糊匹配、行政区划查询、经纬度查询等
 */
@RestController
@RequestMapping("/data-query")
@Validated
@Tag(name = "数据查询", description = "水泥厂和识别记录的高级查询接口")
public class DataQueryController {
    
    @Autowired
    private CementPlantService cementPlantService;
    
    // ==================== 水泥厂查询 ====================
    
    @GetMapping("/cement-plants/search")
    @Operation(summary = "水泥厂模糊搜索", description = "根据水泥厂名称进行模糊匹配查询")
    public Result<List<CementPlant>> searchCementPlantsByName(
            @RequestParam String plantName,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<CementPlant> plants = cementPlantService.searchPlantsByName(plantName, limit);
            return Result.success(plants);
        } catch (Exception e) {
            return Result.internalServerError("查询失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/cement-plants/by-region")
    @Operation(summary = "按行政区划查询水泥厂", description = "根据省市区查询水泥厂")
    public Result<List<CementPlant>> getCementPlantsByRegion(
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district) {
        try {
            List<CementPlant> plants = cementPlantService.findPlantsByRegion(province, city, district);
            return Result.success(plants);
        } catch (Exception e) {
            return Result.internalServerError("查询失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/cement-plants/by-location")
    @Operation(summary = "按地理位置查询水泥厂", description = "根据经纬度范围查询水泥厂")
    public Result<List<CementPlant>> getCementPlantsByLocation(
            @RequestParam Double longitude,
            @RequestParam Double latitude,
            @RequestParam(defaultValue = "10.0") Double radiusKm) {
        try {
            List<CementPlant> plants = cementPlantService.findPlantsByLocation(longitude, latitude, radiusKm);
            return Result.success(plants);
        } catch (Exception e) {
            return Result.internalServerError("查询失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/cement-plants/by-status")
    @Operation(summary = "按状态查询水泥厂", description = "根据运行状态查询水泥厂")
    public Result<List<CementPlant>> getCementPlantsByStatus(
            @RequestParam String status) {
        try {
            List<CementPlant> plants = cementPlantService.findPlantsByStatus(status);
            return Result.success(plants);
        } catch (Exception e) {
            return Result.internalServerError("查询失败: " + e.getMessage());
        }
    }
    
    // ==================== 识别记录查询 ====================
    
    @GetMapping("/identifications/by-plant")
    @Operation(summary = "查询水泥厂的识别记录", description = "根据水泥厂ID查询所有识别记录")
    public Result<List<CementPlantIdentification>> getIdentificationsByPlant(
            @RequestParam Long plantId) {
        try {
            List<CementPlantIdentification> identifications = 
                cementPlantService.findIdentificationsByPlantId(plantId);
            return Result.success(identifications);
        } catch (Exception e) {
            return Result.internalServerError("查询失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/identifications/latest")
    @Operation(summary = "获取最新识别记录", description = "获取指定水泥厂的最新识别记录")
    public Result<CementPlantIdentification> getLatestIdentification(
            @RequestParam Long plantId) {
        try {
            CementPlantIdentification identification = 
                cementPlantService.findLatestIdentificationByPlantId(plantId);
            if (identification != null) {
                return Result.success(identification);
            } else {
                return Result.notFound("未找到识别记录");
            }
        } catch (Exception e) {
            return Result.internalServerError("查询失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/identifications/by-time-range")
    @Operation(summary = "按时间范围查询识别记录", description = "根据时间范围查询识别记录")
    public Result<List<CementPlantIdentification>> getIdentificationsByTimeRange(
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(required = false) Long plantId) {
        try {
            List<CementPlantIdentification> identifications = 
                cementPlantService.findIdentificationsByTimeRange(startTime, endTime, plantId);
            return Result.success(identifications);
        } catch (Exception e) {
            return Result.internalServerError("查询失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/identifications/by-ndvi-range")
    @Operation(summary = "按NDVI范围查询识别记录", description = "根据NDVI指数范围查询识别记录")
    public Result<List<CementPlantIdentification>> getIdentificationsByNdviRange(
            @RequestParam Float minNdvi,
            @RequestParam Float maxNdvi) {
        try {
            List<CementPlantIdentification> identifications = 
                cementPlantService.findIdentificationsByNdviRange(minNdvi, maxNdvi);
            return Result.success(identifications);
        } catch (Exception e) {
            return Result.internalServerError("查询失败: " + e.getMessage());
        }
    }
    
    // ==================== 综合查询 ====================
    
    @GetMapping("/comprehensive-search")
    @Operation(summary = "综合搜索", description = "支持多条件组合查询")
    public Result<PageBean<CementPlantIdentification>> comprehensiveSearch(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String plantName,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Float minNdvi,
            @RequestParam(required = false) Float maxNdvi,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double radiusKm) {
        try {
            PageBean<CementPlantIdentification> result = cementPlantService.comprehensiveSearch(
                pageNum, pageSize, plantName, province, city, district, status,
                startTime, endTime, minNdvi, maxNdvi, longitude, latitude, radiusKm);
            return Result.success(result);
        } catch (Exception e) {
            return Result.internalServerError("查询失败: " + e.getMessage());
        }
    }
    
    // ==================== 统计查询 ====================
    
    @GetMapping("/statistics/overview")
    @Operation(summary = "获取统计概览", description = "获取系统整体统计数据")
    public Result<Map<String, Object>> getStatisticsOverview() {
        try {
            Map<String, Object> statistics = cementPlantService.getStatisticsOverview();
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.internalServerError("获取统计信息失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/statistics/by-region")
    @Operation(summary = "按地区统计", description = "获取各省市的水泥厂和识别记录统计")
    public Result<Map<String, Object>> getStatisticsByRegion() {
        try {
            Map<String, Object> statistics = cementPlantService.getStatisticsByRegion();
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.internalServerError("获取地区统计信息失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/statistics/by-time")
    @Operation(summary = "按时间统计", description = "获取按时间维度的统计数据")
    public Result<Map<String, Object>> getStatisticsByTime(
            @RequestParam(defaultValue = "month") String timeUnit) {
        try {
            Map<String, Object> statistics = cementPlantService.getStatisticsByTime(timeUnit);
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.internalServerError("获取时间统计信息失败: " + e.getMessage());
        }
    }
}
