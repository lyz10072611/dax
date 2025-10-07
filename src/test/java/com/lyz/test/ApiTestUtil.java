package com.lyz.test;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

/**
 * 水泥厂管理系统接口测试工具类
 * 用于在Java代码中进行API接口测试
 */
public class ApiTestUtil {
    
    private static final String BASE_URL = "http://localhost:8085";
    private static final RestTemplate restTemplate = new RestTemplate();
    private static String token = null;
    
    /**
     * 测试用户注册
     */
    public static boolean testUserRegister() {
        try {
            String url = BASE_URL + "/user/register";
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("username", "testuser" + System.currentTimeMillis());
            params.add("password", "testpass123");
            params.add("email", "test" + System.currentTimeMillis() + "@example.com");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            System.out.println("用户注册测试: " + response.getBody());
            return response.getStatusCode() == HttpStatus.OK && response.getBody().contains("注册成功");
            
        } catch (Exception e) {
            System.err.println("用户注册测试失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试用户登录
     */
    public static boolean testUserLogin() {
        try {
            String url = BASE_URL + "/user/login";
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("username", "testuser");
            params.add("password", "testpass123");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            System.out.println("用户登录测试: " + response.getBody());
            
            // 提取token
            if (response.getBody() != null && response.getBody().contains("token")) {
                // 简单的token提取（实际项目中应该使用JSON解析）
                String body = response.getBody();
                int tokenStart = body.indexOf("\"token\":\"") + 9;
                int tokenEnd = body.indexOf("\"", tokenStart);
                if (tokenStart > 8 && tokenEnd > tokenStart) {
                    token = body.substring(tokenStart, tokenEnd);
                    System.out.println("获取到Token: " + token);
                }
            }
            
            return response.getStatusCode() == HttpStatus.OK && response.getBody().contains("token");
            
        } catch (Exception e) {
            System.err.println("用户登录测试失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试获取用户信息
     */
    public static boolean testUserInfo() {
        if (token == null) {
            System.out.println("用户信息测试跳过: 需要先登录获取token");
            return false;
        }
        
        try {
            String url = BASE_URL + "/user/userInfo";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            
            System.out.println("用户信息测试: " + response.getBody());
            return response.getStatusCode() == HttpStatus.OK && response.getBody().contains("username");
            
        } catch (Exception e) {
            System.err.println("用户信息测试失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试水泥厂列表查询
     */
    public static boolean testCementPlantList() {
        try {
            String url = BASE_URL + "/cement-plant/list";
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            System.out.println("水泥厂列表测试: " + response.getBody());
            return response.getStatusCode() == HttpStatus.OK && response.getBody().contains("code");
            
        } catch (Exception e) {
            System.err.println("水泥厂列表测试失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试水泥厂分页查询
     */
    public static boolean testCementPlantPage() {
        try {
            String url = BASE_URL + "/cement-plant/page?pageNum=1&pageSize=10";
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            System.out.println("水泥厂分页测试: " + response.getBody());
            return response.getStatusCode() == HttpStatus.OK && response.getBody().contains("code");
            
        } catch (Exception e) {
            System.err.println("水泥厂分页测试失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试数据上传
     */
    public static boolean testDataUpload() {
        if (token == null) {
            System.out.println("数据上传测试跳过: 需要先登录获取token");
            return false;
        }
        
        try {
            String url = BASE_URL + "/data-upload/cement-plant-data";
            
            Map<String, Object> data = new HashMap<>();
            data.put("plantName", "测试水泥厂" + System.currentTimeMillis());
            data.put("longitude", 116.3974);
            data.put("latitude", 39.9093);
            data.put("province", "北京市");
            data.put("city", "北京市");
            data.put("district", "朝阳区");
            data.put("status", "active");
            data.put("ndviIndex", 0.75);
            data.put("imageUuid", "test-image-" + System.currentTimeMillis());
            data.put("tifFilePath", "/test/path/test.tif");
            data.put("identificationTime", "2024-01-01 10:00:00");
            data.put("dataSource", "GF-2");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", token);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(data, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            System.out.println("数据上传测试: " + response.getBody());
            return response.getStatusCode() == HttpStatus.OK && response.getBody().contains("成功");
            
        } catch (Exception e) {
            System.err.println("数据上传测试失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试数据查询
     */
    public static boolean testDataQuery() {
        try {
            // 测试模糊搜索
            String url1 = BASE_URL + "/data-query/cement-plants/search?plantName=测试&limit=10";
            ResponseEntity<String> response1 = restTemplate.getForEntity(url1, String.class);
            System.out.println("模糊搜索测试: " + response1.getBody());
            
            // 测试地区查询
            String url2 = BASE_URL + "/data-query/cement-plants/by-region?province=北京市";
            ResponseEntity<String> response2 = restTemplate.getForEntity(url2, String.class);
            System.out.println("地区查询测试: " + response2.getBody());
            
            // 测试地理位置查询
            String url3 = BASE_URL + "/data-query/cement-plants/by-location?longitude=116.3974&latitude=39.9093&radiusKm=10";
            ResponseEntity<String> response3 = restTemplate.getForEntity(url3, String.class);
            System.out.println("地理位置查询测试: " + response3.getBody());
            
            return response1.getStatusCode() == HttpStatus.OK && 
                   response2.getStatusCode() == HttpStatus.OK && 
                   response3.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            System.err.println("数据查询测试失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试统计接口
     */
    public static boolean testStatistics() {
        try {
            String url = BASE_URL + "/data-query/statistics/overview";
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            System.out.println("统计接口测试: " + response.getBody());
            return response.getStatusCode() == HttpStatus.OK && response.getBody().contains("code");
            
        } catch (Exception e) {
            System.err.println("统计接口测试失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试文件管理接口
     */
    public static boolean testFileManagement() {
        try {
            String url = BASE_URL + "/files";
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            System.out.println("文件管理测试: " + response.getBody());
            return response.getStatusCode() == HttpStatus.OK && response.getBody().contains("code");
            
        } catch (Exception e) {
            System.err.println("文件管理测试失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 测试下载权限检查
     */
    public static boolean testDownloadPermission() {
        if (token == null) {
            System.out.println("下载权限测试跳过: 需要先登录获取token");
            return false;
        }
        
        try {
            String url = BASE_URL + "/data-download/download-status";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            
            System.out.println("下载权限测试: " + response.getBody());
            return response.getStatusCode() == HttpStatus.OK && response.getBody().contains("canDownload");
            
        } catch (Exception e) {
            System.err.println("下载权限测试失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 运行所有测试
     */
    public static void runAllTests() {
        System.out.println("========================================");
        System.out.println("水泥厂管理系统接口测试");
        System.out.println("========================================");
        
        int totalTests = 0;
        int passedTests = 0;
        
        // 执行各项测试
        totalTests++;
        if (testUserRegister()) passedTests++;
        
        totalTests++;
        if (testUserLogin()) passedTests++;
        
        totalTests++;
        if (testUserInfo()) passedTests++;
        
        totalTests++;
        if (testCementPlantList()) passedTests++;
        
        totalTests++;
        if (testCementPlantPage()) passedTests++;
        
        totalTests++;
        if (testDataUpload()) passedTests++;
        
        totalTests++;
        if (testDataQuery()) passedTests++;
        
        totalTests++;
        if (testStatistics()) passedTests++;
        
        totalTests++;
        if (testFileManagement()) passedTests++;
        
        totalTests++;
        if (testDownloadPermission()) passedTests++;
        
        // 输出测试结果
        System.out.println("\n========================================");
        System.out.println("测试结果汇总");
        System.out.println("========================================");
        System.out.println("总测试数: " + totalTests);
        System.out.println("通过: " + passedTests);
        System.out.println("失败: " + (totalTests - passedTests));
        
        if (passedTests == totalTests) {
            System.out.println("\n🎉 所有测试通过！");
        } else {
            System.out.println("\n❌ 有测试失败，请检查服务状态和配置");
        }
    }
    
    /**
     * 主方法，用于直接运行测试
     */
    public static void main(String[] args) {
        runAllTests();
    }
}
