package com.lyz;

import com.lyz.service.FrontendStartupService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PreDestroy;

/**
 * 水泥厂管理系统主应用类
 */
@SpringBootApplication(scanBasePackages = "com.lyz")
@MapperScan("com.lyz.mapper")
@EnableScheduling  // 启用定时任务
public class App {
    
    @Autowired
    private FrontendStartupService frontendStartupService;
    
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
    
    /**
     * 应用关闭时清理前端服务
     */
    @PreDestroy
    public void cleanup() {
        if (frontendStartupService != null) {
            frontendStartupService.stopFrontend();
        }
    }

}
