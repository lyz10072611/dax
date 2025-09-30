package com.lyz.interceptors;

import com.lyz.enums.ResultCode;
import com.lyz.pojo.Result;
import com.lyz.utils.JwtUtil;
import com.lyz.utils.ThreadLocalUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

/**
 * 登录拦截器 - 修复了token鉴权bug，正确处理未授权访问
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求头中的token
        String token = request.getHeader("Authorization");
        
        // 检查token是否存在
        if (token == null || token.trim().isEmpty()) {
            handleUnauthorized(request, response, "缺少访问令牌");
            return false;
        }
        
        try {
            // 从redis中验证token是否存在
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            String redisToken = operations.get(token);
            if (redisToken == null) {
                handleUnauthorized(request, response, "访问令牌已过期或无效");
                return false;
            }
            
            // 解析token获取用户信息
            Map<String, Object> claims = JwtUtil.parseToken(token);
            if (claims == null || claims.isEmpty()) {
                handleUnauthorized(request, response, "访问令牌格式错误");
                return false;
            }
            
            // 将用户信息存储到ThreadLocal中，供后续使用
            ThreadLocalUtil.set(claims);
            
            // 放行
            return true;
            
        } catch (Exception e) {
            // 记录日志（可选）
            System.err.println("Token验证失败: " + e.getMessage());
            handleUnauthorized(request, response, "访问令牌验证失败");
            return false;
        }
    }
    
    /**
     * 处理未授权访问的情况
     * 根据请求类型决定返回JSON错误还是重定向到登录页
     */
    private void handleUnauthorized(HttpServletRequest request, HttpServletResponse response, String message) throws Exception {
        String accept = request.getHeader("Accept");
        String contentType = request.getHeader("Content-Type");
        
        // 判断是否为API请求（JSON请求）
        boolean isApiRequest = (accept != null && accept.contains("application/json")) ||
                              (contentType != null && contentType.contains("application/json")) ||
                              request.getRequestURI().startsWith("/api/") ||
                              request.getRequestURI().startsWith("/user/") ||
                              request.getRequestURI().startsWith("/admin/") ||
                              request.getRequestURI().startsWith("/files/") ||
                              request.getRequestURI().startsWith("/pollution/");
        
        if (isApiRequest) {
            // API请求返回JSON错误
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            
            // 使用Result类统一响应格式
            Result<Void> errorResult = Result.error(ResultCode.UNAUTHORIZED, message);
            ObjectMapper objectMapper = new ObjectMapper();
            String errorResponse = objectMapper.writeValueAsString(errorResult);
            
            response.getWriter().write(errorResponse);
            response.getWriter().flush();
        } else {
            // 页面请求重定向到登录页
            response.sendRedirect("/login");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理ThreadLocal，防止内存泄漏
        ThreadLocalUtil.remove();
    }
}
