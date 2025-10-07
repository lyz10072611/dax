package com.lyz.service.impl;

import com.lyz.mapper.UserMapper;
import com.lyz.pojo.User;
import com.lyz.pojo.PageBean;
import com.lyz.utils.Md5Util;
import com.lyz.utils.ThreadLocalUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户服务实现类单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPassword("encrypted_password");
        testUser.setNickname("测试用户");
        testUser.setStatus(1);

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testFindByUserName_Success() {
        // 准备测试数据
        String username = "testuser";
        when(userMapper.findByUserName(username)).thenReturn(testUser);

        // 执行测试
        User result = userService.findByUserName(username);

        // 验证结果
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userMapper).findByUserName(username);
    }

    @Test
    void testFindByUserName_NotFound() {
        // 准备测试数据
        String username = "nonexistent";
        when(userMapper.findByUserName(username)).thenReturn(null);

        // 执行测试
        User result = userService.findByUserName(username);

        // 验证结果
        assertNull(result);
        verify(userMapper).findByUserName(username);
    }

    @Test
    void testFindRoleCodeByUsername_Success() {
        // 准备测试数据
        String username = "testuser";
        when(userMapper.findRoleCodeByUsername(username)).thenReturn(1);

        // 执行测试
        Integer result = userService.findRoleCodeByUsername(username);

        // 验证结果
        assertEquals(1, result);
        verify(userMapper).findRoleCodeByUsername(username);
    }

    @Test
    void testFindRoleIdByUserId_Success() {
        // 准备测试数据
        Integer userId = 1;
        when(userMapper.findRoleIdByUserId(userId)).thenReturn(1);

        // 执行测试
        Integer result = userService.findRoleIdByUserId(userId);

        // 验证结果
        assertEquals(1, result);
        verify(userMapper).findRoleIdByUserId(userId);
    }

    @Test
    void testListUsers_Success() {
        // 准备测试数据
        List<User> users = Arrays.asList(testUser);
        when(userMapper.listUsers(null)).thenReturn(users);

        // 执行测试
        List<User> result = userService.listUsers(null);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userMapper).listUsers(null);
    }

    @Test
    void testListUsers_WithUsernameLike() {
        // 准备测试数据
        String usernameLike = "test";
        List<User> users = Arrays.asList(testUser);
        when(userMapper.listUsers(usernameLike)).thenReturn(users);

        // 执行测试
        List<User> result = userService.listUsers(usernameLike);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userMapper).listUsers(usernameLike);
    }

    @Test
    void testPageUsers_Success() {
        // 准备测试数据
        List<User> users = Arrays.asList(testUser);
        PageInfo<User> pageInfo = new PageInfo<>(users);
        pageInfo.setTotal(1);

        try (MockedStatic<PageHelper> pageHelperMock = mockStatic(PageHelper.class)) {
            when(userMapper.listUsers(null)).thenReturn(users);

            // 执行测试
            PageBean<User> result = userService.pageUsers(1, 10, null);

            // 验证结果
            assertNotNull(result);
            assertEquals(1L, result.getTotal());
            assertEquals(1, result.getList().size());
            verify(userMapper).listUsers(null);
        }
    }

    @Test
    void testAdminAddUser_Success() {
        // 准备测试数据
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("plainpassword");
        // status为null，应该设置默认值

        try (MockedStatic<Md5Util> md5UtilMock = mockStatic(Md5Util.class)) {
            md5UtilMock.when(() -> Md5Util.getMD5String("plainpassword")).thenReturn("encrypted_password");
            doNothing().when(userMapper).adminAdd(any(User.class));

            // 执行测试
            userService.adminAddUser(newUser);

            // 验证结果
            assertEquals("encrypted_password", newUser.getPassword());
            assertEquals(1, newUser.getStatus());
            verify(userMapper).adminAdd(newUser);
        }
    }

    @Test
    void testAdminAddUser_WithStatus() {
        // 准备测试数据
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("plainpassword");
        newUser.setStatus(0);

        try (MockedStatic<Md5Util> md5UtilMock = mockStatic(Md5Util.class)) {
            md5UtilMock.when(() -> Md5Util.getMD5String("plainpassword")).thenReturn("encrypted_password");
            doNothing().when(userMapper).adminAdd(any(User.class));

            // 执行测试
            userService.adminAddUser(newUser);

            // 验证结果
            assertEquals("encrypted_password", newUser.getPassword());
            assertEquals(0, newUser.getStatus());
            verify(userMapper).adminAdd(newUser);
        }
    }

    @Test
    void testAdminUpdateUser_Success() {
        // 准备测试数据
        doNothing().when(userMapper).adminUpdate(any(User.class));

        // 执行测试
        userService.adminUpdateUser(testUser);

        // 验证结果
        verify(userMapper).adminUpdate(testUser);
    }

    @Test
    void testAdminDeleteUser_Success() {
        // 准备测试数据
        Integer userId = 1;
        doNothing().when(userMapper).adminDelete(userId);

        // 执行测试
        userService.adminDeleteUser(userId);

        // 验证结果
        verify(userMapper).adminDelete(userId);
    }

    @Test
    void testIncrUserSumDownloadBy_Success() {
        // 准备测试数据
        Integer userId = 1;
        Integer delta = 5;
        doNothing().when(userMapper).incrSumDownloadBy(userId, delta);

        // 执行测试
        userService.incrUserSumDownloadBy(userId, delta);

        // 验证结果
        verify(userMapper).incrSumDownloadBy(userId, delta);
    }

    @Test
    void testRegister_Success() {
        // 准备测试数据
        String username = "newuser";
        String password = "plainpassword";

        try (MockedStatic<Md5Util> md5UtilMock = mockStatic(Md5Util.class)) {
            md5UtilMock.when(() -> Md5Util.getMD5String(password)).thenReturn("encrypted_password");
            doNothing().when(userMapper).add(username, "encrypted_password");

            // 执行测试
            userService.register(username, password);

            // 验证结果
            verify(userMapper).add(username, "encrypted_password");
        }
    }

    @Test
    void testUpdate_Success() {
        // 准备测试数据
        doNothing().when(userMapper).update(any(User.class));

        // 执行测试
        userService.update(testUser);

        // 验证结果
        verify(userMapper).update(testUser);
    }

    @Test
    void testUpdateAvatar_Success() {
        // 准备测试数据
        String avatarUrl = "https://example.com/avatar.jpg";
        Integer userId = 1;

        Map<String, Object> threadLocalMap = new HashMap<>();
        threadLocalMap.put("id", userId);

        try (MockedStatic<ThreadLocalUtil> threadLocalMock = mockStatic(ThreadLocalUtil.class)) {
            threadLocalMock.when(ThreadLocalUtil::get).thenReturn(threadLocalMap);
            doNothing().when(userMapper).updateAvatar(avatarUrl, userId);

            // 执行测试
            userService.updateAvatar(avatarUrl);

            // 验证结果
            verify(userMapper).updateAvatar(avatarUrl, userId);
        }
    }

    @Test
    void testUpdatePwd_Success() {
        // 准备测试数据
        String newPwd = "newpassword";
        Integer userId = 1;

        Map<String, Object> threadLocalMap = new HashMap<>();
        threadLocalMap.put("id", userId);

        try (MockedStatic<Md5Util> md5UtilMock = mockStatic(Md5Util.class);
             MockedStatic<ThreadLocalUtil> threadLocalMock = mockStatic(ThreadLocalUtil.class)) {

            md5UtilMock.when(() -> Md5Util.getMD5String(newPwd)).thenReturn("encrypted_new_password");
            threadLocalMock.when(ThreadLocalUtil::get).thenReturn(threadLocalMap);
            doNothing().when(userMapper).updatePwd("encrypted_new_password", userId);

            // 执行测试
            userService.updatePwd(newPwd);

            // 验证结果
            verify(userMapper).updatePwd("encrypted_new_password", userId);
        }
    }

    @Test
    void testUpdateLastLoginTime_Success() {
        // 准备测试数据
        Integer userId = 1;
        doNothing().when(userMapper).updateLastLoginTime(userId);

        // 执行测试
        userService.updateLastLoginTime(userId);

        // 验证结果
        verify(userMapper).updateLastLoginTime(userId);
    }

    @Test
    void testGetDailyDownloadCount_Success() {
        // 准备测试数据
        Integer userId = 1;
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = "daily_download:" + userId + ":" + today;

        when(valueOperations.get(key)).thenReturn("5");

        // 执行测试
        Integer result = userService.getDailyDownloadCount(userId);

        // 验证结果
        assertEquals(5, result);
        verify(valueOperations).get(key);
    }

    @Test
    void testGetDailyDownloadCount_NoCount() {
        // 准备测试数据
        Integer userId = 1;
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = "daily_download:" + userId + ":" + today;

        when(valueOperations.get(key)).thenReturn(null);

        // 执行测试
        Integer result = userService.getDailyDownloadCount(userId);

        // 验证结果
        assertEquals(0, result);
        verify(valueOperations).get(key);
    }

    @Test
    void testIncrementDailyDownloadCount_Success() {
        // 准备测试数据
        Integer userId = 1;
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = "daily_download:" + userId + ":" + today;

        when(valueOperations.increment(key)).thenReturn(1L);
        when(stringRedisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // 执行测试
        userService.incrementDailyDownloadCount(userId);

        // 验证结果
        verify(valueOperations).increment(key);
        verify(stringRedisTemplate).expire(anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void testIncrementDailyDownloadCount_ExistingCount() {
        // 准备测试数据
        Integer userId = 1;
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = "daily_download:" + userId + ":" + today;

        when(valueOperations.increment(key)).thenReturn(6L);

        // 执行测试
        userService.incrementDailyDownloadCount(userId);

        // 验证结果
        verify(valueOperations).increment(key);
        verify(stringRedisTemplate, never()).expire(anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void testCheckDailyDownloadLimit_WithinLimit() {
        // 准备测试数据
        Integer userId = 1;
        Integer limit = 10;
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = "daily_download:" + userId + ":" + today;

        when(valueOperations.get(key)).thenReturn("5");

        // 执行测试
        boolean result = userService.checkDailyDownloadLimit(userId, limit);

        // 验证结果
        assertTrue(result);
        verify(valueOperations).get(key);
    }

    @Test
    void testCheckDailyDownloadLimit_ExceedLimit() {
        // 准备测试数据
        Integer userId = 1;
        Integer limit = 10;
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = "daily_download:" + userId + ":" + today;

        when(valueOperations.get(key)).thenReturn("10");

        // 执行测试
        boolean result = userService.checkDailyDownloadLimit(userId, limit);

        // 验证结果
        assertFalse(result);
        verify(valueOperations).get(key);
    }

    @Test
    void testCheckDailyDownloadLimit_NoCount() {
        // 准备测试数据
        Integer userId = 1;
        Integer limit = 10;
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = "daily_download:" + userId + ":" + today;

        when(valueOperations.get(key)).thenReturn(null);

        // 执行测试
        boolean result = userService.checkDailyDownloadLimit(userId, limit);

        // 验证结果
        assertTrue(result);
        verify(valueOperations).get(key);
    }

    @Test
    void testResetDailyDownloadCount_Success() {
        // 准备测试数据
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String resetKey = "daily_download_reset_date:" + today;
        String pattern = "daily_download:*:" + today;

        Set<String> keys = Set.of("daily_download:1:" + today, "daily_download:2:" + today);

        when(stringRedisTemplate.hasKey(resetKey)).thenReturn(false);
        when(stringRedisTemplate.keys(pattern)).thenReturn(keys);
        when(stringRedisTemplate.delete(keys)).thenReturn(2L);
        when(stringRedisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // 执行测试
        userService.resetDailyDownloadCount();

        // 验证结果
        verify(stringRedisTemplate).hasKey(resetKey);
        verify(stringRedisTemplate).keys(pattern);
        verify(stringRedisTemplate).delete(keys);
        verify(valueOperations).set(resetKey, "1");
        verify(stringRedisTemplate).expire(anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void testResetDailyDownloadCount_AlreadyReset() {
        // 准备测试数据
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String resetKey = "daily_download_reset_date:" + today;

        when(stringRedisTemplate.hasKey(resetKey)).thenReturn(true);

        // 执行测试
        userService.resetDailyDownloadCount();

        // 验证结果
        verify(stringRedisTemplate).hasKey(resetKey);
        verify(stringRedisTemplate, never()).keys(anyString());
        verify(stringRedisTemplate, never()).delete(any());
        verify(valueOperations, never()).set(anyString(), anyString());
    }
}
