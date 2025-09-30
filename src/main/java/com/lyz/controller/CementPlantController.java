package com.lyz.controller;

import com.lyz.pojo.CementPlant;
import com.lyz.pojo.CementPlantIdentification;
import com.lyz.pojo.CementPlantDataDTO;
import com.lyz.pojo.PageBean;
import com.lyz.pojo.Result;
import com.lyz.service.CementPlantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 水泥厂管理控制器
 */
@RestController
@RequestMapping("/cement-plant")
@Validated
@Tag(name = "水泥厂管理", description = "水泥厂基本信息和识别记录管理")
@CrossOrigin(origins = "http://localhost:5173")
public class CementPlantController {
    
    @Autowired
    private CementPlantService cementPlantService;
    
    // ==================== 水泥厂基本信息管理 ====================
    
    @GetMapping("/{plantId}")
    @Operation(summary = "根据ID查询水泥厂", description = "根据水泥厂ID获取详细信息")
    public Result<CementPlant> getPlantById(@PathVariable @NotNull Long plantId) {
        CementPlant plant = cementPlantService.findById(plantId);
        if (plant != null) {
            return Result.success(plant);
        }
        return Result.error("水泥厂不存在");
    }
    
    @GetMapping("/list")
    @Operation(summary = "查询水泥厂列表", description = "查询所有水泥厂")
    public Result<List<CementPlant>> listPlants() {
        List<CementPlant> plants = cementPlantService.listPlants();
        return Result.success(plants);
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询水泥厂", description = "分页查询水泥厂列表")
    public Result<PageBean<CementPlant>> pagePlants(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String plantName,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String status) {
        PageBean<CementPlant> pageBean = cementPlantService.pagePlants(pageNum, pageSize, plantName, province, city, status);
        return Result.success(pageBean);
    }
    
    @PostMapping
    @Operation(summary = "新增水泥厂", description = "添加新的水泥厂信息")
    public Result addPlant(@RequestBody @Valid CementPlant cementPlant) {
        // 检查名称是否已存在
        CementPlant existingPlant = cementPlantService.findByName(cementPlant.getPlantName());
        if (existingPlant != null) {
            return Result.error("水泥厂名称已存在");
        }
        
        cementPlantService.addPlant(cementPlant);
        return Result.success("水泥厂添加成功");
    }
    
    @PutMapping
    @Operation(summary = "更新水泥厂", description = "更新水泥厂信息")
    public Result updatePlant(@RequestBody @Valid CementPlant cementPlant) {
        CementPlant existingPlant = cementPlantService.findById(cementPlant.getPlantId());
        if (existingPlant == null) {
            return Result.error("水泥厂不存在");
        }
        
        cementPlantService.updatePlant(cementPlant);
        return Result.success("水泥厂更新成功");
    }
    
    @DeleteMapping("/{plantId}")
    @Operation(summary = "删除水泥厂", description = "根据ID删除水泥厂")
    public Result deletePlant(@PathVariable @NotNull Long plantId) {
        CementPlant existingPlant = cementPlantService.findById(plantId);
        if (existingPlant == null) {
            return Result.error("水泥厂不存在");
        }
        
        cementPlantService.deletePlant(plantId);
        return Result.success("水泥厂删除成功");
    }
    
    @GetMapping("/location-range")
    @Operation(summary = "按地理位置范围查询", description = "根据经纬度范围查询水泥厂")
    public Result<List<CementPlant>> findPlantsByLocationRange(
            @RequestParam Double minLng,
            @RequestParam Double maxLng,
            @RequestParam Double minLat,
            @RequestParam Double maxLat) {
        List<CementPlant> plants = cementPlantService.findPlantsByLocationRange(minLng, maxLng, minLat, maxLat);
        return Result.success(plants);
    }
    
    // ==================== 水泥厂识别记录管理 ====================
    
    @GetMapping("/identification/{identificationId}")
    @Operation(summary = "根据ID查询识别记录", description = "根据识别记录ID获取详细信息")
    public Result<CementPlantIdentification> getIdentificationById(@PathVariable @NotNull Long identificationId) {
        CementPlantIdentification identification = cementPlantService.findIdentificationById(identificationId);
        if (identification != null) {
            return Result.success(identification);
        }
        return Result.error("识别记录不存在");
    }
    
    @GetMapping("/identification/list")
    @Operation(summary = "查询识别记录列表", description = "查询所有识别记录")
    public Result<List<CementPlantIdentification>> listIdentifications() {
        List<CementPlantIdentification> identifications = cementPlantService.listIdentifications();
        return Result.success(identifications);
    }
    
    @GetMapping("/identification/page")
    @Operation(summary = "分页查询识别记录", description = "分页查询识别记录列表")
    public Result<PageBean<CementPlantIdentification>> pageIdentifications(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long plantId,
            @RequestParam(required = false) String dataSource,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        PageBean<CementPlantIdentification> pageBean = cementPlantService.pageIdentifications(pageNum, pageSize, plantId, dataSource, startTime, endTime);
        return Result.success(pageBean);
    }
    
    @PostMapping("/identification")
    @Operation(summary = "新增识别记录", description = "添加新的水泥厂识别记录")
    public Result addIdentification(@RequestBody @Valid CementPlantIdentification identification) {
        // 检查关联的水泥厂是否存在
        CementPlant plant = cementPlantService.findById(identification.getPlantId());
        if (plant == null) {
            return Result.error("关联的水泥厂不存在");
        }
        
        cementPlantService.addIdentification(identification);
        return Result.success("识别记录添加成功");
    }
    
    @PutMapping("/identification")
    @Operation(summary = "更新识别记录", description = "更新识别记录信息")
    public Result updateIdentification(@RequestBody @Valid CementPlantIdentification identification) {
        CementPlantIdentification existingIdentification = cementPlantService.findIdentificationById(identification.getIdentificationId());
        if (existingIdentification == null) {
            return Result.error("识别记录不存在");
        }
        
        cementPlantService.updateIdentification(identification);
        return Result.success("识别记录更新成功");
    }
    
    @DeleteMapping("/identification/{identificationId}")
    @Operation(summary = "删除识别记录", description = "根据ID删除识别记录")
    public Result deleteIdentification(@PathVariable @NotNull Long identificationId) {
        CementPlantIdentification existingIdentification = cementPlantService.findIdentificationById(identificationId);
        if (existingIdentification == null) {
            return Result.error("识别记录不存在");
        }
        
        cementPlantService.deleteIdentification(identificationId);
        return Result.success("识别记录删除成功");
    }
    
    // ==================== 统计信息 ====================
    
    @GetMapping("/stats/active-count")
    @Operation(summary = "获取活跃水泥厂数量", description = "统计状态为active的水泥厂数量")
    public Result<Integer> getActivePlantsCount() {
        Integer count = cementPlantService.countActivePlants();
        return Result.success(count);
    }
    
    @GetMapping("/stats/identification-count/{plantId}")
    @Operation(summary = "获取水泥厂识别记录数量", description = "统计指定水泥厂的识别记录数量")
    public Result<Integer> getIdentificationCountByPlantId(@PathVariable @NotNull Long plantId) {
        Integer count = cementPlantService.countIdentificationsByPlantId(plantId);
        return Result.success(count);
    }
    
    @GetMapping("/stats/identification-count-by-time")
    @Operation(summary = "按时间范围获取识别记录数量", description = "统计指定时间范围内的识别记录数量")
    public Result<Integer> getIdentificationCountByTimeRange(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        Integer count = cementPlantService.countIdentificationsByTimeRange(startTime, endTime);
        return Result.success(count);
    }
    
    // ==================== 批量数据新增 ====================
    
    @PostMapping("/batch-data")
    @Operation(summary = "批量新增水泥厂数据", description = "接收JSON数据，根据名称判断是否存在，自动处理水泥厂和识别记录的插入")
    public Result addCementPlantData(@RequestBody @Valid CementPlantDataDTO dataDTO) {
        try {
            cementPlantService.addCementPlantData(dataDTO);
            return Result.success("水泥厂数据添加成功");
        } catch (Exception e) {
            return Result.error("数据添加失败: " + e.getMessage());
        }
    }
}

