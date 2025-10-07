"""
水泥厂管理系统 - 用户功能模块 pytest 测试框架
测试后端用户相关的所有API接口功能
"""

import pytest
import requests
import json
import time
from typing import Dict, Any, Optional
from dataclasses import dataclass
from datetime import datetime


@dataclass
class TestConfig:
    """测试配置类"""
    BASE_URL: str = "http://localhost:8085"
    USER_BASE_URL: str = "http://localhost:8085/user"
    ADMIN_BASE_URL: str = "http://localhost:8085/admin/users"
    TIMEOUT: int = 10
    RETRY_COUNT: int = 3
    RETRY_DELAY: float = 1.0


@dataclass
class TestUser:
    """测试用户数据类"""
    username: str
    password: str
    email: Optional[str] = None
    phone: Optional[str] = None
    role_id: int = 1  # 默认普通用户


class UserAPIClient:
    """用户API客户端类"""
    
    def __init__(self, base_url: str = TestConfig.BASE_URL):
        self.base_url = base_url
        self.user_url = f"{base_url}/user"
        self.admin_url = f"{base_url}/admin/users"
        self.session = requests.Session()
        self.token: Optional[str] = None
        self.user_info: Optional[Dict[str, Any]] = None
    
    def _make_request(self, method: str, url: str, **kwargs) -> requests.Response:
        """发送HTTP请求的通用方法"""
        headers = kwargs.get('headers', {})
        if self.token:
            headers['Authorization'] = self.token
        kwargs['headers'] = headers
        kwargs['timeout'] = TestConfig.TIMEOUT
        
        try:
            response = self.session.request(method, url, **kwargs)
            return response
        except requests.exceptions.RequestException as e:
            pytest.fail(f"请求失败: {e}")
    
    def register(self, user: TestUser) -> Dict[str, Any]:
        """用户注册"""
        data = {
            'username': user.username,
            'password': user.password,
            'email': user.email,
            'phone': user.phone
        }
        response = self._make_request('POST', f"{self.user_url}/register", data=data)
        return response.json()
    
    def login(self, username: str, password: str) -> Dict[str, Any]:
        """用户登录"""
        data = {
            'username': username,
            'password': password
        }
        response = self._make_request('POST', f"{self.user_url}/login", data=data)
        result = response.json()
        
        if result.get('code') == 200:
            self.token = result.get('data', {}).get('token')
            self.user_info = result.get('data', {})
        
        return result
    
    def logout(self) -> Dict[str, Any]:
        """用户登出"""
        if not self.token:
            return {'code': 401, 'message': '未登录'}
        
        response = self._make_request('POST', f"{self.user_url}/logout")
        result = response.json()
        
        if result.get('code') == 200:
            self.token = None
            self.user_info = None
        
        return result
    
    def get_user_info(self) -> Dict[str, Any]:
        """获取用户信息"""
        response = self._make_request('GET', f"{self.user_url}/userInfo")
        return response.json()
    
    def update_user_info(self, user_data: Dict[str, Any]) -> Dict[str, Any]:
        """更新用户信息"""
        response = self._make_request('PUT', f"{self.user_url}/update", 
                                    json=user_data)
        return response.json()
    
    def update_avatar(self, avatar_url: str) -> Dict[str, Any]:
        """更新头像"""
        data = {'avatarUrl': avatar_url}
        response = self._make_request('PATCH', f"{self.user_url}/updateAvatar", 
                                    data=data)
        return response.json()
    
    def update_password(self, old_pwd: str, new_pwd: str, re_pwd: str) -> Dict[str, Any]:
        """修改密码"""
        data = {
            'oldPwd': old_pwd,
            'newPwd': new_pwd,
            'rePwd': re_pwd
        }
        response = self._make_request('PATCH', f"{self.user_url}/updatePwd", 
                                    json=data)
        return response.json()
    
    def get_permissions(self) -> Dict[str, Any]:
        """获取用户权限"""
        response = self._make_request('GET', f"{self.user_url}/permissions")
        return response.json()
    
    # 管理员功能
    def admin_get_users(self, page_num: int = 1, page_size: int = 10, 
                       username: Optional[str] = None) -> Dict[str, Any]:
        """管理员分页查询用户"""
        params = {
            'pageNum': page_num,
            'pageSize': page_size
        }
        if username:
            params['username'] = username
        
        response = self._make_request('GET', self.admin_url, params=params)
        return response.json()
    
    def admin_add_user(self, user_data: Dict[str, Any]) -> Dict[str, Any]:
        """管理员新增用户"""
        response = self._make_request('POST', self.admin_url, json=user_data)
        return response.json()
    
    def admin_update_user(self, user_data: Dict[str, Any]) -> Dict[str, Any]:
        """管理员更新用户"""
        response = self._make_request('PUT', self.admin_url, json=user_data)
        return response.json()
    
    def admin_delete_user(self, user_id: int) -> Dict[str, Any]:
        """管理员删除用户"""
        response = self._make_request('DELETE', f"{self.admin_url}/{user_id}")
        return response.json()
    
    def admin_get_user_quota(self, user_id: int) -> Dict[str, Any]:
        """管理员查询用户下载配额"""
        response = self._make_request('GET', f"{self.admin_url}/{user_id}/quota")
        return response.json()
    
    def admin_set_user_quota(self, user_id: int, value: int, ttl_hours: int) -> Dict[str, Any]:
        """管理员设置用户下载配额"""
        data = {
            'value': value,
            'ttlHours': ttl_hours
        }
        response = self._make_request('PATCH', f"{self.admin_url}/{user_id}/quota", 
                                    json=data)
        return response.json()


class TestDataGenerator:
    """测试数据生成器"""
    
    @staticmethod
    def generate_test_user(username_suffix: str = "") -> TestUser:
        """生成测试用户数据"""
        timestamp = int(time.time())
        username = f"testuser{timestamp}{username_suffix}"
        return TestUser(
            username=username,
            password="test123456",
            email=f"{username}@test.com",
            phone=f"138{timestamp % 100000000:08d}"
        )
    
    @staticmethod
    def generate_admin_user() -> TestUser:
        """生成管理员用户数据"""
        timestamp = int(time.time())
        username = f"admin{timestamp}"
        return TestUser(
            username=username,
            password="admin123456",
            email=f"{username}@admin.com",
            role_id=1  # 管理员角色
        )


class TestAssertions:
    """测试断言工具类"""
    
    @staticmethod
    def assert_success_response(response: Dict[str, Any], expected_message: str = None):
        """断言成功响应"""
        assert response.get('code') == 200, f"期望成功响应(200)，实际: {response.get('code')}"
        assert response.get('businessCode') == 0, f"期望业务成功(0)，实际: {response.get('businessCode')}"
        if expected_message:
            assert expected_message in response.get('message', ''), f"期望消息包含: {expected_message}"
    
    @staticmethod
    def assert_error_response(response: Dict[str, Any], expected_code: int, 
                             expected_message: str = None):
        """断言错误响应"""
        assert response.get('code') == expected_code, f"期望错误码: {expected_code}，实际: {response.get('code')}"
        assert response.get('businessCode') == 1, f"期望业务失败(1)，实际: {response.get('businessCode')}"
        if expected_message:
            assert expected_message in response.get('message', ''), f"期望消息包含: {expected_message}"
    
    @staticmethod
    def assert_user_data(user_data: Dict[str, Any], expected_fields: list):
        """断言用户数据包含必要字段"""
        for field in expected_fields:
            assert field in user_data, f"用户数据缺少字段: {field}"
    
    @staticmethod
    def assert_pagination_data(page_data: Dict[str, Any]):
        """断言分页数据格式"""
        assert 'total' in page_data, "分页数据缺少total字段"
        assert 'rows' in page_data, "分页数据缺少rows字段"
        assert isinstance(page_data['total'], int), "total应为整数"
        assert isinstance(page_data['rows'], list), "rows应为列表"


# 测试夹具
@pytest.fixture
def api_client():
    """API客户端夹具"""
    return UserAPIClient()


@pytest.fixture
def test_user():
    """测试用户数据夹具"""
    return TestDataGenerator.generate_test_user()


@pytest.fixture
def admin_user():
    """管理员用户数据夹具"""
    return TestDataGenerator.generate_admin_user()


@pytest.fixture
def registered_user(api_client, test_user):
    """已注册用户夹具"""
    # 注册用户
    response = api_client.register(test_user)
    TestAssertions.assert_success_response(response, "注册成功")
    
    # 登录用户
    login_response = api_client.login(test_user.username, test_user.password)
    TestAssertions.assert_success_response(login_response, "登录成功")
    
    yield test_user
    
    # 清理：登出用户
    try:
        api_client.logout()
    except:
        pass


@pytest.fixture
def admin_client(api_client, admin_user):
    """管理员客户端夹具"""
    # 注册管理员用户
    response = api_client.register(admin_user)
    TestAssertions.assert_success_response(response, "注册成功")
    
    # 登录管理员用户
    login_response = api_client.login(admin_user.username, admin_user.password)
    TestAssertions.assert_success_response(login_response, "登录成功")
    
    yield api_client
    
    # 清理：登出管理员
    try:
        api_client.logout()
    except:
        pass


# 基础功能测试
class TestUserBasicFunctions:
    """用户基础功能测试类"""
    
    def test_server_connectivity(self, api_client):
        """测试服务器连通性"""
        try:
            response = requests.get(f"{TestConfig.BASE_URL}/user/userInfo", 
                                  timeout=TestConfig.TIMEOUT)
            # 即使返回401也是正常的，说明服务器在运行
            assert response.status_code in [200, 401], f"服务器连接失败: {response.status_code}"
        except requests.exceptions.RequestException as e:
            pytest.fail(f"无法连接到服务器: {e}")
    
    def test_user_register_success(self, api_client, test_user):
        """测试用户注册成功"""
        response = api_client.register(test_user)
        TestAssertions.assert_success_response(response, "注册成功")
        
        # 验证返回数据
        data = response.get('data', {})
        TestAssertions.assert_user_data(data, ['userId', 'username', 'email', 'roleCode'])
        assert data['username'] == test_user.username
        assert data['email'] == test_user.email
        assert data['roleCode'] == 1  # 默认普通用户
    
    def test_user_register_duplicate_username(self, api_client, test_user):
        """测试重复用户名注册"""
        # 第一次注册
        response1 = api_client.register(test_user)
        TestAssertions.assert_success_response(response1, "注册成功")
        
        # 第二次注册相同用户名
        response2 = api_client.register(test_user)
        TestAssertions.assert_error_response(response2, 409, "用户名已被占用")
    
    def test_user_register_duplicate_email(self, api_client, test_user):
        """测试重复邮箱注册"""
        # 第一次注册
        response1 = api_client.register(test_user)
        TestAssertions.assert_success_response(response1, "注册成功")
        
        # 创建相同邮箱的不同用户
        duplicate_user = TestDataGenerator.generate_test_user("_dup")
        duplicate_user.email = test_user.email
        
        response2 = api_client.register(duplicate_user)
        TestAssertions.assert_error_response(response2, 409, "邮箱已被占用")
    
    def test_user_register_invalid_username(self, api_client):
        """测试无效用户名注册"""
        invalid_user = TestUser(
            username="ab",  # 太短
            password="test123456",
            email="test@test.com"
        )
        response = api_client.register(invalid_user)
        TestAssertions.assert_error_response(response, 400)
    
    def test_user_register_invalid_password(self, api_client):
        """测试无效密码注册"""
        invalid_user = TestUser(
            username="testuser123",
            password="123",  # 太短
            email="test@test.com"
        )
        response = api_client.register(invalid_user)
        TestAssertions.assert_error_response(response, 400)
    
    def test_user_login_success(self, api_client, registered_user):
        """测试用户登录成功"""
        response = api_client.login(registered_user.username, registered_user.password)
        TestAssertions.assert_success_response(response, "登录成功")
        
        # 验证返回数据
        data = response.get('data', {})
        TestAssertions.assert_user_data(data, ['token', 'username', 'id', 'email', 'roleCode', 'expiresIn'])
        assert data['username'] == registered_user.username
        assert data['roleCode'] == 1
        assert data['expiresIn'] == 24 * 3600  # 24小时
    
    def test_user_login_wrong_password(self, api_client, registered_user):
        """测试错误密码登录"""
        response = api_client.login(registered_user.username, "wrongpassword")
        TestAssertions.assert_error_response(response, 401, "密码错误")
    
    def test_user_login_nonexistent_user(self, api_client):
        """测试不存在用户登录"""
        response = api_client.login("nonexistentuser", "password123")
        TestAssertions.assert_error_response(response, 404, "用户名不存在")
    
    def test_user_logout_success(self, api_client, registered_user):
        """测试用户登出成功"""
        response = api_client.logout()
        TestAssertions.assert_success_response(response, "登出成功")
        
        # 验证token已失效
        user_info_response = api_client.get_user_info()
        TestAssertions.assert_error_response(user_info_response, 401, "用户未登录")
    
    def test_get_user_info_success(self, api_client, registered_user):
        """测试获取用户信息成功"""
        response = api_client.get_user_info()
        TestAssertions.assert_success_response(response)
        
        # 验证返回数据
        data = response.get('data', {})
        TestAssertions.assert_user_data(data, ['id', 'username', 'email'])
        assert data['username'] == registered_user.username
        assert 'password' not in data  # 密码不应返回
    
    def test_get_user_info_without_login(self, api_client):
        """测试未登录获取用户信息"""
        response = api_client.get_user_info()
        TestAssertions.assert_error_response(response, 401, "用户未登录")


# 用户信息管理测试
class TestUserInfoManagement:
    """用户信息管理测试类"""
    
    def test_update_user_info_success(self, api_client, registered_user):
        """测试更新用户信息成功"""
        update_data = {
            'nickname': '测试昵称',
            'email': 'updated@test.com',
            'phone': '13912345678'
        }
        
        response = api_client.update_user_info(update_data)
        TestAssertions.assert_success_response(response, "更新成功")
    
    def test_update_user_info_without_login(self, api_client):
        """测试未登录更新用户信息"""
        update_data = {'nickname': '测试昵称'}
        response = api_client.update_user_info(update_data)
        TestAssertions.assert_error_response(response, 401, "用户未登录")
    
    def test_update_avatar_success(self, api_client, registered_user):
        """测试更新头像成功"""
        avatar_url = "https://example.com/avatar.jpg"
        response = api_client.update_avatar(avatar_url)
        TestAssertions.assert_success_response(response, "头像更新成功")
    
    def test_update_avatar_invalid_url(self, api_client, registered_user):
        """测试无效头像URL"""
        invalid_url = "invalid-url"
        response = api_client.update_avatar(invalid_url)
        TestAssertions.assert_error_response(response, 400, "头像URL格式不正确")
    
    def test_update_password_success(self, api_client, registered_user):
        """测试修改密码成功"""
        new_password = "newpassword123"
        response = api_client.update_password(
            registered_user.password, 
            new_password, 
            new_password
        )
        TestAssertions.assert_success_response(response, "密码修改成功")
        
        # 验证旧密码失效
        old_login_response = api_client.login(registered_user.username, registered_user.password)
        TestAssertions.assert_error_response(old_login_response, 401, "密码错误")
        
        # 验证新密码有效
        new_login_response = api_client.login(registered_user.username, new_password)
        TestAssertions.assert_success_response(new_login_response, "登录成功")
    
    def test_update_password_wrong_old_password(self, api_client, registered_user):
        """测试错误原密码修改密码"""
        response = api_client.update_password(
            "wrongpassword", 
            "newpassword123", 
            "newpassword123"
        )
        TestAssertions.assert_error_response(response, 401, "原密码不正确")
    
    def test_update_password_mismatch(self, api_client, registered_user):
        """测试新密码不匹配"""
        response = api_client.update_password(
            registered_user.password, 
            "newpassword123", 
            "differentpassword"
        )
        TestAssertions.assert_error_response(response, 400, "两次输入的密码不同")
    
    def test_update_password_same_as_old(self, api_client, registered_user):
        """测试新密码与旧密码相同"""
        response = api_client.update_password(
            registered_user.password, 
            registered_user.password, 
            registered_user.password
        )
        TestAssertions.assert_error_response(response, 400, "新修改的密码不能与旧密码相同")
    
    def test_get_permissions_success(self, api_client, registered_user):
        """测试获取用户权限成功"""
        response = api_client.get_permissions()
        TestAssertions.assert_success_response(response)
        
        # 验证返回数据
        data = response.get('data', {})
        TestAssertions.assert_user_data(data, ['roleCode', 'permissions'])
        assert data['roleCode'] == 1  # 普通用户


# 管理员功能测试
class TestAdminFunctions:
    """管理员功能测试类"""
    
    def test_admin_get_users_success(self, admin_client):
        """测试管理员分页查询用户成功"""
        response = admin_client.admin_get_users(page_num=1, page_size=5)
        TestAssertions.assert_success_response(response)
        
        # 验证分页数据格式
        data = response.get('data', {})
        TestAssertions.assert_pagination_data(data)
    
    def test_admin_get_users_with_username_filter(self, admin_client):
        """测试管理员按用户名筛选用户"""
        response = admin_client.admin_get_users(username="admin")
        TestAssertions.assert_success_response(response)
    
    def test_admin_add_user_success(self, admin_client):
        """测试管理员新增用户成功"""
        new_user_data = {
            'username': f'admin_created_{int(time.time())}',
            'password': 'admin123456',
            'email': f'admin_created_{int(time.time())}@test.com',
            'roleId': 1
        }
        
        response = admin_client.admin_add_user(new_user_data)
        TestAssertions.assert_success_response(response)
    
    def test_admin_update_user_success(self, admin_client):
        """测试管理员更新用户成功"""
        # 先获取一个用户
        users_response = admin_client.admin_get_users(page_num=1, page_size=1)
        TestAssertions.assert_success_response(users_response)
        
        users = users_response.get('data', {}).get('rows', [])
        if users:
            user = users[0]
            user['nickname'] = '管理员更新昵称'
            
            response = admin_client.admin_update_user(user)
            TestAssertions.assert_success_response(response)
    
    def test_admin_delete_user_success(self, admin_client):
        """测试管理员删除用户成功"""
        # 先创建一个用户
        new_user_data = {
            'username': f'to_delete_{int(time.time())}',
            'password': 'delete123456',
            'email': f'to_delete_{int(time.time())}@test.com',
            'roleId': 1
        }
        
        add_response = admin_client.admin_add_user(new_user_data)
        TestAssertions.assert_success_response(add_response)
        
        # 获取用户ID（这里简化处理，实际可能需要通过查询获取）
        # 由于无法直接获取ID，这个测试可能需要调整
        pytest.skip("需要实现获取新创建用户ID的逻辑")
    
    def test_admin_get_user_quota_success(self, admin_client):
        """测试管理员查询用户下载配额成功"""
        # 使用管理员自己的ID
        admin_id = admin_client.user_info.get('id') if admin_client.user_info else 1
        
        response = admin_client.admin_get_user_quota(admin_id)
        TestAssertions.assert_success_response(response)
        
        # 验证返回数据
        data = response.get('data', {})
        TestAssertions.assert_user_data(data, ['value', 'ttlSeconds'])
    
    def test_admin_set_user_quota_success(self, admin_client):
        """测试管理员设置用户下载配额成功"""
        # 使用管理员自己的ID
        admin_id = admin_client.user_info.get('id') if admin_client.user_info else 1
        
        response = admin_client.admin_set_user_quota(admin_id, 100, 24)
        TestAssertions.assert_success_response(response)
        
        # 验证设置是否生效
        quota_response = admin_client.admin_get_user_quota(admin_id)
        TestAssertions.assert_success_response(quota_response)
        
        quota_data = quota_response.get('data', {})
        assert quota_data.get('value') == 100
    
    def test_admin_functions_without_permission(self, api_client, registered_user):
        """测试非管理员访问管理员功能"""
        # 使用普通用户尝试访问管理员功能
        response = api_client.admin_get_users()
        TestAssertions.assert_error_response(response, 403, "无权限")


# 边界条件和异常测试
class TestEdgeCasesAndExceptions:
    """边界条件和异常测试类"""
    
    def test_login_rate_limiting(self, api_client, registered_user):
        """测试登录频率限制"""
        # 连续多次错误登录
        for i in range(6):  # 超过5次限制
            response = api_client.login(registered_user.username, "wrongpassword")
            if i < 5:
                TestAssertions.assert_error_response(response, 401, "密码错误")
            else:
                TestAssertions.assert_error_response(response, 429, "登录失败次数过多")
    
    def test_invalid_token_access(self, api_client):
        """测试无效token访问"""
        api_client.token = "invalid_token_12345"
        response = api_client.get_user_info()
        TestAssertions.assert_error_response(response, 401)
    
    def test_missing_required_parameters(self, api_client):
        """测试缺少必要参数"""
        # 测试注册时缺少用户名
        response = api_client.register(TestUser(username="", password="test123456"))
        TestAssertions.assert_error_response(response, 400)
    
    def test_sql_injection_attempt(self, api_client):
        """测试SQL注入尝试"""
        malicious_user = TestUser(
            username="'; DROP TABLE users; --",
            password="test123456",
            email="test@test.com"
        )
        response = api_client.register(malicious_user)
        # 应该被参数验证拦截
        TestAssertions.assert_error_response(response, 400)
    
    def test_xss_attempt(self, api_client):
        """测试XSS攻击尝试"""
        xss_user = TestUser(
            username="testuser123",
            password="test123456",
            email="<script>alert('xss')</script>@test.com"
        )
        response = api_client.register(xss_user)
        # 应该被邮箱格式验证拦截
        TestAssertions.assert_error_response(response, 400)


# 性能测试
class TestPerformance:
    """性能测试类"""
    
    def test_concurrent_registration(self, api_client):
        """测试并发注册"""
        import threading
        import queue
        
        results = queue.Queue()
        
        def register_user(user_suffix):
            user = TestDataGenerator.generate_test_user(f"_concurrent_{user_suffix}")
            response = api_client.register(user)
            results.put((user_suffix, response))
        
        # 创建多个线程并发注册
        threads = []
        for i in range(5):
            thread = threading.Thread(target=register_user, args=(i,))
            threads.append(thread)
            thread.start()
        
        # 等待所有线程完成
        for thread in threads:
            thread.join()
        
        # 验证结果
        success_count = 0
        while not results.empty():
            suffix, response = results.get()
            if response.get('code') == 200:
                success_count += 1
        
        assert success_count == 5, f"期望5个成功注册，实际: {success_count}"
    
    def test_response_time(self, api_client, registered_user):
        """测试响应时间"""
        import time
        
        start_time = time.time()
        response = api_client.get_user_info()
        end_time = time.time()
        
        response_time = end_time - start_time
        assert response_time < 2.0, f"响应时间过长: {response_time:.2f}秒"
        TestAssertions.assert_success_response(response)


if __name__ == "__main__":
    # 运行测试
    pytest.main([
        "-v",  # 详细输出
        "--tb=short",  # 简短的错误追踪
        "--maxfail=5",  # 最多失败5个测试后停止
        __file__
    ])
