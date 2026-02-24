# 花粉小组管理系统 - 后端

基于 Spring Boot 3.2 的后端服务，提供 RESTful API 接口。

## 技术栈

- Java 17
- Spring Boot 3.2.x
- Spring Security 6.x + JWT
- Spring Data JPA / Hibernate
- MySQL 8.0
- Maven 3.8+
- Swagger / OpenAPI 3.0

## 目录结构

```
src/main/java/com/huafen/system/
├── annotation/       # 自定义注解（操作日志等）
├── aspect/           # AOP切面（日志记录）
├── common/           # 通用类（响应封装、常量）
├── config/           # 配置类（Security、Swagger、CORS）
├── controller/       # 控制器层
├── dto/              # 数据传输对象
├── entity/           # JPA实体类
├── exception/        # 异常处理
├── repository/       # 数据访问层
├── security/         # 安全相关（JWT、UserDetails）
├── service/          # 业务逻辑层
└── HuafenApplication.java  # 启动类
```

## 配置说明

### application.yml 主要配置

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/huafen_system
    username: root
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

jwt:
  secret: your-secret-key
  expiration: 86400000  # 24小时
```

## API 接口列表

### 认证模块 `/api/auth`
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /login | 用户登录 |
| POST | /register | 用户注册 |
| GET | /me | 获取当前用户信息 |

### 成员管理 `/api/members`
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | / | 获取成员列表 |
| GET | /{id} | 获取成员详情 |
| PUT | /{id} | 更新成员信息 |
| PUT | /{id}/role | 修改成员角色 |
| PUT | /{id}/status | 修改成员状态 |

### 招募管理 `/api/applications`
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | / | 获取报名列表 |
| POST | / | 提交报名申请 |
| PUT | /{id}/status | 更新报名状态 |
| POST | /{id}/convert | 转正为正式成员 |

### AI面试 `/api/interview`
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /questions | 获取面试题目 |
| POST | /questions | 添加面试题目 |
| POST | /start | 开始面试 |
| POST | /submit | 提交面试答案 |
| GET | /records | 获取面试记录 |

### 积分系统 `/api/points`
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /my | 获取我的积分 |
| POST | /checkin | 每日签到 |
| GET | /records | 积分记录 |
| POST | /adjust | 调整积分（管理员） |

### 薪酬管理 `/api/salary`
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | / | 获取薪酬列表 |
| POST | /batch | 批量保存薪酬 |
| GET | /config | 获取薪酬配置 |
| PUT | /config | 更新薪酬配置 |

### 活动管理 `/api/activities`
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | / | 获取活动列表 |
| POST | / | 创建活动 |
| PUT | /{id} | 更新活动 |
| DELETE | /{id} | 删除活动 |
| POST | /{id}/register | 报名活动 |
| POST | /{id}/checkin | 活动签到 |

### 数据看板 `/api/dashboard`
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /stats | 获取统计数据 |
| GET | /charts | 获取图表数据 |

### 系统设置 `/api/settings`
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | / | 获取系统配置 |
| PUT | / | 更新系统配置 |

### 操作日志 `/api/logs`
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | / | 获取操作日志列表 |

## 启动命令

```bash
# 开发环境
mvn spring-boot:run

# 打包
mvn clean package -DskipTests

# 运行jar
java -jar target/huafen-system-1.0.0.jar
```

## 数据库初始化

首次启动会自动创建表结构（ddl-auto: update），并通过 DataInitializer 初始化默认数据：
- 默认管理员账号
- 默认角色配置
- 示例面试题目
