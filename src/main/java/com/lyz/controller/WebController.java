package com.lyz.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web页面控制器
 * 处理页面路由和重定向到Vue项目
 */
@Controller
public class WebController {
    
    /**
     * 根路径重定向到Vue项目主页
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/myVue/index.html";
    }
    
    /**
     * 登录页面重定向到Vue项目的登录页
     */
    @GetMapping("/login")
    public String login() {
        return "redirect:/myVue/index.html#/login";
    }
    
    /**
     * 测试页面
     */
    @GetMapping("/test")
    public String test() {
        return "redirect:/test.html";
    }
}
