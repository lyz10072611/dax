#!/bin/bash

echo "========================================"
echo "水泥厂管理系统启动脚本"
echo "========================================"
echo

echo "检查Node.js环境..."
if ! command -v node &> /dev/null; then
    echo "错误: 未检测到Node.js环境，请先安装Node.js"
    echo "下载地址: https://nodejs.org/"
    exit 1
fi

echo "检查npm环境..."
if ! command -v npm &> /dev/null; then
    echo "错误: npm未正确安装"
    exit 1
fi

echo "检查前端项目依赖..."
cd static/myVue
if [ ! -d "node_modules" ]; then
    echo "正在安装前端依赖..."
    npm install
    if [ $? -ne 0 ]; then
        echo "错误: 前端依赖安装失败"
        exit 1
    fi
fi
cd ../..

echo
echo "启动后端Spring Boot应用..."
echo "后端将在端口8085启动"
echo "前端将在端口5173自动启动"
echo "浏览器将自动打开前端页面"
echo

java -jar target/*.jar
if [ $? -ne 0 ]; then
    echo "错误: 后端启动失败，请检查Java环境和jar文件"
    exit 1
fi
