# 花粉小组管理系统 - 前端

基于 Vue 3 + TypeScript + Vite 的现代化前端应用。

## 技术栈

- Vue 3.4+ (Composition API)
- TypeScript 5.x
- Vite 5.x
- Vue Router 4.x
- Pinia 2.x
- Element Plus 2.x
- Tailwind CSS 3.x
- ECharts 5.x
- Axios

## 目录结构

```
src/
├── api/              # API 接口封装
│   ├── auth.ts       # 认证接口
│   ├── member.ts     # 成员接口
│   ├── application.ts # 报名接口
│   ├── interview.ts  # 面试接口
│   ├── points.ts     # 积分接口
│   ├── salary.ts     # 薪酬接口
│   ├── activity.ts   # 活动接口
│   ├── dashboard.ts  # 看板接口
│   └── index.ts      # Axios 实例
├── components/       # 公共组件
│   └── common/       # 通用组件
├── layouts/          # 布局组件
│   └── MainLayout.vue
├── router/           # 路由配置
│   └── index.ts
├── stores/           # Pinia 状态管理
│   ├── user.ts       # 用户状态
│   └── app.ts        # 应用状态
├── styles/           # 样式文件
│   └── index.css     # Tailwind 入口
├── types/            # TypeScript 类型定义
│   └── index.ts
├── utils/            # 工具函数
│   └── index.ts
├── views/            # 页面组件
│   ├── login/        # 登录页
│   ├── members/      # 成员管理
│   ├── recruitment/  # 招募管理
│   ├── application/  # 报名申请
│   ├── interview/    # AI面试
│   ├── activities/   # 活动管理
│   ├── salary/       # 薪酬管理
│   ├── dashboard/    # 数据看板
│   ├── settings/     # 系统设置
│   ├── logs/         # 操作日志
│   └── Home.vue      # 首页
├── App.vue           # 根组件
├── main.ts           # 入口文件
└── vite-env.d.ts     # Vite 类型声明
```

## 页面组件说明

| 页面 | 路径 | 描述 |
|------|------|------|
| Login | /login | 登录/注册页面 |
| Home | / | 首页仪表盘 |
| Members | /members | 成员列表管理 |
| Recruitment | /recruitment | 招募管理（审核报名） |
| Application | /application | 报名申请表单 |
| Interview | /interview | AI面试系统 |
| Activities | /activities | 活动列表与管理 |
| Salary | /salary | 薪酬管理表格 |
| Dashboard | /dashboard | 数据统计看板 |
| Settings | /settings | 系统配置 |
| Logs | /logs | 操作日志查询 |

## 启动命令

```bash
# 安装依赖
npm install

# 开发环境启动
npm run dev

# 类型检查
npm run type-check

# 代码检查
npm run lint

# 生产构建
npm run build

# 预览构建结果
npm run preview
```

## 环境变量

创建 `.env.local` 文件配置环境变量：

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

## 构建部署

```bash
# 构建生产版本
npm run build

# 输出目录: dist/
# 将 dist 目录部署到 Nginx 或其他静态服务器
```

### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 开发规范

- 组件使用 `<script setup lang="ts">` 语法
- 状态管理使用 Pinia Composition API 风格
- 样式优先使用 Tailwind CSS 工具类
- API 请求统一通过 `src/api` 目录封装
- 类型定义统一放在 `src/types` 目录
