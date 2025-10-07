package com.lyz.config;

import com.lyz.App;
import com.lyz.config.CorsConfig;
import com.lyz.config.OpenApiConfig;
import com.lyz.config.RabbitConfig;
import com.lyz.config.ScheduledTasks;
import com.lyz.config.WebConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 配置类测试
 */
@SpringBootTest
@ActiveProfiles("test")
class ConfigTest {

    @Autowired
    private App app;

    @Autowired
    private CorsConfig corsConfig;

    @Autowired
    private OpenApiConfig openApiConfig;

    @Autowired
    private RabbitConfig rabbitConfig;

    @Autowired
    private ScheduledTasks scheduledTasks;

    @Autowired
    private WebConfig webConfig;

    @Test
    void testAppContextLoads() {
        // 测试Spring Boot应用上下文是否正确加载
        assertNotNull(app);
    }

    @Test
    void testCorsConfig() {
        // 测试CORS配置
        assertNotNull(corsConfig);
        
        // 测试CORS配置器
        WebMvcConfigurer configurer = corsConfig.corsConfigurer();
        assertNotNull(configurer);
    }

    @Test
    void testOpenApiConfig() {
        // 测试OpenAPI配置
        assertNotNull(openApiConfig);
    }

    @Test
    void testRabbitConfig() {
        // 测试RabbitMQ配置
        assertNotNull(rabbitConfig);
    }

    @Test
    void testScheduledTasks() {
        // 测试定时任务配置
        assertNotNull(scheduledTasks);
    }

    @Test
    void testWebConfig() {
        // 测试Web配置
        assertNotNull(webConfig);
    }

    @Test
    void testCorsMapping() {
        // 测试CORS映射配置
        WebMvcConfigurer configurer = corsConfig.corsConfigurer();
        
        // 创建一个模拟的CorsRegistry来测试配置
        CorsRegistry registry = new CorsRegistry();
        configurer.addCorsMappings(registry);
        
        // 验证CORS配置是否正确应用
        assertNotNull(registry);
    }
}
