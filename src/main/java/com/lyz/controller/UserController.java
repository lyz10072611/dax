package com.lyz.controller;
import com.lyz.pojo.Result;
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
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.lyz.pojo.User;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginContext;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Validated
@Tag(name = "用户模块", description = "注册、登录与用户信息")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户，默认角色为 user")
    public Result<Void> register(@Pattern(regexp = "^\\S{5,16}$")String username, @Pattern(regexp = "^\\S{5,16}$")String password){
        //查询用户根据用户名
        User u=userService.findByUserName(username);
        if(u==null){
            //没有被占用  --->注册

                userService.register(username,password);
                return Result.<Void>success();
        } else {
            //占用
            return Result.error("用户名已被占用");
        }
    }
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "根据用户名与密码登录，返回 JWT Token")
    public Result<Map<String, Object>> login(@Pattern(regexp = "^\\S{5,16}$")String username, @Pattern(regexp = "^\\S{5,16}$")String password){
        //根据username查询用户是否存在
        User loginUser = userService.findByUserName(username);
        if(loginUser==null){
            return Result.error("用户名不存在");
        }
        //判断密码是否匹配， 数据库中的密码是加密的 需要将输入的密码通过md5加密后进行比对
        // 登录成功：生成JWT Token并存储到Redis
        if (Md5Util.getMD5String(password).equals(loginUser.getPassword())){
            Map<String,Object> claims=new HashMap<>();
            claims.put("username",loginUser.getUsername());
            claims.put("id",loginUser.getId());
            Integer roleCode = userService.findRoleCodeByUsername(username);
            claims.put("roleCode", roleCode);
            String token = JwtUtil.genToken(claims);
            log.info("Login success. username={}, roleCode={}, token={}", username, roleCode, token);
            
            // 更新最后登录时间
            userService.updateLastLoginTime(loginUser.getId());
            
            //将用户登录时候获取到的token存储到redis中
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            operations.set(token,token,1, TimeUnit.HOURS);
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("username", loginUser.getUsername());
            result.put("id", loginUser.getId());
            result.put("roleCode", roleCode);
            return Result.success(result);
        }
        return Result.error("密码错误");
    }

    @GetMapping("/userInfo")
    @Operation(summary = "获取用户信息", description = "根据 Token 获取当前用户信息")
    public Result<User> userinfo(){
        Map<String, Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        User user = userService.findByUserName(username);
        return Result.success(user);
    }
    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Validated User user){
        userService.update(user);
        return Result.<Void>success();
    }
    @PatchMapping("/updateAvatar")
    public Result<Void> updayeAvatar(@RequestParam @Pattern(regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", message = "头像URL格式不正确") String avatarUrl){
        userService.updateAvatar(avatarUrl);
        return Result.<Void>success();
    }
    @PatchMapping("/updatePwd")
    public Result<Void> updatePwd(@RequestBody Map<String,String> params,@RequestHeader("Authorization") String token){
        //校验参数
        String oldPwd=params.get("oldPwd");
        String newPwd=params.get("newPwd");
        String rePwd=params.get("rePwd");
        if (!StringUtils.hasLength(oldPwd) && StringUtils.hasLength(newPwd) && StringUtils.hasLength(rePwd)){
            return Result.error("缺少必要参数");
        }

        //判断旧密码是否正确  从线程中获取
        Map<String, Object> map = ThreadLocalUtil.get();
        String  username = (String) map.get("username");
        User LoginUser = userService.findByUserName(username);
        if (!LoginUser.getPassword().equals(Md5Util.getMD5String(oldPwd))){
            return Result.error("原密码不正确");
        }
        if (!rePwd.equals(newPwd)){
            return Result.error("两次输入的密码不同");
        }
        if(oldPwd.equals(newPwd)){
            return Result.error("新修改的密码不能与旧密码相同");
        }
        userService.updatePwd(newPwd);
        //删除redis中的token
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        operations.getOperations().delete(token);

        return Result.<Void>success();

    }

    // ==================== 下载计数管理 ====================
    
    @GetMapping("/daily-download-count")
    @Operation(summary = "获取用户每日下载计数", description = "获取当前用户今日已下载次数")
    public Result<Integer> getDailyDownloadCount() {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        Integer count = userService.getDailyDownloadCount(userId);
        return Result.success(count);
    }
    
    @PostMapping("/increment-download")
    @Operation(summary = "增加下载计数", description = "用户下载文件时调用，增加下载计数")
    public Result<Void> incrementDownloadCount() {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        
        // 检查是否超过每日限制
        if (!userService.checkDailyDownloadLimit(userId, 10)) {
            return Result.error("今日下载次数已达上限");
        }
        
        // 增加下载计数
        userService.incrementDailyDownloadCount(userId);
        
        // 同时增加总下载计数
        userService.incrUserSumDownloadBy(userId, 1);
        
        return Result.<Void>success();
    }
    
    @GetMapping("/download-limit-check")
    @Operation(summary = "检查下载限制", description = "检查用户是否还可以下载")
    public Result<Boolean> checkDownloadLimit() {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        boolean canDownload = userService.checkDailyDownloadLimit(userId, 10);
        return Result.success(canDownload);
    }


}

