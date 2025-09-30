package com.lyz.service;

import com.lyz.pojo.User;
import com.lyz.pojo.PageBean;

import java.util.List;

/**
 * 用户服务接口 - 适配水泥厂业务系统
 */
public interface UserService {
    
    // 基础查询方法
    User findByUserName(String username);
    Integer findRoleCodeByUsername(String username);
    Integer findRoleIdByUserId(Integer id);
    
    // 分页和列表查询
    PageBean<User> pageUsers(Integer pageNum, Integer pageSize, String usernameLike);
    List<User> listUsers(String usernameLike);
    
    // 管理员操作
    void adminAddUser(User user);
    void adminUpdateUser(User user);
    void adminDeleteUser(Integer id);
    
    // 用户操作
    void register(String username, String password);
    void update(User user);
    void updateAvatar(String avatarUrl);
    void updatePwd(String newPwd);
    
    // 下载相关
    void incrUserSumDownloadBy(Integer id, Integer delta);
    
    // Redis下载计数管理
    Integer getDailyDownloadCount(Integer userId);
    void incrementDailyDownloadCount(Integer userId);
    boolean checkDailyDownloadLimit(Integer userId, Integer limit);
    void resetDailyDownloadCount();
    
    // 登录相关
    void updateLastLoginTime(Integer id);
}
