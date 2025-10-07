package com.lyz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

/**
 * 前端Vue项目自动启动服务
 * 在Spring Boot应用启动完成后自动启动前端开发服务器
 */
@Service
public class FrontendStartupService {
    
    private static final Logger log = LoggerFactory.getLogger(FrontendStartupService.class);
    
    @Value("${frontend.auto-start:true}")
    private boolean autoStart;
    
    @Value("${frontend.port:5173}")
    private int frontendPort;
    
    @Value("${frontend.path:static/myVue}")
    private String frontendPath;
    
    @Value("${frontend.open-browser:true}")
    private boolean openBrowser;
    
    private Process frontendProcess;
    
    /**
     * 应用启动完成后自动启动前端
     */
    @EventListener(ApplicationReadyEvent.class)
    public void startFrontend() {
        if (!autoStart) {
            log.info("前端自动启动已禁用");
            return;
        }
        
        // 异步启动前端，避免阻塞主线程
        CompletableFuture.runAsync(() -> {
            try {
                startVueDevServer();
                if (openBrowser) {
                    openBrowser();
                }
            } catch (Exception e) {
                log.error("启动前端服务失败", e);
            }
        });
    }
    
    /**
     * 启动Vue开发服务器
     */
    private void startVueDevServer() throws IOException {
        // 获取前端项目路径
        String projectRoot = System.getProperty("user.dir");
        String frontendDir = Paths.get(projectRoot, frontendPath).toString();
        
        File frontendDirFile = new File(frontendDir);
        if (!frontendDirFile.exists()) {
            log.error("前端项目目录不存在: {}", frontendDir);
            return;
        }
        
        // 检查package.json是否存在
        File packageJson = new File(frontendDirFile, "package.json");
        if (!packageJson.exists()) {
            log.error("package.json文件不存在: {}", packageJson.getAbsolutePath());
            return;
        }
        
        log.info("正在启动前端Vue项目...");
        log.info("前端项目路径: {}", frontendDir);
        log.info("前端端口: {}", frontendPort);
        
        // 构建启动命令
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(frontendDirFile);
        
        // 根据操作系统选择命令
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            // Windows系统
            processBuilder.command("cmd", "/c", "npm", "run", "dev", "--", "--port", String.valueOf(frontendPort), "--host", "0.0.0.0");
        } else {
            // Linux/Mac系统
            processBuilder.command("npm", "run", "dev", "--", "--port", String.valueOf(frontendPort), "--host", "0.0.0.0");
        }
        
        // 设置环境变量
        processBuilder.environment().put("NODE_ENV", "development");
        
        // 启动进程
        frontendProcess = processBuilder.start();
        
        // 启动日志输出线程
        startLogThread(frontendProcess);
        
        log.info("前端Vue项目启动命令已执行");
        log.info("前端访问地址: http://localhost:{}", frontendPort);
    }
    
    /**
     * 启动日志输出线程
     */
    private void startLogThread(Process process) {
        CompletableFuture.runAsync(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("[Vue Dev Server] {}", line);
                    
                    // 检查是否启动成功
                    if (line.contains("Local:") || line.contains("ready in")) {
                        log.info("前端Vue项目启动成功！");
                    }
                }
            } catch (IOException e) {
                log.error("读取前端进程输出失败", e);
            }
        });
        
        // 错误输出线程
        CompletableFuture.runAsync(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.error("[Vue Dev Server Error] {}", line);
                }
            } catch (IOException e) {
                log.error("读取前端进程错误输出失败", e);
            }
        });
    }
    
    /**
     * 自动打开浏览器
     */
    private void openBrowser() {
        try {
            // 等待前端服务启动
            Thread.sleep(3000);
            
            String url = "http://localhost:" + frontendPort;
            log.info("正在打开浏览器: {}", url);
            
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb = new ProcessBuilder();
            
            if (os.contains("windows")) {
                pb.command("rundll32", "url.dll,FileProtocolHandler", url);
            } else if (os.contains("mac")) {
                pb.command("open", url);
            } else if (os.contains("nix") || os.contains("nux")) {
                pb.command("xdg-open", url);
            }
            
            pb.start();
            log.info("浏览器已打开: {}", url);
        } catch (Exception e) {
            log.error("打开浏览器失败", e);
        }
    }
    
    /**
     * 停止前端服务
     */
    public void stopFrontend() {
        if (frontendProcess != null && frontendProcess.isAlive()) {
            log.info("正在停止前端服务...");
            frontendProcess.destroy();
            try {
                if (frontendProcess.waitFor() != 0) {
                    frontendProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                frontendProcess.destroyForcibly();
            }
            log.info("前端服务已停止");
        }
    }
    
    /**
     * 检查前端服务是否运行
     */
    public boolean isFrontendRunning() {
        return frontendProcess != null && frontendProcess.isAlive();
    }
    
    /**
     * 获取前端服务URL
     */
    public String getFrontendUrl() {
        return "http://localhost:" + frontendPort;
    }
}
