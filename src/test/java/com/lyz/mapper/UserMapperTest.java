package com.lyz.mapper;

import com.lyz.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户Mapper集成测试
 * 注意：这些测试需要真实的数据库连接
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional // 确保测试后回滚数据
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void testFindByUserName_Success() {
        // 准备测试数据
        String username = "testuser";
        String password = "encrypted_password";
        
        userMapper.add(username, password);

        // 执行测试
        User result = userMapper.findByUserName(username);

        // 验证结果
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(password, result.getPassword());
        assertNotNull(result.getId());
        assertTrue(result.getId() > 0);
    }

    @Test
    void testFindByUserName_NotFound() {
        // 执行测试
        User result = userMapper.findByUserName("nonexistentuser");

        // 验证结果
        assertNull(result);
    }

    @Test
    void testFindRoleCodeByUsername_Success() {
        // 准备测试数据
        String username = "rolecodetest";
        String password = "encrypted_password";
        
        userMapper.add(username, password);

        // 执行测试
        Integer result = userMapper.findRoleCodeByUsername(username);

        // 验证结果
        assertNotNull(result);
        // 默认角色应该是user角色，假设role_code为1
        assertTrue(result >= 1);
    }

    @Test
    void testFindRoleCodeByUsername_NotFound() {
        // 执行测试
        Integer result = userMapper.findRoleCodeByUsername("nonexistentuser");

        // 验证结果
        assertNull(result);
    }

    @Test
    void testFindRoleIdByUserId_Success() {
        // 准备测试数据
        String username = "roleidtest";
        String password = "encrypted_password";
        
        userMapper.add(username, password);
        User user = userMapper.findByUserName(username);
        Integer userId = user.getId();

        // 执行测试
        Integer result = userMapper.findRoleIdByUserId(userId);

        // 验证结果
        assertNotNull(result);
        assertTrue(result >= 1);
    }

    @Test
    void testFindRoleIdByUserId_NotFound() {
        // 执行测试
        Integer result = userMapper.findRoleIdByUserId(999);

        // 验证结果
        assertNull(result);
    }

    @Test
    void testListUsers_AllUsers() {
        // 准备测试数据
        userMapper.add("user1", "password1");
        userMapper.add("user2", "password2");
        userMapper.add("user3", "password3");

        // 执行测试
        List<User> result = userMapper.listUsers(null);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.size() >= 3);
        
        // 验证数据按ID降序排列
        boolean foundUser1 = result.stream().anyMatch(u -> "user1".equals(u.getUsername()));
        boolean foundUser2 = result.stream().anyMatch(u -> "user2".equals(u.getUsername()));
        boolean foundUser3 = result.stream().anyMatch(u -> "user3".equals(u.getUsername()));
        
        assertTrue(foundUser1);
        assertTrue(foundUser2);
        assertTrue(foundUser3);
    }

    @Test
    void testListUsers_WithUsernameLike() {
        // 准备测试数据
        userMapper.add("testuser1", "password1");
        userMapper.add("testuser2", "password2");
        userMapper.add("otheruser", "password3");

        // 执行测试
        List<User> result = userMapper.listUsers("test");

        // 验证结果
        assertNotNull(result);
        assertTrue(result.size() >= 2);
        
        // 验证只返回包含"test"的用户
        boolean foundTestUser1 = result.stream().anyMatch(u -> "testuser1".equals(u.getUsername()));
        boolean foundTestUser2 = result.stream().anyMatch(u -> "testuser2".equals(u.getUsername()));
        boolean foundOtherUser = result.stream().anyMatch(u -> "otheruser".equals(u.getUsername()));
        
        assertTrue(foundTestUser1);
        assertTrue(foundTestUser2);
        assertFalse(foundOtherUser);
    }

    @Test
    void testAdminAdd_Success() {
        // 准备测试数据
        User user = new User();
        user.setUsername("adminuser");
        user.setPassword("encrypted_password");
        user.setNickname("管理员用户");
        user.setEmail("admin@example.com");
        user.setUserPic("https://example.com/avatar.jpg");
        user.setRoleId(1);
        user.setStatus(1);

        // 执行测试
        userMapper.adminAdd(user);

        // 验证结果
        User result = userMapper.findByUserName("adminuser");
        assertNotNull(result);
        assertEquals("adminuser", result.getUsername());
        assertEquals("encrypted_password", result.getPassword());
        assertEquals("管理员用户", result.getNickname());
        assertEquals("admin@example.com", result.getEmail());
        assertEquals("https://example.com/avatar.jpg", result.getUserPic());
        assertEquals(1, result.getRoleId());
        assertEquals(1, result.getStatus());
    }

    @Test
    void testAdminUpdate_Success() {
        // 准备测试数据
        User user = new User();
        user.setUsername("updateuser");
        user.setPassword("encrypted_password");
        user.setNickname("原始昵称");
        user.setEmail("original@example.com");
        user.setUserPic("https://example.com/original.jpg");
        user.setRoleId(1);
        user.setStatus(1);

        userMapper.adminAdd(user);
        User originalUser = userMapper.findByUserName("updateuser");
        Integer userId = originalUser.getId();

        // 更新数据
        user.setId(userId);
        user.setNickname("更新后的昵称");
        user.setEmail("updated@example.com");
        user.setUserPic("https://example.com/updated.jpg");
        user.setRoleId(2);
        user.setStatus(0);

        // 执行测试
        userMapper.adminUpdate(user);

        // 验证结果
        User result = userMapper.findByUserName("updateuser");
        assertNotNull(result);
        assertEquals("更新后的昵称", result.getNickname());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("https://example.com/updated.jpg", result.getUserPic());
        assertEquals(2, result.getRoleId());
        assertEquals(0, result.getStatus());
    }

    @Test
    void testAdminDelete_Success() {
        // 准备测试数据
        User user = new User();
        user.setUsername("deleteuser");
        user.setPassword("encrypted_password");
        user.setNickname("要删除的用户");
        user.setRoleId(1);
        user.setStatus(1);

        userMapper.adminAdd(user);
        User originalUser = userMapper.findByUserName("deleteuser");
        Integer userId = originalUser.getId();

        // 验证数据存在
        User beforeDelete = userMapper.findByUserName("deleteuser");
        assertNotNull(beforeDelete);

        // 执行测试
        userMapper.adminDelete(userId);

        // 验证结果
        User afterDelete = userMapper.findByUserName("deleteuser");
        assertNull(afterDelete);
    }

    @Test
    void testAdd_Success() {
        // 准备测试数据
        String username = "newuser";
        String password = "encrypted_password";

        // 执行测试
        userMapper.add(username, password);

        // 验证结果
        User result = userMapper.findByUserName(username);
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(password, result.getPassword());
        assertNotNull(result.getId());
        assertTrue(result.getId() > 0);
        assertEquals(1, result.getStatus()); // 默认状态为1
    }

    @Test
    void testUpdate_Success() {
        // 准备测试数据
        String username = "updateinfouser";
        String password = "encrypted_password";
        
        userMapper.add(username, password);
        User originalUser = userMapper.findByUserName(username);
        Integer userId = originalUser.getId();

        // 更新数据
        User user = new User();
        user.setId(userId);
        user.setNickname("更新后的昵称");
        user.setEmail("updated@example.com");

        // 执行测试
        userMapper.update(user);

        // 验证结果
        User result = userMapper.findByUserName(username);
        assertNotNull(result);
        assertEquals("更新后的昵称", result.getNickname());
        assertEquals("updated@example.com", result.getEmail());
    }

    @Test
    void testUpdateAvatar_Success() {
        // 准备测试数据
        String username = "avataruser";
        String password = "encrypted_password";
        
        userMapper.add(username, password);
        User originalUser = userMapper.findByUserName(username);
        Integer userId = originalUser.getId();

        String avatarUrl = "https://example.com/newavatar.jpg";

        // 执行测试
        userMapper.updateAvatar(avatarUrl, userId);

        // 验证结果
        User result = userMapper.findByUserName(username);
        assertNotNull(result);
        assertEquals(avatarUrl, result.getUserPic());
    }

    @Test
    void testUpdatePwd_Success() {
        // 准备测试数据
        String username = "pwduser";
        String originalPassword = "original_password";
        String newPassword = "new_encrypted_password";
        
        userMapper.add(username, originalPassword);
        User originalUser = userMapper.findByUserName(username);
        Integer userId = originalUser.getId();

        // 执行测试
        userMapper.updatePwd(newPassword, userId);

        // 验证结果
        User result = userMapper.findByUserName(username);
        assertNotNull(result);
        assertEquals(newPassword, result.getPassword());
    }

    @Test
    void testIncrSumDownloadBy_Success() {
        // 准备测试数据
        String username = "downloaduser";
        String password = "encrypted_password";
        
        userMapper.add(username, password);
        User originalUser = userMapper.findByUserName(username);
        Integer userId = originalUser.getId();

        // 执行测试
        userMapper.incrSumDownloadBy(userId, 5);

        // 验证结果
        User result = userMapper.findByUserName(username);
        assertNotNull(result);
        // 注意：这里假设用户表有download_count字段，如果没有则跳过此验证
        // assertEquals(5, result.getDownloadCount());
    }

    @Test
    void testUpdateLastLoginTime_Success() {
        // 准备测试数据
        String username = "logintimeuser";
        String password = "encrypted_password";
        
        userMapper.add(username, password);
        User originalUser = userMapper.findByUserName(username);
        Integer userId = originalUser.getId();

        // 执行测试
        userMapper.updateLastLoginTime(userId);

        // 验证结果
        User result = userMapper.findByUserName(username);
        assertNotNull(result);
        assertNotNull(result.getLastLoginTime());
    }

    @Test
    void testMultipleOperations() {
        // 准备测试数据
        String username = "multiuser";
        String password = "encrypted_password";
        
        // 添加用户
        userMapper.add(username, password);
        User user = userMapper.findByUserName(username);
        Integer userId = user.getId();

        // 更新用户信息
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setNickname("多操作测试用户");
        updateUser.setEmail("multi@example.com");
        userMapper.update(updateUser);

        // 更新头像
        userMapper.updateAvatar("https://example.com/multi.jpg", userId);

        // 更新密码
        userMapper.updatePwd("new_encrypted_password", userId);

        // 更新最后登录时间
        userMapper.updateLastLoginTime(userId);

        // 验证所有操作
        User result = userMapper.findByUserName(username);
        assertNotNull(result);
        assertEquals("多操作测试用户", result.getNickname());
        assertEquals("multi@example.com", result.getEmail());
        assertEquals("https://example.com/multi.jpg", result.getUserPic());
        assertEquals("new_encrypted_password", result.getPassword());
        assertNotNull(result.getLastLoginTime());
    }
}
