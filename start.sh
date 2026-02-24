#!/bin/bash

# HF-IAI 启动脚本 (Linux/Mac)
# 使用方法: ./start.sh [backend|frontend|all]

set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$PROJECT_DIR/backend"
FRONTEND_DIR="$PROJECT_DIR/frontend"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查Java环境
check_java() {
    if ! command -v java &> /dev/null; then
        log_error "Java未安装，请先安装JDK 17+"
        exit 1
    fi
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        log_error "Java版本过低，需要JDK 17+，当前版本: $JAVA_VERSION"
        exit 1
    fi
    log_info "Java版本检查通过: $(java -version 2>&1 | head -n 1)"
}

# 检查Node环境
check_node() {
    if ! command -v node &> /dev/null; then
        log_error "Node.js未安装，请先安装Node.js 18+"
        exit 1
    fi
    NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
    if [ "$NODE_VERSION" -lt 18 ]; then
        log_error "Node.js版本过低，需要18+，当前版本: $(node -v)"
        exit 1
    fi
    log_info "Node.js版本检查通过: $(node -v)"
}

# 检查MySQL
check_mysql() {
    if ! command -v mysql &> /dev/null; then
        log_warn "MySQL客户端未安装，请确保MySQL服务已启动"
    else
        log_info "MySQL客户端已安装"
    fi
}

# 启动后端
start_backend() {
    log_info "启动后端服务..."
    cd "$BACKEND_DIR"

    if [ ! -f "pom.xml" ]; then
        log_error "后端项目不存在"
        exit 1
    fi

    # 检查Maven
    if command -v mvn &> /dev/null; then
        log_info "使用Maven启动..."
        mvn spring-boot:run
    elif [ -f "mvnw" ]; then
        log_info "使用Maven Wrapper启动..."
        chmod +x mvnw
        ./mvnw spring-boot:run
    else
        log_error "Maven未安装，请先安装Maven或使用Maven Wrapper"
        exit 1
    fi
}

# 启动前端
start_frontend() {
    log_info "启动前端服务..."
    cd "$FRONTEND_DIR"

    if [ ! -f "package.json" ]; then
        log_error "前端项目不存在"
        exit 1
    fi

    # 安装依赖
    if [ ! -d "node_modules" ]; then
        log_info "安装前端依赖..."
        npm install
    fi

    log_info "启动开发服务器..."
    npm run dev
}

# 同时启动前后端
start_all() {
    log_info "同时启动前后端服务..."

    # 后台启动后端
    cd "$BACKEND_DIR"
    if command -v mvn &> /dev/null; then
        mvn spring-boot:run &
    elif [ -f "mvnw" ]; then
        chmod +x mvnw
        ./mvnw spring-boot:run &
    fi
    BACKEND_PID=$!

    # 等待后端启动
    log_info "等待后端启动..."
    sleep 10

    # 启动前端
    cd "$FRONTEND_DIR"
    if [ ! -d "node_modules" ]; then
        npm install
    fi
    npm run dev &
    FRONTEND_PID=$!

    log_info "后端PID: $BACKEND_PID"
    log_info "前端PID: $FRONTEND_PID"
    log_info "按Ctrl+C停止所有服务"

    # 等待任意进程结束
    wait
}

# 主函数
main() {
    log_info "=========================================="
    log_info "    HF-IAI 花粉小组管理系统启动脚本"
    log_info "=========================================="

    MODE=${1:-all}

    case $MODE in
        backend)
            check_java
            check_mysql
            start_backend
            ;;
        frontend)
            check_node
            start_frontend
            ;;
        all)
            check_java
            check_node
            check_mysql
            start_all
            ;;
        *)
            echo "用法: $0 [backend|frontend|all]"
            echo "  backend  - 仅启动后端服务"
            echo "  frontend - 仅启动前端服务"
            echo "  all      - 同时启动前后端(默认)"
            exit 1
            ;;
    esac
}

main "$@"
