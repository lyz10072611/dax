package com.lyz.controller;

import com.lyz.pojo.Result;
import com.lyz.pojo.User;
import com.lyz.service.UserService;
import com.lyz.utils.JwtUtil;
import com.lyz.utils.Md5Util;
import com.lyz.utils.ThreadLocalUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户控制器单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testRegister_Success() throws Exception {
        // 准备测试数据
        String username = "testuser";
        String password = "testpass";

        when(userService.findByUserName(username)).thenReturn(null);
        doNothing().when(userService).register(username, password);

        // 执行测试
        mockMvc.perform(post("/user/register")
                .param("username", username)
                .param("password", password))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).findByUserName(username);
        verify(userService).register(username, password);
    }

    @Test
    void testRegister_UsernameExists() throws Exception {
        // 准备测试数据
        String username = "existinguser";
        String password = "testpass";

        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setUsername(username);

        when(userService.findByUserName(username)).thenReturn(existingUser);

        // 执行测试
        mockMvc.perform(post("/user/register")
                .param("username", username)
                .param("password", password))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("用户名已被占用"));

        verify(userService).findByUserName(username);
        verify(userService, never()).register(anyString(), anyString());
    }

    @Test
    void testLogin_Success() throws Exception {
        // 准备测试数据
        String username = "testuser";
        String password = "testpass";
        String encryptedPassword = "encrypted_password";

        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUsername(username);
        loginUser.setPassword(encryptedPassword);

        when(userService.findByUserName(username)).thenReturn(loginUser);
        when(userService.findRoleCodeByUsername(username)).thenReturn(1);
        doNothing().when(userService).updateLastLoginTime(1);

        try (MockedStatic<Md5Util> md5UtilMock = mockStatic(Md5Util.class);
             MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {

            md5UtilMock.when(() -> Md5Util.getMD5String(password)).thenReturn(encryptedPassword);
            jwtUtilMock.when(() -> JwtUtil.genToken(any(Map.class))).thenReturn("test_token");

            // 执行测试
            mockMvc.perform(post("/user/login")
                    .param("username", username)
                    .param("password", password))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.token").value("test_token"))
                    .andExpect(jsonPath("$.data.username").value(username))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.roleCode").value(1));

            verify(userService).findByUserName(username);
            verify(userService).findRoleCodeByUsername(username);
            verify(userService).updateLastLoginTime(1);
            verify(valueOperations).set(eq("test_token"), eq("test_token"), eq(1L), any());
        }
    }

    @Test
    void testLogin_UserNotFound() throws Exception {
        // 准备测试数据
        String username = "nonexistentuser";
        String password = "testpass";

        when(userService.findByUserName(username)).thenReturn(null);

        // 执行测试
        mockMvc.perform(post("/user/login")
                .param("username", username)
                .param("password", password))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("用户名不存在"));

        verify(userService).findByUserName(username);
        verify(userService, never()).findRoleCodeByUsername(anyString());
    }

    @Test
    void testLogin_WrongPassword() throws Exception {
        // 准备测试数据
        String username = "testuser";
        String password = "wrongpass";
        String encryptedPassword = "encrypted_password";

        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUsername(username);
        loginUser.setPassword(encryptedPassword);

        when(userService.findByUserName(username)).thenReturn(loginUser);

        try (MockedStatic<Md5Util> md5UtilMock = mockStatic(Md5Util.class)) {
            md5UtilMock.when(() -> Md5Util.getMD5String(password)).thenReturn("wrong_encrypted");

            // 执行测试
            mockMvc.perform(post("/user/login")
                    .param("username", username)
                    .param("password", password))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.msg").value("密码错误"));

            verify(userService).findByUserName(username);
            verify(userService, never()).findRoleCodeByUsername(anyString());
        }
    }

    @Test
    void testUserInfo_Success() throws Exception {
        // 准备测试数据
        String username = "testuser";
        User user = new User();
        user.setId(1);
        user.setUsername(username);

        Map<String, Object> threadLocalMap = new HashMap<>();
        threadLocalMap.put("username", username);

        when(userService.findByUserName(username)).thenReturn(user);

        try (MockedStatic<ThreadLocalUtil> threadLocalMock = mockStatic(ThreadLocalUtil.class)) {
            threadLocalMock.when(ThreadLocalUtil::get).thenReturn(threadLocalMap);

            // 执行测试
            mockMvc.perform(get("/user/userInfo"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.username").value(username));

            verify(userService).findByUserName(username);
        }
    }

    @Test
    void testUpdate_Success() throws Exception {
        // 准备测试数据
        User user = new User();
        user.setId(1);
        user.setUsername("testuser");
        user.setNickname("测试用户");

        doNothing().when(userService).update(any(User.class));

        // 执行测试
        mockMvc.perform(put("/user/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).update(any(User.class));
    }

    @Test
    void testUpdateAvatar_Success() throws Exception {
        // 准备测试数据
        String avatarUrl = "https://example.com/avatar.jpg";

        doNothing().when(userService).updateAvatar(avatarUrl);

        // 执行测试
        mockMvc.perform(patch("/user/updateAvatar")
                .param("avatarUrl", avatarUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).updateAvatar(avatarUrl);
    }

    @Test
    void testUpdatePwd_Success() throws Exception {
        // 准备测试数据
        String oldPwd = "oldpass";
        String newPwd = "newpass";
        String rePwd = "newpass";
        String token = "test_token";
        String username = "testuser";
        String encryptedOldPwd = "encrypted_old";
        String encryptedNewPwd = "encrypted_new";

        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUsername(username);
        loginUser.setPassword(encryptedOldPwd);

        Map<String, Object> threadLocalMap = new HashMap<>();
        threadLocalMap.put("username", username);

        when(userService.findByUserName(username)).thenReturn(loginUser);
        doNothing().when(userService).updatePwd(anyString());
        when(valueOperations.getOperations()).thenReturn(stringRedisTemplate);

        try (MockedStatic<Md5Util> md5UtilMock = mockStatic(Md5Util.class);
             MockedStatic<ThreadLocalUtil> threadLocalMock = mockStatic(ThreadLocalUtil.class)) {

            md5UtilMock.when(() -> Md5Util.getMD5String(oldPwd)).thenReturn(encryptedOldPwd);
            md5UtilMock.when(() -> Md5Util.getMD5String(newPwd)).thenReturn(encryptedNewPwd);
            threadLocalMock.when(ThreadLocalUtil::get).thenReturn(threadLocalMap);

            Map<String, String> params = new HashMap<>();
            params.put("oldPwd", oldPwd);
            params.put("newPwd", newPwd);
            params.put("rePwd", rePwd);

            // 执行测试
            mockMvc.perform(patch("/user/updatePwd")
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(params)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(userService).findByUserName(username);
            verify(userService).updatePwd(anyString());
            verify(stringRedisTemplate).delete(token);
        }
    }

    @Test
    void testUpdatePwd_WrongOldPassword() throws Exception {
        // 准备测试数据
        String oldPwd = "wrongold";
        String newPwd = "newpass";
        String rePwd = "newpass";
        String token = "test_token";
        String username = "testuser";
        String encryptedOldPwd = "encrypted_old";

        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUsername(username);
        loginUser.setPassword(encryptedOldPwd);

        Map<String, Object> threadLocalMap = new HashMap<>();
        threadLocalMap.put("username", username);

        when(userService.findByUserName(username)).thenReturn(loginUser);

        try (MockedStatic<Md5Util> md5UtilMock = mockStatic(Md5Util.class);
             MockedStatic<ThreadLocalUtil> threadLocalMock = mockStatic(ThreadLocalUtil.class)) {

            md5UtilMock.when(() -> Md5Util.getMD5String(oldPwd)).thenReturn("wrong_encrypted");
            threadLocalMock.when(ThreadLocalUtil::get).thenReturn(threadLocalMap);

            Map<String, String> params = new HashMap<>();
            params.put("oldPwd", oldPwd);
            params.put("newPwd", newPwd);
            params.put("rePwd", rePwd);

            // 执行测试
            mockMvc.perform(patch("/user/updatePwd")
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(params)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.msg").value("原密码不正确"));

            verify(userService).findByUserName(username);
            verify(userService, never()).updatePwd(anyString());
        }
    }

    @Test
    void testGetDailyDownloadCount_Success() throws Exception {
        // 准备测试数据
        Integer userId = 1;
        Integer count = 5;

        Map<String, Object> threadLocalMap = new HashMap<>();
        threadLocalMap.put("id", userId);

        when(userService.getDailyDownloadCount(userId)).thenReturn(count);

        try (MockedStatic<ThreadLocalUtil> threadLocalMock = mockStatic(ThreadLocalUtil.class)) {
            threadLocalMock.when(ThreadLocalUtil::get).thenReturn(threadLocalMap);

            // 执行测试
            mockMvc.perform(get("/user/daily-download-count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(5));

            verify(userService).getDailyDownloadCount(userId);
        }
    }

    @Test
    void testIncrementDownloadCount_Success() throws Exception {
        // 准备测试数据
        Integer userId = 1;

        Map<String, Object> threadLocalMap = new HashMap<>();
        threadLocalMap.put("id", userId);

        when(userService.checkDailyDownloadLimit(userId, 10)).thenReturn(true);
        doNothing().when(userService).incrementDailyDownloadCount(userId);
        doNothing().when(userService).incrUserSumDownloadBy(userId, 1);

        try (MockedStatic<ThreadLocalUtil> threadLocalMock = mockStatic(ThreadLocalUtil.class)) {
            threadLocalMock.when(ThreadLocalUtil::get).thenReturn(threadLocalMap);

            // 执行测试
            mockMvc.perform(post("/user/increment-download"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(userService).checkDailyDownloadLimit(userId, 10);
            verify(userService).incrementDailyDownloadCount(userId);
            verify(userService).incrUserSumDownloadBy(userId, 1);
        }
    }

    @Test
    void testIncrementDownloadCount_LimitExceeded() throws Exception {
        // 准备测试数据
        Integer userId = 1;

        Map<String, Object> threadLocalMap = new HashMap<>();
        threadLocalMap.put("id", userId);

        when(userService.checkDailyDownloadLimit(userId, 10)).thenReturn(false);

        try (MockedStatic<ThreadLocalUtil> threadLocalMock = mockStatic(ThreadLocalUtil.class)) {
            threadLocalMock.when(ThreadLocalUtil::get).thenReturn(threadLocalMap);

            // 执行测试
            mockMvc.perform(post("/user/increment-download"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.msg").value("今日下载次数已达上限"));

            verify(userService).checkDailyDownloadLimit(userId, 10);
            verify(userService, never()).incrementDailyDownloadCount(anyInt());
        }
    }

    @Test
    void testCheckDownloadLimit_Success() throws Exception {
        // 准备测试数据
        Integer userId = 1;
        boolean canDownload = true;

        Map<String, Object> threadLocalMap = new HashMap<>();
        threadLocalMap.put("id", userId);

        when(userService.checkDailyDownloadLimit(userId, 10)).thenReturn(canDownload);

        try (MockedStatic<ThreadLocalUtil> threadLocalMock = mockStatic(ThreadLocalUtil.class)) {
            threadLocalMock.when(ThreadLocalUtil::get).thenReturn(threadLocalMap);

            // 执行测试
            mockMvc.perform(get("/user/download-limit-check"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));

            verify(userService).checkDailyDownloadLimit(userId, 10);
        }
    }
}
