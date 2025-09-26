package com.lyz.service.impl;

import com.lyz.mapper.UserMapper;
import com.lyz.pojo.User;
import com.lyz.pojo.PageBean;
import com.lyz.service.UserService;
import com.lyz.utils.Md5Util;
import com.lyz.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public User findByUserName(String username) {
        User u=userMapper.findByUserName(username);
        return u;
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
    public void adminAddUser(User user) {
        if (user.getPassword() != null) {
            user.setPassword(Md5Util.getMD5String(user.getPassword()));
        }
        if (user.getDailyDownload() == null) user.setDailyDownload(500);
        if (user.getSumDownload() == null) user.setSumDownload(0);
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
        //密码加密
        String md5String = Md5Util.getMD5String(password);
        //添加用户
        userMapper.add(username,md5String);


    }

    @Override
    public void update(User user) {
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }

    @Override
    public void updateAvatar(String avatarUrl) {
        Map map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        userMapper.updateAvatar(avatarUrl,id);
    }

    @Override
    public void updatePwd(String newPwd) {
        Map map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        userMapper.updatePwd(Md5Util.getMD5String(newPwd),id);
    }
}
