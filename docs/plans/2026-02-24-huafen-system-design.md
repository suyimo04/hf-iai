# 花粉小组管理系统 - 设计文档

## 1. 项目概述

开发一个花粉小组管理系统，实现完整的招募流程闭环、角色权限控制、AI面试模拟、薪酬积分管理、活动管理及数据看板功能。

## 2. 技术栈

### 后端
- Java 17 + Spring Boot 3.x
- Spring Security + JWT
- JPA / Hibernate
- MySQL 8.0
- Lombok, MapStruct

### 前端
- Vue 3 + Vite + TypeScript
- Vue Router + Pinia
- Axios + Element Plus
- Tailwind CSS

## 3. 系统架构

```
前端 (Vue 3 + Vite, 端口 5173)
        │ REST API (JWT)
后端 (Spring Boot 3.x, 端口 8080)
        │
MySQL 8.0 (数据库: huafen_system)
```

## 4. 核心模块

| 模块 | 功能 |
|------|------|
| 用户认证 | 注册/登录/JWT鉴权/角色权限 |
| 招募管理 | 问卷报名→筛选→AI面试→复审→实习→转正 |
| AI面试 | 本地JSON题库+评分规则，预留真实API接口，前端可视化配置 |
| 积分系统 | 签到奖惩、任务积分、积分流水 |
| 薪酬管理 | 可配置薪酬池(默认2000)、积分换算、可编辑表格、批量保存 |
| 活动管理 | 创建/报名/手动签到/归档 |
| 数据看板 | 统计图表、日志审计 |

## 5. 数据库设计

### 5.1 用户表 users
| 字段 | 类型 | 描述 |
|------|------|------|
| id | BIGINT PK | 主键 |
| username | VARCHAR(50) | 用户名 |
| password | VARCHAR(255) | BCrypt加密密码 |
| nickname | VARCHAR(50) | 昵称 |
| email | VARCHAR(100) | 邮箱 |
| phone | VARCHAR(20) | 手机号 |
| role | VARCHAR(20) | 角色 |
| status | VARCHAR(20) | 状态 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 5.2 报名表 applications
| 字段 | 类型 | 描述 |
|------|------|------|
| id | BIGINT PK | 主键 |
| user_id | BIGINT FK | 关联users.id |
| status | VARCHAR(20) | 报名状态 |
| form_data | JSON | 问卷字段内容 |
| reviewer_id | BIGINT FK | 审核人 |
| review_comment | TEXT | 审核意见 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 5.3 面试记录表 interviews
| 字段 | 类型 | 描述 |
|------|------|------|
| id | BIGINT PK | 主键 |
| user_id | BIGINT FK | 关联users.id |
| application_id | BIGINT FK | 关联applications.id |
| score | INT | AI评分 |
| answers | JSON | 回答记录 |
| report | TEXT | 评分详情 |
| status | VARCHAR(20) | 面试状态 |
| created_at | DATETIME | 创建时间 |

### 5.4 面试题库表 interview_questions
| 字段 | 类型 | 描述 |
|------|------|------|
| id | BIGINT PK | 主键 |
| category | VARCHAR(50) | 分类 |
| question | TEXT | 问题内容 |
| options | JSON | 选项(选择题) |
| answer | TEXT | 参考答案 |
| score | INT | 分值 |
| sort_order | INT | 排序 |
| enabled | BOOLEAN | 是否启用 |

### 5.5 积分流水表 points
| 字段 | 类型 | 描述 |
|------|------|------|
| id | BIGINT PK | 主键 |
| user_id | BIGINT FK | 关联users.id |
| type | VARCHAR(50) | 类型(签到/任务/奖励/扣除) |
| amount | INT | 积分数(正负) |
| description | VARCHAR(255) | 描述 |
| created_at | DATETIME | 创建时间 |

### 5.6 薪酬记录表 salaries
| 字段 | 类型 | 描述 |
|------|------|------|
| id | BIGINT PK | 主键 |
| user_id | BIGINT FK | 关联users.id |
| period | VARCHAR(20) | 周期(如2026-02) |
| base_points | INT | 基础积分 |
| bonus_points | INT | 奖励积分 |
| deduction | INT | 扣除积分 |
| total_points | INT | 总积分 |
| coins | INT | 迷你币 |
| salary | DECIMAL(10,2) | 实际工资 |
| remark | TEXT | 备注 |
| status | VARCHAR(20) | 状态(draft/confirmed/archived) |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 5.7 活动表 activities
| 字段 | 类型 | 描述 |
|------|------|------|
| id | BIGINT PK | 主键 |
| title | VARCHAR(100) | 活动标题 |
| description | TEXT | 活动描述 |
| start_time | DATETIME | 开始时间 |
| end_time | DATETIME | 结束时间 |
| location | VARCHAR(255) | 地点 |
| max_participants | INT | 最大人数 |
| points_reward | INT | 签到奖励积分 |
| status | VARCHAR(20) | 状态 |
| created_by | BIGINT FK | 创建人 |
| created_at | DATETIME | 创建时间 |

### 5.8 活动报名签到表 activity_signups
| 字段 | 类型 | 描述 |
|------|------|------|
| id | BIGINT PK | 主键 |
| activity_id | BIGINT FK | 关联activities.id |
| user_id | BIGINT FK | 关联users.id |
| signed_in | BOOLEAN | 是否已签到 |
| sign_in_time | DATETIME | 签到时间 |
| created_at | DATETIME | 报名时间 |

### 5.9 系统配置表 system_config
| 字段 | 类型 | 描述 |
|------|------|------|
| id | BIGINT PK | 主键 |
| config_key | VARCHAR(50) | 配置键 |
| config_value | TEXT | 配置值 |
| description | VARCHAR(255) | 描述 |
| updated_at | DATETIME | 更新时间 |

### 5.10 操作日志表 operation_logs
| 字段 | 类型 | 描述 |
|------|------|------|
| id | BIGINT PK | 主键 |
| user_id | BIGINT FK | 操作人 |
| action | VARCHAR(50) | 操作类型 |
| target_type | VARCHAR(50) | 目标类型 |
| target_id | BIGINT | 目标ID |
| detail | TEXT | 详情 |
| ip | VARCHAR(50) | IP地址 |
| created_at | DATETIME | 创建时间 |

## 6. AI面试设计

### 6.1 架构
```
InterviewService
      │
      ▼
AIProvider接口
      │
      ├── LocalJSONProvider (默认启用)
      ├── ClaudeAPIProvider (预留)
      └── OpenAIAPIProvider (预留)
```

### 6.2 本地JSON评分逻辑
- 从interview_questions表加载题目
- 根据用户回答与参考答案匹配度评分
- 支持选择题自动评分、问答题关键词匹配
- 生成评分报告

### 6.3 前端可视化配置
- 题库管理：增删改查题目
- 评分规则：设置各类题目权重
- 场景设置：配置面试流程

## 7. 权限矩阵

| 功能 | ADMIN | LEADER | VICE_LEADER | MEMBER | INTERN | APPLICANT |
|------|-------|--------|-------------|--------|--------|-----------|
| 系统配置 | ✓ | - | - | - | - | - |
| 成员管理 | ✓ | ✓ | 查看 | - | - | - |
| 招募审核 | ✓ | ✓ | ✓ | - | - | - |
| AI面试配置 | ✓ | ✓ | - | - | - | - |
| 薪酬管理 | ✓ | ✓ | 查看 | 查看 | - | - |
| 活动管理 | ✓ | ✓ | ✓ | 查看 | 查看 | - |
| 报名 | - | - | - | - | - | ✓ |

## 8. API接口规范

### 8.1 统一返回格式
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 8.2 错误码
- 200: 成功
- 400: 参数错误
- 401: 未登录
- 403: 无权限
- 500: 系统异常

### 8.3 主要接口
| 路径 | 方法 | 功能 |
|------|------|------|
| /api/auth/register | POST | 用户注册 |
| /api/auth/login | POST | 登录 |
| /api/users | GET | 用户列表 |
| /api/users/{id}/role | PUT | 修改角色 |
| /api/applications | GET/POST | 报名管理 |
| /api/applications/{id}/review | POST | 审核报名 |
| /api/interviews/start | POST | 启动AI面试 |
| /api/interviews/{id} | GET | 获取面试结果 |
| /api/interview-questions | CRUD | 题库管理 |
| /api/points | GET | 积分流水 |
| /api/salaries | GET/POST | 薪酬管理 |
| /api/salaries/batch | POST | 批量保存 |
| /api/activities | CRUD | 活动管理 |
| /api/activities/{id}/signup | POST | 活动报名 |
| /api/activities/{id}/signin | POST | 活动签到 |
| /api/dashboard | GET | 数据看板 |
| /api/logs | GET | 操作日志 |
| /api/config | GET/PUT | 系统配置 |

## 9. 薪酬管理特殊逻辑

### 9.1 薪酬池机制
- 默认总额：2000元/月
- 可在系统配置中修改
- 按积分比例分配

### 9.2 前端可编辑表格
- 单元格点击编辑
- 修改缓存池(editedRows)
- 显示未保存数量
- 批量保存按钮

### 9.3 校验规则
- 单人工资：200~400元
- 总额：≤薪酬池总额
- 必须5名正式成员
- 校验失败高亮提示

## 10. 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | ADMIN |
| leader | admin123 | LEADER |
| teacher | admin123 | VICE_LEADER |
| intern | admin123 | INTERN |
