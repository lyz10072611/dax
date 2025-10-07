package com.lyz.integration;

import com.lyz.controller.CementPlantController;
import com.lyz.controller.UserController;
import com.lyz.pojo.CementPlant;
import com.lyz.pojo.CementPlantDataDTO;
import com.lyz.pojo.Result;
import com.lyz.pojo.User;
import com.lyz.service.CementPlantService;
import com.lyz.service.UserService;
import com.lyz.utils.JwtUtil;
import com.lyz.utils.Md5Util;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 集成测试 - 测试整个应用的功能流程
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class IntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CementPlantService cementPlantService;

    @Autowired
    private UserService userService;

    @Autowired
    private CementPlantController cementPlantController;

    @Autowired
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCompleteUserRegistrationAndLoginFlow() throws Exception {
        // 1. 用户注册
        mockMvc.perform(post("/user/register")
                .param("username", "integrationuser")
                .param("password", "integrationpass"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 2. 用户登录
        String loginResponse = mockMvc.perform(post("/user/login")
                .param("username", "integrationuser")
                .param("password", "integrationpass"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.username").value("integrationuser"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 解析登录响应获取token
        Result loginResult = objectMapper.readValue(loginResponse, Result.class);
        Map<String, Object> loginData = (Map<String, Object>) loginResult.getData();
        String token = (String) loginData.get("token");

        // 3. 使用token获取用户信息
        mockMvc.perform(get("/user/userInfo")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("integrationuser"));
    }

    @Test
    void testCompleteCementPlantManagementFlow() throws Exception {
        // 1. 创建水泥厂
        CementPlant plant = new CementPlant();
        plant.setPlantName("集成测试水泥厂");
        plant.setLongitude(118.0);
        plant.setLatitude(32.0);
        plant.setProvince("江苏省");
        plant.setCity("南京市");
        plant.setStatus("active");

        String createResponse = mockMvc.perform(post("/cement-plant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(plant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 2. 查询水泥厂列表
        mockMvc.perform(get("/cement-plant/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        // 3. 分页查询水泥厂
        mockMvc.perform(get("/cement-plant/page")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").exists())
                .andExpect(jsonPath("$.data.list").isArray());

        // 4. 按地理位置范围查询
        mockMvc.perform(get("/cement-plant/location-range")
                .param("minLng", "117.0")
                .param("maxLng", "119.0")
                .param("minLat", "31.0")
                .param("maxLat", "33.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        // 5. 获取统计信息
        mockMvc.perform(get("/cement-plant/stats/active-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void testCompleteCementPlantDataFlow() throws Exception {
        // 1. 批量添加水泥厂数据
        CementPlantDataDTO dataDTO = new CementPlantDataDTO();
        dataDTO.setPlantName("批量数据水泥厂");
        dataDTO.setLongitude(118.0);
        dataDTO.setLatitude(32.0);
        dataDTO.setProvince("江苏省");
        dataDTO.setCity("南京市");
        dataDTO.setStatus("active");
        dataDTO.setIdentificationTime(LocalDateTime.now());
        dataDTO.setDataSource("GF-2");
        dataDTO.setImageUuid("test-uuid-123");
        dataDTO.setNdviIndex(0.5);

        mockMvc.perform(post("/cement-plant/batch-data")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dataDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 2. 查询识别记录列表
        mockMvc.perform(get("/cement-plant/identification/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        // 3. 分页查询识别记录
        mockMvc.perform(get("/cement-plant/identification/page")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").exists())
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    void testUserPasswordUpdateFlow() throws Exception {
        // 1. 注册用户
        mockMvc.perform(post("/user/register")
                .param("username", "pwduser")
                .param("password", "originalpass"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 2. 登录获取token
        String loginResponse = mockMvc.perform(post("/user/login")
                .param("username", "pwduser")
                .param("password", "originalpass"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result loginResult = objectMapper.readValue(loginResponse, Result.class);
        Map<String, Object> loginData = (Map<String, Object>) loginResult.getData();
        String token = (String) loginData.get("token");

        // 3. 更新密码
        Map<String, String> passwordData = new HashMap<>();
        passwordData.put("oldPwd", "originalpass");
        passwordData.put("newPwd", "newpass");
        passwordData.put("rePwd", "newpass");

        mockMvc.perform(patch("/user/updatePwd")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 4. 使用新密码登录
        mockMvc.perform(post("/user/login")
                .param("username", "pwduser")
                .param("password", "newpass"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists());
    }

    @Test
    void testUserDownloadCountFlow() throws Exception {
        // 1. 注册用户
        mockMvc.perform(post("/user/register")
                .param("username", "downloaduser")
                .param("password", "downloadpass"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 2. 登录获取token
        String loginResponse = mockMvc.perform(post("/user/login")
                .param("username", "downloaduser")
                .param("password", "downloadpass"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result loginResult = objectMapper.readValue(loginResponse, Result.class);
        Map<String, Object> loginData = (Map<String, Object>) loginResult.getData();
        String token = (String) loginData.get("token");

        // 3. 检查下载限制
        mockMvc.perform(get("/user/download-limit-check")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());

        // 4. 获取每日下载计数
        mockMvc.perform(get("/user/daily-download-count")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());

        // 5. 增加下载计数
        mockMvc.perform(post("/user/increment-download")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testErrorHandlingFlow() throws Exception {
        // 1. 尝试登录不存在的用户
        mockMvc.perform(post("/user/login")
                .param("username", "nonexistent")
                .param("password", "password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("用户名不存在"));

        // 2. 尝试查询不存在的水泥厂
        mockMvc.perform(get("/cement-plant/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("水泥厂不存在"));

        // 3. 尝试添加重复名称的水泥厂
        CementPlant plant1 = new CementPlant();
        plant1.setPlantName("重复名称水泥厂");
        plant1.setLongitude(118.0);
        plant1.setLatitude(32.0);
        plant1.setProvince("江苏省");
        plant1.setCity("南京市");
        plant1.setStatus("active");

        // 第一次添加成功
        mockMvc.perform(post("/cement-plant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(plant1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 第二次添加失败
        CementPlant plant2 = new CementPlant();
        plant2.setPlantName("重复名称水泥厂");
        plant2.setLongitude(119.0);
        plant2.setLatitude(33.0);
        plant2.setProvince("浙江省");
        plant2.setCity("杭州市");
        plant2.setStatus("active");

        mockMvc.perform(post("/cement-plant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(plant2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("水泥厂名称已存在"));
    }

    @Test
    void testServiceLayerIntegration() {
        // 测试服务层之间的集成
        // 1. 创建用户
        userService.register("serviceuser", "servicepass");
        User user = userService.findByUserName("serviceuser");
        assertNotNull(user);
        assertEquals("serviceuser", user.getUsername());

        // 2. 创建水泥厂
        CementPlant plant = new CementPlant();
        plant.setPlantName("服务集成测试水泥厂");
        plant.setLongitude(118.0);
        plant.setLatitude(32.0);
        plant.setProvince("江苏省");
        plant.setCity("南京市");
        plant.setStatus("active");

        cementPlantService.addPlant(plant);
        assertNotNull(plant.getPlantId());

        // 3. 查询水泥厂
        CementPlant foundPlant = cementPlantService.findById(plant.getPlantId());
        assertNotNull(foundPlant);
        assertEquals("服务集成测试水泥厂", foundPlant.getPlantName());

        // 4. 获取统计信息
        Integer activeCount = cementPlantService.countActivePlants();
        assertNotNull(activeCount);
        assertTrue(activeCount >= 1);
    }

    @Test
    void testUtilityClassesIntegration() {
        // 测试工具类的集成使用
        // 1. MD5加密
        String password = "testpassword";
        String encryptedPassword = Md5Util.getMD5String(password);
        assertNotNull(encryptedPassword);
        assertEquals(32, encryptedPassword.length());

        // 验证密码
        boolean isValid = Md5Util.checkPassword(password, encryptedPassword);
        assertTrue(isValid);

        // 2. JWT Token生成和解析
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", "testuser");
        claims.put("id", 1);
        claims.put("roleCode", 1);

        String token = JwtUtil.genToken(claims);
        assertNotNull(token);
        assertFalse(token.isEmpty());

        Map<String, Object> parsedClaims = JwtUtil.parseToken(token);
        assertNotNull(parsedClaims);
        assertEquals("testuser", parsedClaims.get("username"));
        assertEquals(1, parsedClaims.get("id"));
        assertEquals(1, parsedClaims.get("roleCode"));
    }
}
