package com.lyz.controller;

import com.lyz.pojo.Result;
import com.lyz.pojo.User;
import com.lyz.service.UserService;
import com.lyz.utils.JwtUtil;
import com.lyz.utils.Md5Util;
import com.lyz.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户管理控制器
 * 提供用户注册、登录、权限管理等功能
 */
@RestController
@RequestMapping("/user")
@Validated
@Tag(name = "用户管理", description = "用户注册、登录、权限管理")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户，邮箱为必填项")
    public ResponseEntity<Result<Map<String, Object>>> register(
            @Pattern(regexp = "^\\S{5,16}$", message = "用户名长度应为5-16位，不能包含空格") String username, 
            @Pattern(regexp = "^\\S{5,16}$", message = "密码长度应为5-16位，不能包含空格") String password,
            @RequestParam(required = true) String email,
            @RequestParam(required = false) String phone) {
        
        // 验证邮箱格式
        if (!StringUtils.hasText(email)) {
            return ResponseEntity.ok(Result.badRequest("邮箱不能为空"));
        }
        
        // 查询用户是否已存在
        User existingUser = userService.findByUserName(username);
        if (existingUser != null) {
            return ResponseEntity.ok(Result.conflict("用户名已被占用"));
        }
        
        // 检查邮箱是否已存在
        User existingEmail = userService.findByEmail(email);
        if (existingEmail != null) {
            return ResponseEntity.ok(Result.conflict("邮箱已被占用"));
        }
        
        // 注册用户
        User newUser = userService.register(username, password, email, phone);
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", newUser.getId());
        result.put("username", newUser.getUsername());
        result.put("email", newUser.getEmail());
        result.put("roleCode", 1); // 默认普通用户
        
        return ResponseEntity.ok(Result.success(result, "注册成功"));
    }
    
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "根据用户名与密码登录，返回 JWT Token")
    public Result<Map<String, Object>> login(
            @Pattern(regexp = "^\\S{5,16}$", message = "用户名长度应为5-16位") String username, 
            @Pattern(regexp = "^\\S{5,16}$", message = "密码长度应为5-16位") String password) {
        
        try {
            // 检查登录频率限制
            String loginAttemptKey = "login_attempt:" + username;
            String attemptCount = stringRedisTemplate.opsForValue().get(loginAttemptKey);
            if (attemptCount != null && Integer.parseInt(attemptCount) >= 5) {
                return Result.tooManyRequests("登录失败次数过多，请15分钟后重试");
            }
            
            // 根据username查询用户是否存在
            User loginUser = userService.findByUserName(username);
            if (loginUser == null) {
                incrementLoginAttempt(username);
                return Result.notFound("用户名不存在");
            }
            
            // 检查用户状态
            if (loginUser.getStatus() != null && loginUser.getStatus() == 0) {
                return Result.forbidden("账户已被禁用，请联系管理员");
            }
            
            // 判断密码是否匹配
            if (Md5Util.getMD5String(password).equals(loginUser.getPassword())) {
                // 登录成功，清除失败计数
                stringRedisTemplate.delete(loginAttemptKey);
                
                // 生成JWT Token
                Map<String, Object> claims = new HashMap<>();
                claims.put("username", loginUser.getUsername());
                claims.put("id", loginUser.getId());
                claims.put("email", loginUser.getEmail());
                
                Integer roleCode = userService.findRoleCodeByUsername(username);
                claims.put("roleCode", roleCode);
                
                String token = JwtUtil.genToken(claims);
                log.info("Login success. username={}, roleCode={}", username, roleCode);
                
                // 更新最后登录时间
                userService.updateLastLoginTime(loginUser.getId());
                
                // 将token存储到Redis中，设置过期时间
                ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
                operations.set(token, token, 24, TimeUnit.HOURS); // 24小时过期
                
                // 存储用户会话信息
                String sessionKey = "user_session:" + loginUser.getId();
                Map<String, Object> sessionInfo = new HashMap<>();
                sessionInfo.put("userId", loginUser.getId());
                sessionInfo.put("username", loginUser.getUsername());
                sessionInfo.put("roleCode", roleCode);
                sessionInfo.put("loginTime", System.currentTimeMillis());
                operations.set(sessionKey, sessionInfo.toString(), 24, TimeUnit.HOURS);
                
                Map<String, Object> result = new HashMap<>();
                result.put("token", token);
                result.put("username", loginUser.getUsername());
                result.put("id", loginUser.getId());
                result.put("email", loginUser.getEmail());
                result.put("roleCode", roleCode);
                result.put("expiresIn", 24 * 3600); // 24小时，单位秒
                
                return Result.success(result, "登录成功");
            } else {
                incrementLoginAttempt(username);
                return Result.unauthorized("密码错误");
            }
            
        } catch (Exception e) {
            log.error("用户登录失败", e);
            incrementLoginAttempt(username);
            return Result.internalServerError("登录失败: " + e.getMessage());
        }
    }

    @GetMapping("/userInfo")
    @Operation(summary = "获取用户信息", description = "根据 Token 获取当前用户信息")
    public Result<User> userinfo() {
        try {
            Map<String, Object> map = ThreadLocalUtil.get();
            if (map == null) {
                return Result.unauthorized("用户未登录");
            }
            
            String username = (String) map.get("username");
            User user = userService.findByUserName(username);
            
            if (user != null) {
                // 不返回密码信息
                user.setPassword(null);
                return Result.success(user);
            } else {
                return Result.notFound("用户不存在");
            }
            
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.internalServerError("获取用户信息失败");
        }
    }
    
    @PutMapping("/update")
    @Operation(summary = "更新用户信息", description = "更新当前用户的基本信息")
    public Result<Void> update(@RequestBody @Validated User user) {
        try {
            Map<String, Object> map = ThreadLocalUtil.get();
            if (map == null) {
                return Result.unauthorized("用户未登录");
            }
            
            Integer currentUserId = (Integer) map.get("id");
            user.setId(currentUserId);
            
            userService.update(user);
            return Result.success(null, "更新成功");
            
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return Result.internalServerError("更新失败: " + e.getMessage());
        }
    }
    
    @PatchMapping("/updateAvatar")
    @Operation(summary = "更新头像", description = "更新用户头像URL")
    public Result<Void> updateAvatar(
            @RequestParam @Pattern(regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", message = "头像URL格式不正确") String avatarUrl) {
        try {
            userService.updateAvatar(avatarUrl);
            return Result.success(null, "头像更新成功");
        } catch (Exception e) {
            log.error("更新头像失败", e);
            return Result.internalServerError("更新头像失败: " + e.getMessage());
        }
    }
    
    @PatchMapping("/updatePwd")
    @Operation(summary = "修改密码", description = "修改用户密码")
    public Result<Void> updatePwd(@RequestBody Map<String, String> params, @RequestHeader("Authorization") String token) {
        try {
            // 校验参数
            String oldPwd = params.get("oldPwd");
            String newPwd = params.get("newPwd");
            String rePwd = params.get("rePwd");
            
            if (!StringUtils.hasLength(oldPwd) || !StringUtils.hasLength(newPwd) || !StringUtils.hasLength(rePwd)) {
                return Result.badRequest("缺少必要参数");
            }

            // 判断旧密码是否正确
            Map<String, Object> map = ThreadLocalUtil.get();
            if (map == null) {
                return Result.unauthorized("用户未登录");
            }
            
            String username = (String) map.get("username");
            User loginUser = userService.findByUserName(username);
            if (!loginUser.getPassword().equals(Md5Util.getMD5String(oldPwd))) {
                return Result.unauthorized("原密码不正确");
            }
            
            if (!rePwd.equals(newPwd)) {
                return Result.badRequest("两次输入的密码不同");
            }
            
            if (oldPwd.equals(newPwd)) {
                return Result.badRequest("新修改的密码不能与旧密码相同");
            }
            
            // 密码强度检查
            if (newPwd.length() < 6) {
                return Result.badRequest("新密码长度不能少于6位");
            }
            
            userService.updatePwd(newPwd);
            
            // 删除Redis中的token，强制重新登录
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            operations.getOperations().delete(token);
            
            return Result.success(null, "密码修改成功，请重新登录");

        } catch (Exception e) {
            log.error("修改密码失败", e);
            return Result.internalServerError("修改密码失败: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出，清除Token")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        try {
            // 从Redis中删除token
            stringRedisTemplate.delete(token);
            
            // 清除用户会话信息
            Map<String, Object> map = ThreadLocalUtil.get();
            if (map != null) {
                Integer userId = (Integer) map.get("id");
                String sessionKey = "user_session:" + userId;
                stringRedisTemplate.delete(sessionKey);
            }
            
            return Result.success(null, "登出成功");
            
        } catch (Exception e) {
            log.error("用户登出失败", e);
            return Result.internalServerError("登出失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/permissions")
    @Operation(summary = "获取用户权限", description = "获取当前用户的权限列表")
    public Result<Map<String, Object>> getPermissions() {
        try {
            Map<String, Object> map = ThreadLocalUtil.get();
            if (map == null) {
                return Result.unauthorized("用户未登录");
            }
            
            Integer roleCode = (Integer) map.get("roleCode");
            Map<String, Object> permissions = userService.getUserPermissions(roleCode);
            
            return Result.success(permissions);
            
        } catch (Exception e) {
            log.error("获取用户权限失败", e);
            return Result.internalServerError("获取用户权限失败: " + e.getMessage());
        }
    }

    // ==================== 下载计数管理 ====================
    
    @GetMapping("/daily-download-count")
    @Operation(summary = "获取用户每日下载计数", description = "获取当前用户今日已下载次数")
    public Result<Integer> getDailyDownloadCount() {
        try {
            Map<String, Object> map = ThreadLocalUtil.get();
            if (map == null) {
                return Result.unauthorized("用户未登录");
            }
            
            Integer userId = (Integer) map.get("id");
            Integer count = userService.getDailyDownloadCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取下载计数失败", e);
            return Result.internalServerError("获取下载计数失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/increment-download")
    @Operation(summary = "增加下载计数", description = "用户下载文件时调用，增加下载计数")
    public Result<Void> incrementDownloadCount() {
        try {
            Map<String, Object> map = ThreadLocalUtil.get();
            if (map == null) {
                return Result.unauthorized("用户未登录");
            }
            
            Integer userId = (Integer) map.get("id");
            Integer roleCode = (Integer) map.get("roleCode");
            
            // 检查是否超过每日限制
            if (!userService.checkDailyDownloadLimit(userId, roleCode)) {
                return Result.tooManyRequests("今日下载次数已达上限");
            }
            
            // 增加下载计数
            userService.incrementDailyDownloadCount(userId);
            
            // 同时增加总下载计数
            userService.incrUserSumDownloadBy(userId, 1);
            
            return Result.success(null, "下载计数更新成功");
            
        } catch (Exception e) {
            log.error("更新下载计数失败", e);
            return Result.internalServerError("更新下载计数失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/download-limit-check")
    @Operation(summary = "检查下载限制", description = "检查用户是否还可以下载")
    public Result<Boolean> checkDownloadLimit() {
        try {
            Map<String, Object> map = ThreadLocalUtil.get();
            if (map == null) {
                return Result.unauthorized("用户未登录");
            }
            
            Integer userId = (Integer) map.get("id");
            Integer roleCode = (Integer) map.get("roleCode");
            boolean canDownload = userService.checkDailyDownloadLimit(userId, roleCode);
            return Result.success(canDownload);
            
        } catch (Exception e) {
            log.error("检查下载限制失败", e);
            return Result.internalServerError("检查下载限制失败: " + e.getMessage());
        }
    }
    
    /**
     * 增加登录失败次数
     */
    private void incrementLoginAttempt(String username) {
        String loginAttemptKey = "login_attempt:" + username;
        stringRedisTemplate.opsForValue().increment(loginAttemptKey);
        stringRedisTemplate.expire(loginAttemptKey, 15, TimeUnit.MINUTES); // 15分钟后重置
    }
}