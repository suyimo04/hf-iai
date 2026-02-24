# 花粉小组管理系统

一个完整的花粉小组管理系统，实现招募流程闭环、角色权限控制、AI面试模拟、薪酬积分管理、活动管理及数据看板功能。

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2.x
- Spring Security + JWT
- JPA / Hibernate
- MySQL 8.0

### 前端
- Vue 3 + Vite + TypeScript
- Vue Router + Pinia
- Element Plus
- Tailwind CSS
- ECharts

## 快速开始

### 环境要求
- JDK 17+
- MySQL 8.0+
- Node.js 18+
- Maven 3.8+

### 数据库配置
1. 创建数据库：
   ```sql
   CREATE DATABASE huafen_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. 修改 `backend/src/main/resources/application.yml` 中的数据库连接信息

### 启动项目

#### Windows
```bash
start.bat
```

#### Linux/Mac
```bash
chmod +x start.sh
./start.sh
```

#### 手动启动
```bash
# 后端
cd backend
mvn spring-boot:run

# 前端（新终端）
cd frontend
npm install
npm run dev
```

### 访问地址
- 前端：http://localhost:5173
- 后端API：http://localhost:8080/api
- Swagger文档：http://localhost:8080/swagger-ui.html

## 默认账号

| 用户名 | 密码 | 角色 | 权限 |
|--------|------|------|------|
| admin | admin123 | 管理员 | 全部权限 |
| leader | admin123 | 组长 | 成员管理、招募、薪酬、活动 |
| teacher | admin123 | 副组长 | 查看成员、审核报名 |
| intern | admin123 | 实习成员 | 查看任务、积分、活动 |

## 功能模块

- 用户认证（注册/登录/JWT）
- 成员管理（角色/状态管理）
- 招募管理（报名→筛选→面试→转正）
- AI面试系统（题库配置、自动评分）
- 积分系统（签到、任务、奖惩）
- 薪酬管理（可编辑表格、批量保存）
- 活动管理（创建、报名、签到）
- 数据看板（统计图表）
- 系统配置（薪酬池等）
- 操作日志

## 项目结构

```
├── backend/          # Spring Boot 后端
├── frontend/         # Vue 3 前端
├── docs/             # 文档
├── start.sh          # Linux启动脚本
├── start.bat         # Windows启动脚本
└── README.md
```

## License

MIT
