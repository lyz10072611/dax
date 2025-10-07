#!/usr/bin/env python3
"""
测试数据管理脚本
用于清理测试数据和管理测试环境
"""

import requests
import json
import time
from typing import List, Dict, Any


class TestDataManager:
    """测试数据管理器"""
    
    def __init__(self, base_url: str = "http://localhost:8085"):
        self.base_url = base_url
        self.user_url = f"{base_url}/user"
        self.admin_url = f"{base_url}/admin/users"
        self.session = requests.Session()
        self.admin_token = None
    
    def login_as_admin(self, username: str = "admin", password: str = "admin123456"):
        """以管理员身份登录"""
        data = {
            'username': username,
            'password': password
        }
        
        try:
            response = self.session.post(f"{self.user_url}/login", data=data, timeout=10)
            result = response.json()
            
            if result.get('code') == 200:
                self.admin_token = result.get('data', {}).get('token')
                self.session.headers.update({'Authorization': self.admin_token})
                print(f"✅ 管理员登录成功: {username}")
                return True
            else:
                print(f"❌ 管理员登录失败: {result.get('message')}")
                return False
        except Exception as e:
            print(f"❌ 管理员登录异常: {e}")
            return False
    
    def get_all_test_users(self) -> List[Dict[str, Any]]:
        """获取所有测试用户"""
        if not self.admin_token:
            print("❌ 请先以管理员身份登录")
            return []
        
        try:
            # 分页获取所有用户
            all_users = []
            page_num = 1
            page_size = 100
            
            while True:
                params = {
                    'pageNum': page_num,
                    'pageSize': page_size
                }
                
                response = self.session.get(self.admin_url, params=params, timeout=10)
                result = response.json()
                
                if result.get('code') != 200:
                    break
                
                users = result.get('data', {}).get('rows', [])
                if not users:
                    break
                
                all_users.extend(users)
                page_num += 1
            
            # 筛选测试用户（用户名以test开头）
            test_users = [user for user in all_users if user.get('username', '').startswith('test')]
            return test_users
            
        except Exception as e:
            print(f"❌ 获取测试用户失败: {e}")
            return []
    
    def delete_test_users(self, users: List[Dict[str, Any]]):
        """删除测试用户"""
        if not self.admin_token:
            print("❌ 请先以管理员身份登录")
            return
        
        deleted_count = 0
        failed_count = 0
        
        for user in users:
            user_id = user.get('id')
            username = user.get('username')
            
            if not user_id:
                continue
            
            try:
                response = self.session.delete(f"{self.admin_url}/{user_id}", timeout=10)
                result = response.json()
                
                if result.get('code') == 200:
                    print(f"✅ 删除用户成功: {username}")
                    deleted_count += 1
                else:
                    print(f"❌ 删除用户失败: {username} - {result.get('message')}")
                    failed_count += 1
                    
            except Exception as e:
                print(f"❌ 删除用户异常: {username} - {e}")
                failed_count += 1
        
        print(f"\n📊 删除结果: 成功 {deleted_count} 个，失败 {failed_count} 个")
    
    def cleanup_test_data(self):
        """清理所有测试数据"""
        print("🧹 开始清理测试数据...")
        
        # 登录管理员
        if not self.login_as_admin():
            print("❌ 无法以管理员身份登录，跳过清理")
            return
        
        # 获取测试用户
        test_users = self.get_all_test_users()
        
        if not test_users:
            print("✅ 没有找到测试用户")
            return
        
        print(f"📋 找到 {len(test_users)} 个测试用户")
        
        # 确认删除
        response = input(f"确认删除这 {len(test_users)} 个测试用户? (y/N): ")
        if response.lower() != 'y':
            print("❌ 用户取消删除操作")
            return
        
        # 删除测试用户
        self.delete_test_users(test_users)
    
    def create_test_admin(self):
        """创建测试管理员用户"""
        print("👑 创建测试管理员用户...")
        
        admin_data = {
            'username': 'testadmin',
            'password': 'testadmin123456',
            'email': 'testadmin@test.com',
            'roleId': 1  # 管理员角色
        }
        
        try:
            response = self.session.post(self.admin_url, json=admin_data, timeout=10)
            result = response.json()
            
            if result.get('code') == 200:
                print("✅ 测试管理员创建成功")
                return True
            else:
                print(f"❌ 测试管理员创建失败: {result.get('message')}")
                return False
                
        except Exception as e:
            print(f"❌ 创建测试管理员异常: {e}")
            return False
    
    def check_server_status(self):
        """检查服务器状态"""
        try:
            response = self.session.get(f"{self.user_url}/userInfo", timeout=5)
            if response.status_code in [200, 401]:
                print("✅ 后端服务器运行正常")
                return True
        except Exception as e:
            print(f"❌ 后端服务器连接失败: {e}")
            return False
    
    def show_test_users(self):
        """显示所有测试用户"""
        print("📋 获取测试用户列表...")
        
        if not self.login_as_admin():
            return
        
        test_users = self.get_all_test_users()
        
        if not test_users:
            print("✅ 没有找到测试用户")
            return
        
        print(f"\n📊 找到 {len(test_users)} 个测试用户:")
        print("-" * 80)
        print(f"{'ID':<5} {'用户名':<20} {'邮箱':<30} {'角色':<10} {'状态':<10}")
        print("-" * 80)
        
        for user in test_users:
            user_id = user.get('id', 'N/A')
            username = user.get('username', 'N/A')
            email = user.get('email', 'N/A')
            role_id = user.get('roleId', 'N/A')
            status = '启用' if user.get('status', 1) == 1 else '禁用'
            
            print(f"{user_id:<5} {username:<20} {email:<30} {role_id:<10} {status:<10}")
        
        print("-" * 80)


def main():
    """主函数"""
    import argparse
    
    parser = argparse.ArgumentParser(description="测试数据管理工具")
    parser.add_argument(
        "--action",
        choices=["cleanup", "create-admin", "show-users", "check-server"],
        default="check-server",
        help="执行的操作"
    )
    parser.add_argument(
        "--base-url",
        default="http://localhost:8085",
        help="后端服务器地址"
    )
    
    args = parser.parse_args()
    
    manager = TestDataManager(args.base_url)
    
    print("🧪 水泥厂管理系统 - 测试数据管理工具")
    print("=" * 50)
    
    if args.action == "check-server":
        manager.check_server_status()
    elif args.action == "cleanup":
        manager.cleanup_test_data()
    elif args.action == "create-admin":
        manager.create_test_admin()
    elif args.action == "show-users":
        manager.show_test_users()


if __name__ == "__main__":
    main()
