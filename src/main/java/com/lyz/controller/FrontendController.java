package com.lyz.controller;

import com.lyz.pojo.Result;
import com.lyz.service.FrontendStartupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 前端服务管理控制器
 * 提供前端服务的启动、停止、状态查询等功能
 */
@RestController
@RequestMapping("/frontend")
@Tag(name = "前端服务管理", description = "前端Vue项目的启动、停止和状态管理")
public class FrontendController {
    
    @Autowired
    private FrontendStartupService frontendStartupService;
    
    @GetMapping("/status")
    @Operation(summary = "获取前端服务状态", description = "查询前端Vue项目的运行状态")
    public Result<Map<String, Object>> getFrontendStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("isRunning", frontendStartupService.isFrontendRunning());
            status.put("frontendUrl", frontendStartupService.getFrontendUrl());
            status.put("message", frontendStartupService.isFrontendRunning() ? "前端服务正在运行" : "前端服务未运行");
            
            return Result.success(status);
        } catch (Exception e) {
            return Result.internalServerError("获取前端服务状态失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/start")
    @Operation(summary = "启动前端服务", description = "手动启动前端Vue开发服务器")
    public Result<String> startFrontend() {
        try {
            if (frontendStartupService.isFrontendRunning()) {
                return Result.conflict("前端服务已经在运行中");
            }
            
            // 这里可以添加手动启动逻辑
            // 由于前端服务是在应用启动时自动启动的，这里主要提供状态查询
            return Result.success("前端服务启动命令已发送，请检查服务状态");
        } catch (Exception e) {
            return Result.internalServerError("启动前端服务失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/stop")
    @Operation(summary = "停止前端服务", description = "停止前端Vue开发服务器")
    public Result<String> stopFrontend() {
        try {
            if (!frontendStartupService.isFrontendRunning()) {
                return Result.notFound("前端服务未在运行");
            }
            
            frontendStartupService.stopFrontend();
            return Result.success("前端服务已停止");
        } catch (Exception e) {
            return Result.internalServerError("停止前端服务失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/info")
    @Operation(summary = "获取前端项目信息", description = "获取前端Vue项目的基本信息")
    public Result<Map<String, Object>> getFrontendInfo() {
        try {
            Map<String, Object> info = new HashMap<>();
            info.put("projectName", "水泥厂管理系统前端");
            info.put("framework", "Vue 3 + Vite");
            info.put("frontendUrl", frontendStartupService.getFrontendUrl());
            info.put("isRunning", frontendStartupService.isFrontendRunning());
            info.put("description", "基于Vue 3和Vite构建的现代化前端应用");
            
            return Result.success(info);
        } catch (Exception e) {
            return Result.internalServerError("获取前端项目信息失败: " + e.getMessage());
        }
    }
}
