@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: HF-IAI 启动脚本 (Windows)
:: 使用方法: start.bat [backend|frontend|all]

set "PROJECT_DIR=%~dp0"
set "BACKEND_DIR=%PROJECT_DIR%backend"
set "FRONTEND_DIR=%PROJECT_DIR%frontend"

echo ==========================================
echo     HF-IAI 花粉小组管理系统启动脚本
echo ==========================================
echo.

set "MODE=%~1"
if "%MODE%"=="" set "MODE=all"

if "%MODE%"=="backend" goto :start_backend
if "%MODE%"=="frontend" goto :start_frontend
if "%MODE%"=="all" goto :start_all

echo 用法: start.bat [backend^|frontend^|all]
echo   backend  - 仅启动后端服务
echo   frontend - 仅启动前端服务
echo   all      - 同时启动前后端(默认)
goto :eof

:check_java
echo [INFO] 检查Java环境...
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java未安装，请先安装JDK 17+
    exit /b 1
)
for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set "JAVA_VER=%%i"
)
echo [INFO] Java版本: %JAVA_VER%
goto :eof

:check_node
echo [INFO] 检查Node.js环境...
where node >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Node.js未安装，请先安装Node.js 18+
    exit /b 1
)
for /f "tokens=1" %%i in ('node -v') do set "NODE_VER=%%i"
echo [INFO] Node.js版本: %NODE_VER%
goto :eof

:check_mysql
echo [INFO] 检查MySQL...
where mysql >nul 2>&1
if %errorlevel% neq 0 (
    echo [WARN] MySQL客户端未安装，请确保MySQL服务已启动
) else (
    echo [INFO] MySQL客户端已安装
)
goto :eof

:start_backend
call :check_java
if %errorlevel% neq 0 goto :eof
call :check_mysql

echo [INFO] 启动后端服务...
cd /d "%BACKEND_DIR%"

if not exist "pom.xml" (
    echo [ERROR] 后端项目不存在
    goto :eof
)

:: 检查Maven
where mvn >nul 2>&1
if %errorlevel% equ 0 (
    echo [INFO] 使用Maven启动...
    mvn spring-boot:run
    goto :eof
)

if exist "mvnw.cmd" (
    echo [INFO] 使用Maven Wrapper启动...
    call mvnw.cmd spring-boot:run
    goto :eof
)

echo [ERROR] Maven未安装，请先安装Maven
goto :eof

:start_frontend
call :check_node
if %errorlevel% neq 0 goto :eof

echo [INFO] 启动前端服务...
cd /d "%FRONTEND_DIR%"

if not exist "package.json" (
    echo [ERROR] 前端项目不存在
    goto :eof
)

:: 安装依赖
if not exist "node_modules" (
    echo [INFO] 安装前端依赖...
    call npm install
)

echo [INFO] 启动开发服务器...
call npm run dev
goto :eof

:start_all
call :check_java
if %errorlevel% neq 0 goto :eof
call :check_node
if %errorlevel% neq 0 goto :eof
call :check_mysql

echo [INFO] 同时启动前后端服务...
echo [INFO] 将在两个新窗口中分别启动前后端

:: 启动后端(新窗口)
start "HF-IAI Backend" cmd /k "cd /d "%BACKEND_DIR%" && mvn spring-boot:run"

:: 等待后端启动
echo [INFO] 等待后端启动(10秒)...
timeout /t 10 /nobreak >nul

:: 启动前端(新窗口)
start "HF-IAI Frontend" cmd /k "cd /d "%FRONTEND_DIR%" && npm run dev"

echo.
echo [INFO] 服务已在新窗口中启动
echo [INFO] 后端地址: http://localhost:8080
echo [INFO] 前端地址: http://localhost:5173
echo [INFO] API文档: http://localhost:8080/swagger-ui.html
echo.
echo 关闭服务请直接关闭对应的命令行窗口
goto :eof
