package com.lyz.service.impl;

import com.lyz.mapper.UserMapper;
import com.lyz.pojo.User;
import com.lyz.pojo.PageBean;
import com.lyz.service.UserService;
import com.lyz.utils.Md5Util;
import com.lyz.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * 用户服务实现类 - 适配水泥厂业务系统，使用Redis管理每日下载计数
 */
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    // Redis key前缀
    private static final String DAILY_DOWNLOAD_KEY_PREFIX = "daily_download:";
    private static final String DAILY_DOWNLOAD_RESET_KEY = "daily_download_reset_date";
    
    // 默认每日下载限制
    private static final Integer DEFAULT_DAILY_LIMIT = 10;

    @Override
    public User findByUserName(String username) {
        return userMapper.findByUserName(username);
    }

    @Override
    public Integer findRoleCodeByUsername(String username) {
        return userMapper.findRoleCodeByUsername(username);
    }

    @Override
    public Integer findRoleIdByUserId(Integer id) {
        return userMapper.findRoleIdByUserId(id);
    }

    @Override
    public PageBean<User> pageUsers(Integer pageNum, Integer pageSize, String usernameLike) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> list = userMapper.listUsers(usernameLike);
        PageInfo<User> pageInfo = new PageInfo<>(list);
        return new PageBean<>(pageInfo.getTotal(), pageInfo.getList());
    }
    
    @Override
    public List<User> listUsers(String usernameLike) {
        return userMapper.listUsers(usernameLike);
    }

    @Override
    public void adminAddUser(User user) {
        if (user.getPassword() != null) {
            user.setPassword(Md5Util.getMD5String(user.getPassword()));
        }
        if (user.getStatus() == null) user.setStatus(1);
        userMapper.adminAdd(user);
    }

    @Override
    public void adminUpdateUser(User user) {
        userMapper.adminUpdate(user);
    }

    @Override
    public void adminDeleteUser(Integer id) {
        userMapper.adminDelete(id);
    }

    @Override
    public void incrUserSumDownloadBy(Integer id, Integer delta) {
        userMapper.incrSumDownloadBy(id, delta);
    }

    @Override
    public void register(String username, String password) {
        // 密码加密
        String md5String = Md5Util.getMD5String(password);
        // 添加用户
        userMapper.add(username, md5String);
    }

    @Override
    public void update(User user) {
        userMapper.update(user);
    }

    @Override
    public void updateAvatar(String avatarUrl) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        userMapper.updateAvatar(avatarUrl, id);
    }

    @Override
    public void updatePwd(String newPwd) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        userMapper.updatePwd(Md5Util.getMD5String(newPwd), id);
    }
    
    @Override
    public void updateLastLoginTime(Integer id) {
        userMapper.updateLastLoginTime(id);
    }
    
    // ==================== Redis下载计数管理 ====================
    
    @Override
    public Integer getDailyDownloadCount(Integer userId) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = DAILY_DOWNLOAD_KEY_PREFIX + userId + ":" + today;
        
        String count = stringRedisTemplate.opsForValue().get(key);
        return count != null ? Integer.parseInt(count) : 0;
    }
    
    @Override
    public void incrementDailyDownloadCount(Integer userId) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = DAILY_DOWNLOAD_KEY_PREFIX + userId + ":" + today;
        
        // 增加计数，如果key不存在则初始化为1
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count == 1) {
            // 如果是第一次创建，设置过期时间为明天凌晨2点
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            long secondsUntilMidnight = java.time.Duration.between(
                java.time.LocalDateTime.now(),
                tomorrow.atStartOfDay().plusHours(2)
            ).getSeconds();
            stringRedisTemplate.expire(key, secondsUntilMidnight, TimeUnit.SECONDS);
        }
    }
    
    @Override
    public boolean checkDailyDownloadLimit(Integer userId, Integer limit) {
        Integer currentCount = getDailyDownloadCount(userId);
        return currentCount < limit;
    }
    
    @Override
    public void resetDailyDownloadCount() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String resetKey = DAILY_DOWNLOAD_RESET_KEY + ":" + today;
        
        // 检查今天是否已经重置过
        Boolean hasReset = stringRedisTemplate.hasKey(resetKey);
        if (hasReset != null && hasReset) {
            return; // 今天已经重置过
        }
        
        // 删除所有用户的今日下载计数
        String pattern = DAILY_DOWNLOAD_KEY_PREFIX + "*:" + today;
        var keys = stringRedisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
        
        // 标记今天已重置，过期时间为明天凌晨2点
        stringRedisTemplate.opsForValue().set(resetKey, "1");
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        long secondsUntilMidnight = java.time.Duration.between(
            java.time.LocalDateTime.now(),
            tomorrow.atStartOfDay().plusHours(2)
        ).getSeconds();
        stringRedisTemplate.expire(resetKey, secondsUntilMidnight, TimeUnit.SECONDS);
    }
}
