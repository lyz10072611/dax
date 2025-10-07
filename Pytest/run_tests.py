#!/usr/bin/env python3
"""
水泥厂管理系统 - 用户功能模块测试运行脚本
提供多种测试运行方式和选项
"""

import os
import sys
import subprocess
import argparse
import time
from pathlib import Path


class TestRunner:
    """测试运行器类"""
    
    def __init__(self):
        self.project_root = Path(__file__).parent
        self.test_file = self.project_root / "test_user_module.py"
        self.pytest_config = self.project_root / "pytest.ini"
    
    def check_dependencies(self):
        """检查依赖包"""
        required_packages = [
            'pytest',
            'requests',
            'pytest-html',
            'pytest-cov',
            'pytest-xdist'
        ]
        
        missing_packages = []
        for package in required_packages:
            try:
                __import__(package.replace('-', '_'))
            except ImportError:
                missing_packages.append(package)
        
        if missing_packages:
            print(f"❌ 缺少依赖包: {', '.join(missing_packages)}")
            print("请运行以下命令安装:")
            print(f"pip install {' '.join(missing_packages)}")
            return False
        
        print("✅ 所有依赖包已安装")
        return True
    
    def check_server_status(self):
        """检查后端服务器状态"""
        import requests
        
        try:
            response = requests.get("http://localhost:8085/user/userInfo", timeout=5)
            if response.status_code in [200, 401]:  # 401表示服务器运行但未认证
                print("✅ 后端服务器运行正常")
                return True
        except requests.exceptions.RequestException:
            pass
        
        print("❌ 后端服务器未运行或无法连接")
        print("请确保后端服务已启动在 http://localhost:8085")
        return False
    
    def run_basic_tests(self):
        """运行基础测试"""
        print("\n🧪 运行基础功能测试...")
        cmd = [
            sys.executable, "-m", "pytest",
            str(self.test_file),
            "-k", "TestUserBasicFunctions",
            "-v"
        ]
        return subprocess.run(cmd, cwd=self.project_root)
    
    def run_user_management_tests(self):
        """运行用户管理测试"""
        print("\n👤 运行用户管理测试...")
        cmd = [
            sys.executable, "-m", "pytest",
            str(self.test_file),
            "-k", "TestUserInfoManagement",
            "-v"
        ]
        return subprocess.run(cmd, cwd=self.project_root)
    
    def run_admin_tests(self):
        """运行管理员功能测试"""
        print("\n👑 运行管理员功能测试...")
        cmd = [
            sys.executable, "-m", "pytest",
            str(self.test_file),
            "-k", "TestAdminFunctions",
            "-v"
        ]
        return subprocess.run(cmd, cwd=self.project_root)
    
    def run_edge_case_tests(self):
        """运行边界条件测试"""
        print("\n🔍 运行边界条件和异常测试...")
        cmd = [
            sys.executable, "-m", "pytest",
            str(self.test_file),
            "-k", "TestEdgeCasesAndExceptions",
            "-v"
        ]
        return subprocess.run(cmd, cwd=self.project_root)
    
    def run_performance_tests(self):
        """运行性能测试"""
        print("\n⚡ 运行性能测试...")
        cmd = [
            sys.executable, "-m", "pytest",
            str(self.test_file),
            "-k", "TestPerformance",
            "-v"
        ]
        return subprocess.run(cmd, cwd=self.project_root)
    
    def run_all_tests(self):
        """运行所有测试"""
        print("\n🚀 运行所有测试...")
        cmd = [
            sys.executable, "-m", "pytest",
            str(self.test_file),
            "-v",
            "--tb=short"
        ]
        return subprocess.run(cmd, cwd=self.project_root)
    
    def run_tests_with_html_report(self):
        """运行测试并生成HTML报告"""
        print("\n📊 运行测试并生成HTML报告...")
        timestamp = time.strftime("%Y%m%d_%H%M%S")
        html_report = self.project_root / f"test_report_{timestamp}.html"
        
        cmd = [
            sys.executable, "-m", "pytest",
            str(self.test_file),
            "-v",
            "--html", str(html_report),
            "--self-contained-html"
        ]
        
        result = subprocess.run(cmd, cwd=self.project_root)
        
        if result.returncode == 0:
            print(f"✅ 测试报告已生成: {html_report}")
        
        return result
    
    def run_tests_with_coverage(self):
        """运行测试并生成覆盖率报告"""
        print("\n📈 运行测试并生成覆盖率报告...")
        cmd = [
            sys.executable, "-m", "pytest",
            str(self.test_file),
            "--cov=src",
            "--cov-report=html",
            "--cov-report=term-missing",
            "-v"
        ]
        return subprocess.run(cmd, cwd=self.project_root)
    
    def run_parallel_tests(self, workers=4):
        """并行运行测试"""
        print(f"\n🔄 并行运行测试 (workers={workers})...")
        cmd = [
            sys.executable, "-m", "pytest",
            str(self.test_file),
            "-n", str(workers),
            "-v"
        ]
        return subprocess.run(cmd, cwd=self.project_root)
    
    def run_smoke_tests(self):
        """运行冒烟测试（快速验证）"""
        print("\n💨 运行冒烟测试...")
        cmd = [
            sys.executable, "-m", "pytest",
            str(self.test_file),
            "-k", "test_server_connectivity or test_user_register_success or test_user_login_success",
            "-v"
        ]
        return subprocess.run(cmd, cwd=self.project_root)


def main():
    """主函数"""
    parser = argparse.ArgumentParser(description="水泥厂管理系统用户功能测试")
    parser.add_argument(
        "--mode", 
        choices=[
            "basic", "user", "admin", "edge", "performance", 
            "all", "html", "coverage", "parallel", "smoke"
        ],
        default="all",
        help="测试模式"
    )
    parser.add_argument(
        "--workers", 
        type=int, 
        default=4,
        help="并行测试的worker数量"
    )
    parser.add_argument(
        "--skip-checks", 
        action="store_true",
        help="跳过依赖和服务器检查"
    )
    
    args = parser.parse_args()
    
    runner = TestRunner()
    
    print("🧪 水泥厂管理系统 - 用户功能模块测试")
    print("=" * 50)
    
    # 检查依赖和服务器状态
    if not args.skip_checks:
        if not runner.check_dependencies():
            sys.exit(1)
        
        if not runner.check_server_status():
            print("\n⚠️  警告: 后端服务器未运行，某些测试可能失败")
            response = input("是否继续运行测试? (y/N): ")
            if response.lower() != 'y':
                sys.exit(1)
    
    # 根据模式运行测试
    start_time = time.time()
    
    try:
        if args.mode == "basic":
            result = runner.run_basic_tests()
        elif args.mode == "user":
            result = runner.run_user_management_tests()
        elif args.mode == "admin":
            result = runner.run_admin_tests()
        elif args.mode == "edge":
            result = runner.run_edge_case_tests()
        elif args.mode == "performance":
            result = runner.run_performance_tests()
        elif args.mode == "html":
            result = runner.run_tests_with_html_report()
        elif args.mode == "coverage":
            result = runner.run_tests_with_coverage()
        elif args.mode == "parallel":
            result = runner.run_parallel_tests(args.workers)
        elif args.mode == "smoke":
            result = runner.run_smoke_tests()
        else:  # all
            result = runner.run_all_tests()
        
        end_time = time.time()
        duration = end_time - start_time
        
        print("\n" + "=" * 50)
        if result.returncode == 0:
            print(f"✅ 测试完成! 耗时: {duration:.2f}秒")
        else:
            print(f"❌ 测试失败! 耗时: {duration:.2f}秒")
            print(f"退出码: {result.returncode}")
        
        sys.exit(result.returncode)
        
    except KeyboardInterrupt:
        print("\n\n⏹️  测试被用户中断")
        sys.exit(1)
    except Exception as e:
        print(f"\n❌ 测试运行出错: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
