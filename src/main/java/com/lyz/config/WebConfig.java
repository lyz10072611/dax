package com.lyz.config;

import com.lyz.interceptors.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类 - 配置拦截器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private LoginInterceptor loginInterceptor;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // 预检请求缓存时间
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns(
                        // 用户认证相关接口
                        "/user/login",
                        "/user/register",
                        // Vue项目相关路径
                        "/myVue/**",
                        "/assets/**",
                        "/login",
                        "/",
                        "/index.html",
                        // API文档相关
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/webjars/**",
                        "/doc.html",
                        // 错误页面
                        "/error",
                        // 静态资源
                        "/static/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/favicon.ico"
                );
    }
    
    /**
     * 配置静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Vue项目构建后的静态资源映射
        registry.addResourceHandler("/myVue/**")
                .addResourceLocations("file:static/myVue/dist/");
        
        // Vue项目的根路径映射到构建后的index.html
        registry.addResourceHandler("/")
                .addResourceLocations("file:static/myVue/dist/index.html");
        
        // Vue项目的index.html映射
        registry.addResourceHandler("/index.html")
                .addResourceLocations("file:static/myVue/dist/index.html");
        
        // Vue项目的assets目录映射（构建后的资源）
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("file:static/myVue/dist/assets/");
        
        // 其他静态资源
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
