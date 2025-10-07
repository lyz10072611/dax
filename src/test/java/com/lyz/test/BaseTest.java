package com.lyz.test;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 测试基类
 * 提供通用的测试配置和工具方法
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BaseTest {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;

    @BeforeEach
    void setUpBase() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
    }

    /**
     * 创建测试用的JWT Token
     */
    protected String createTestToken(String username, Integer userId, Integer roleCode) {
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("username", username);
        claims.put("id", userId);
        claims.put("roleCode", roleCode);
        return com.lyz.utils.JwtUtil.genToken(claims);
    }

    /**
     * 创建测试用的用户数据
     */
    protected com.lyz.pojo.User createTestUser(String username, String password) {
        com.lyz.pojo.User user = new com.lyz.pojo.User();
        user.setUsername(username);
        user.setPassword(com.lyz.utils.Md5Util.getMD5String(password));
        user.setNickname("测试用户");
        user.setEmail(username + "@test.com");
        user.setRoleId(2); // 普通用户角色
        user.setStatus(1);
        return user;
    }

    /**
     * 创建测试用的水泥厂数据
     */
    protected com.lyz.pojo.CementPlant createTestCementPlant(String plantName) {
        com.lyz.pojo.CementPlant plant = new com.lyz.pojo.CementPlant();
        plant.setPlantName(plantName);
        plant.setLongitude(118.0);
        plant.setLatitude(32.0);
        plant.setProvince("江苏省");
        plant.setCity("南京市");
        plant.setDistrict("鼓楼区");
        plant.setStatus("active");
        return plant;
    }

    /**
     * 创建测试用的水泥厂识别记录数据
     */
    protected com.lyz.pojo.CementPlantIdentification createTestIdentification(Long plantId) {
        com.lyz.pojo.CementPlantIdentification identification = new com.lyz.pojo.CementPlantIdentification();
        identification.setPlantId(plantId);
        identification.setLongitude(118.0);
        identification.setLatitude(32.0);
        identification.setIdentificationTime(java.time.LocalDateTime.now());
        identification.setDataSource("GF-2");
        identification.setImageUuid("test-uuid-" + System.currentTimeMillis());
        identification.setNdviIndex(0.5);
        identification.setProvince("江苏省");
        identification.setCity("南京市");
        identification.setDistrict("鼓楼区");
        return identification;
    }

    /**
     * 创建测试用的水泥厂数据DTO
     */
    protected com.lyz.pojo.CementPlantDataDTO createTestCementPlantDataDTO(String plantName) {
        com.lyz.pojo.CementPlantDataDTO dataDTO = new com.lyz.pojo.CementPlantDataDTO();
        dataDTO.setPlantName(plantName);
        dataDTO.setLongitude(118.0);
        dataDTO.setLatitude(32.0);
        dataDTO.setProvince("江苏省");
        dataDTO.setCity("南京市");
        dataDTO.setDistrict("鼓楼区");
        dataDTO.setStatus("active");
        dataDTO.setIdentificationTime(java.time.LocalDateTime.now());
        dataDTO.setDataSource("GF-2");
        dataDTO.setImageUuid("test-uuid-" + System.currentTimeMillis());
        dataDTO.setNdviIndex(0.5);
        return dataDTO;
    }

    /**
     * 将对象转换为JSON字符串
     */
    protected String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * 将JSON字符串转换为对象
     */
    protected <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }

    /**
     * 等待指定时间（用于测试异步操作）
     */
    protected void waitFor(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 生成随机字符串
     */
    protected String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 生成随机邮箱
     */
    protected String generateRandomEmail() {
        return generateRandomString(8) + "@test.com";
    }

    /**
     * 生成随机用户名
     */
    protected String generateRandomUsername() {
        return "testuser_" + generateRandomString(6);
    }
}
