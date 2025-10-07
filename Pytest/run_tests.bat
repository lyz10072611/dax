@echo off
chcp 65001 >nul
echo 🧪 水泥厂管理系统 - 用户功能模块测试
echo ================================================

REM 检查Python是否安装
python --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Python未安装或未添加到PATH
    echo 请先安装Python 3.8+
    pause
    exit /b 1
)

REM 检查后端服务状态
echo 🔍 检查后端服务状态...
curl -s http://localhost:8085/user/userInfo >nul 2>&1
if errorlevel 1 (
    echo ⚠️  后端服务可能未启动
    echo 请确保后端服务运行在 http://localhost:8085
    echo.
    set /p continue="是否继续运行测试? (y/N): "
    if /i not "%continue%"=="y" exit /b 1
) else (
    echo ✅ 后端服务运行正常
)

REM 安装依赖
echo.
echo 📦 安装测试依赖...
pip install -r requirements_test.txt
if errorlevel 1 (
    echo ❌ 依赖安装失败
    pause
    exit /b 1
)

REM 显示菜单
:menu
echo.
echo 请选择测试模式:
echo 1. 运行所有测试
echo 2. 基础功能测试
echo 3. 用户管理测试
echo 4. 管理员功能测试
echo 5. 边界条件测试
echo 6. 性能测试
echo 7. 冒烟测试
echo 8. 生成HTML报告
echo 9. 清理测试数据
echo 0. 退出
echo.
set /p choice="请输入选择 (0-9): "

if "%choice%"=="1" (
    echo 🚀 运行所有测试...
    python run_tests.py --mode all
) else if "%choice%"=="2" (
    echo 🧪 运行基础功能测试...
    python run_tests.py --mode basic
) else if "%choice%"=="3" (
    echo 👤 运行用户管理测试...
    python run_tests.py --mode user
) else if "%choice%"=="4" (
    echo 👑 运行管理员功能测试...
    python run_tests.py --mode admin
) else if "%choice%"=="5" (
    echo 🔍 运行边界条件测试...
    python run_tests.py --mode edge
) else if "%choice%"=="6" (
    echo ⚡ 运行性能测试...
    python run_tests.py --mode performance
) else if "%choice%"=="7" (
    echo 💨 运行冒烟测试...
    python run_tests.py --mode smoke
) else if "%choice%"=="8" (
    echo 📊 生成HTML报告...
    python run_tests.py --mode html
) else if "%choice%"=="9" (
    echo 🧹 清理测试数据...
    python test_data_manager.py --action cleanup
) else if "%choice%"=="0" (
    echo 👋 再见!
    exit /b 0
) else (
    echo ❌ 无效选择，请重新输入
    goto menu
)

echo.
echo 测试完成!
pause
goto menu
