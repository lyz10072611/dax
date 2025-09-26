package com.lyz.service;

import com.lyz.pojo.User;

public interface UserService {

    User findByUserName(String username);

    void register(String username, String password);

    Integer findRoleCodeByUsername(String username);

    Integer findRoleIdByUserId(Integer id);

    com.lyz.pojo.PageBean<com.lyz.pojo.User> pageUsers(Integer pageNum, Integer pageSize, String usernameLike);

    void adminAddUser(com.lyz.pojo.User user);

    void adminUpdateUser(com.lyz.pojo.User user);

    void adminDeleteUser(Integer id);

    void incrUserSumDownloadBy(Integer id, Integer delta);

    void update(User user);

    void updateAvatar(String avatarUrl);

    void updatePwd(String newPwd);
}
