package com.lyz.controller;

import com.lyz.pojo.PageBean;
import com.lyz.pojo.Result;
import com.lyz.pojo.User;
import com.lyz.service.UserService;
import com.lyz.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.time.Duration;

@RestController
@RequestMapping("/admin/users")
@Validated
@Tag(name = "管理员-用户管理", description = "用户的增删改查（仅管理员 role_id=1）")
public class UserAdminController {
    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 检查当前用户是否为管理员（role_id=1）
    private boolean isAdmin() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer userId = (Integer) claims.get("id");
        Integer roleId = userService.findRoleIdByUserId(userId);
        return roleId != null && roleId == 1;
    }

    @GetMapping
    @Operation(summary = "分页查询用户")
    public Result<PageBean<User>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @RequestParam(required = false) String username) {
        if (!isAdmin()) return Result.error("无权限");
        return Result.success(userService.pageUsers(pageNum, pageSize, username));
    }

    @PostMapping
    @Operation(summary = "新增用户")
    public Result add(@RequestBody @Validated User user) {
        if (!isAdmin()) return Result.error("无权限");
        userService.adminAddUser(user);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "更新用户（可修改角色）")
    public Result update(@RequestBody @Validated User user) {
        if (!isAdmin()) return Result.error("无权限");
        userService.adminUpdateUser(user);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public Result delete(@PathVariable Integer id) {
        if (!isAdmin()) return Result.error("无权限");
        userService.adminDeleteUser(id);
        return Result.success();
    }

    @GetMapping("/{id}/quota")
    @Operation(summary = "查询用户每日下载配额与剩余TTL")
    public Result<Map<String, Object>> getQuota(@PathVariable Integer id) {
        if (!isAdmin()) return Result.error("无权限");
        String key = "download:daily:" + id;
        String val = stringRedisTemplate.opsForValue().get(key);
        Long ttl = stringRedisTemplate.getExpire(key);
        java.util.HashMap<String, Object> data = new java.util.HashMap<>();
        data.put("value", val == null ? null : Integer.parseInt(val));
        data.put("ttlSeconds", ttl);
        return Result.success(data);
    }

    public static class QuotaUpdate {
        public Integer value; // 新的配额值
        public Integer ttlHours; // 新的过期时间（小时）
    }

    @PatchMapping("/{id}/quota")
    @Operation(summary = "设置用户每日下载配额与过期时间")
    public Result setQuota(@PathVariable Integer id, @RequestBody QuotaUpdate body) {
        if (!isAdmin()) return Result.error("无权限");
        if (body == null || body.value == null || body.value < 0 || body.ttlHours == null || body.ttlHours <= 0) {
            return Result.error("参数不合法");
        }
        String key = "download:daily:" + id;
        stringRedisTemplate.opsForValue().set(key, String.valueOf(body.value));
        stringRedisTemplate.expire(key, Duration.ofHours(body.ttlHours.longValue()));
        return Result.success();
    }
}


