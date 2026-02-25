# 花粉小组管理系统 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 构建完整的花粉小组管理系统，包含招募流程、AI面试、薪酬管理、活动管理等功能

**Architecture:** Spring Boot 3.x 后端 + Vue 3 前端，JWT认证，MySQL数据库，模块化设计支持AI接口扩展

**Tech Stack:** Java 17, Spring Boot 3.x, Spring Security, JPA, MySQL 8.0, Vue 3, Vite, TypeScript, Element Plus, Pinia

---

## Phase 1: 项目初始化与基础架构

### Task 1: 后端项目初始化 [backend]

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/huafen/system/HuafenApplication.java`
- Create: `backend/src/main/resources/application.yml`

**Steps:**
1. 创建 backend 目录和 Maven pom.xml，包含 Spring Boot 3.x、Spring Security、JPA、MySQL、Lombok 依赖
2. 创建主启动类 HuafenApplication.java
3. 创建 application.yml 配置数据库连接、JWT密钥、CORS设置
4. 运行 `cd backend && mvn compile` 验证编译通过

---

### Task 2: 数据库Schema创建 [backend]

**Files:**
- Create: `backend/src/main/resources/schema.sql`

**Steps:**
1. 创建包含10张表的schema.sql：users, applications, interviews, interview_questions, points, salaries, activities, activity_signups, system_config, operation_logs
2. 所有表包含适当的索引和外键约束
3. 运行SQL验证语法正确

---

### Task 3: 实体类创建 [backend]

**Files:**
- Create: `backend/src/main/java/com/huafen/system/entity/User.java`
- Create: `backend/src/main/java/com/huafen/system/entity/Application.java`
- Create: `backend/src/main/java/com/huafen/system/entity/Interview.java`
- Create: `backend/src/main/java/com/huafen/system/entity/InterviewQuestion.java`
- Create: `backend/src/main/java/com/huafen/system/entity/Point.java`
- Create: `backend/src/main/java/com/huafen/system/entity/Salary.java`
- Create: `backend/src/main/java/com/huafen/system/entity/Activity.java`
- Create: `backend/src/main/java/com/huafen/system/entity/ActivitySignup.java`
- Create: `backend/src/main/java/com/huafen/system/entity/SystemConfig.java`
- Create: `backend/src/main/java/com/huafen/system/entity/OperationLog.java`
- Create: `backend/src/main/java/com/huafen/system/entity/enums/Role.java`
- Create: `backend/src/main/java/com/huafen/system/entity/enums/UserStatus.java`
- Create: `backend/src/main/java/com/huafen/system/entity/enums/ApplicationStatus.java`

**Steps:**
1. 创建所有JPA实体类，使用Lombok注解
2. 创建枚举类：Role, UserStatus, ApplicationStatus等
3. 编译验证

---

### Task 4: Repository层创建 [backend]

**Files:**
- Create: `backend/src/main/java/com/huafen/system/repository/UserRepository.java`
- Create: `backend/src/main/java/com/huafen/system/repository/ApplicationRepository.java`
- Create: `backend/src/main/java/com/huafen/system/repository/InterviewRepository.java`
- Create: `backend/src/main/java/com/huafen/system/repository/InterviewQuestionRepository.java`
- Create: `backend/src/main/java/com/huafen/system/repository/PointRepository.java`
- Create: `backend/src/main/java/com/huafen/system/repository/SalaryRepository.java`
- Create: `backend/src/main/java/com/huafen/system/repository/ActivityRepository.java`
- Create: `backend/src/main/java/com/huafen/system/repository/ActivitySignupRepository.java`
- Create: `backend/src/main/java/com/huafen/system/repository/SystemConfigRepository.java`
- Create: `backend/src/main/java/com/huafen/system/repository/OperationLogRepository.java`

**Steps:**
1. 创建所有JpaRepository接口
2. 添加必要的自定义查询方法
3. 编译验证

---

### Task 5: 安全配置与JWT [backend]

**Files:**
- Create: `backend/src/main/java/com/huafen/system/security/JwtTokenProvider.java`
- Create: `backend/src/main/java/com/huafen/system/security/JwtAuthenticationFilter.java`
- Create: `backend/src/main/java/com/huafen/system/security/CustomUserDetailsService.java`
- Create: `backend/src/main/java/com/huafen/system/config/SecurityConfig.java`

**Steps:**
1. 创建JWT工具类：生成、验证、解析token
2. 创建JWT过滤器
3. 创建UserDetailsService实现
4. 创建SecurityConfig配置类，设置权限规则
5. 编译验证

---

## Phase 2: 核心业务Service层

### Task 6: 统一响应与异常处理 [backend]

**Files:**
- Create: `backend/src/main/java/com/huafen/system/common/Result.java`
- Create: `backend/src/main/java/com/huafen/system/common/ResultCode.java`
- Create: `backend/src/main/java/com/huafen/system/exception/BusinessException.java`
- Create: `backend/src/main/java/com/huafen/system/exception/GlobalExceptionHandler.java`

**Steps:**
1. 创建统一响应类Result<T>，包含code、message、data
2. 创建ResultCode枚举：SUCCESS(200), BAD_REQUEST(400), UNAUTHORIZED(401), FORBIDDEN(403), ERROR(500)
3. 创建业务异常类
4. 创建全局异常处理器@RestControllerAdvice

---

### Task 7: 认证服务 [backend]

**Files:**
- Create: `backend/src/main/java/com/huafen/system/dto/LoginRequest.java`
- Create: `backend/src/main/java/com/huafen/system/dto/RegisterRequest.java`
- Create: `backend/src/main/java/com/huafen/system/dto/LoginResponse.java`
- Create: `backend/src/main/java/com/huafen/system/service/AuthService.java`
- Create: `backend/src/main/java/com/huafen/system/service/impl/AuthServiceImpl.java`
- Create: `backend/src/main/java/com/huafen/system/controller/AuthController.java`

**Steps:**
1. 创建登录/注册DTO
2. 创建AuthService接口和实现：register、login
3. 新注册用户默认角色APPLICANT
4. 创建AuthController：POST /api/auth/register, POST /api/auth/login
5. 编译验证

---

### Task 8: 用户管理服务 [backend]

**Files:**
- Create: `backend/src/main/java/com/huafen/system/dto/UserDTO.java`
- Create: `backend/src/main/java/com/huafen/system/dto/UserQueryRequest.java`
- Create: `backend/src/main/java/com/huafen/system/service/UserService.java`
- Create: `backend/src/main/java/com/huafen/system/service/impl/UserServiceImpl.java`
- Create: `backend/src/main/java/com/huafen/system/controller/UserController.java`

**Steps:**
1. 创建用户DTO和查询请求类
2. 创建UserService：列表查询、详情、修改角色、修改状态
3. 创建UserController：GET /api/users, GET /api/users/{id}, PUT /api/users/{id}/role
4. 添加权限注解@PreAuthorize

---

### Task 9: 报名管理服务 [backend]

**Files:**
- Create: `backend/src/main/java/com/huafen/system/dto/ApplicationDTO.java`
- Create: `backend/src/main/java/com/huafen/system/dto/ApplicationSubmitRequest.java`
- Create: `backend/src/main/java/com/huafen/system/dto/ApplicationReviewRequest.java`
- Create: `backend/src/main/java/com/huafen/system/service/ApplicationService.java`
- Create: `backend/src/main/java/com/huafen/system/service/impl/ApplicationServiceImpl.java`
- Create: `backend/src/main/java/com/huafen/system/controller/ApplicationController.java`

**Steps:**
1. 创建报名相关DTO
2. 创建ApplicationService：提交报名、列表查询、审核
3. 报名状态流转：PENDING -> REVIEWING -> PASSED/REJECTED
4. 创建ApplicationController

---

### Task 10: AI面试服务 [backend]

**Files:**
- Create: `backend/src/main/java/com/huafen/system/service/ai/AIProvider.java`
- Create: `backend/src/main/java/com/huafen/system/service/ai/LocalJSONProvider.java`
- Create: `backend/src/main/java/com/huafen/system/service/ai/AIProviderFactory.java`
- Create: `backend/src/main/java/com/huafen/system/dto/InterviewStartRequest.java`
- Create: `backend/src/main/java/com/huafen/system/dto/InterviewAnswerRequest.java`
- Create: `backend/src/main/java/com/huafen/system/dto/InterviewResultDTO.java`
- Create: `backend/src/main/java/com/huafen/system/service/InterviewService.java`
- Create: `backend/src/main/java/com/huafen/system/service/impl/InterviewServiceImpl.java`
- Create: `backend/src/main/java/com/huafen/system/controller/InterviewController.java`
- Create: `backend/src/main/java/com/huafen/system/controller/InterviewQuestionController.java`

**Steps:**
1. 创建AIProvider接口：evaluate(answers) -> score, report
2. 创建LocalJSONProvider实现：从题库加载题目，关键词匹配评分
3. 创建AIProviderFactory：根据配置选择provider
4. 创建InterviewService：开始面试、提交答案、获取结果
5. 创建InterviewQuestionController：题库CRUD管理
6. 预留ClaudeAPIProvider和OpenAIAPIProvider接口

---

## Phase 3: 积分、薪酬与活动模块

### Task 11: 积分服务 [backend]

**Files:**
- Create: `backend/src/main/java/com/huafen/system/dto/PointDTO.java`
- Create: `backend/src/main/java/com/huafen/system/dto/PointAddRequest.java`
- Create: `backend/src/main/java/com/huafen/system/service/PointService.java`
- Create: `backend/src/main/java/com/huafen/system/service/impl/PointServiceImpl.java`
- Create: `backend/src/main/java/com/huafen/system/controller/PointController.java`

**Steps:**
1. 创建积分DTO和请求类
2. 创建PointService：添加积分、查询流水、统计用户总积分
3. 积分类型：CHECKIN(签到)、TASK(任务)、REWARD(奖励)、DEDUCTION(扣除)
4. 创建PointController：GET /api/points, POST /api/points

---

### Task 12: 薪酬服务 [backend]

**Files:**
- Create: `backend/src/main/java/com/huafen/system/dto/SalaryDTO.java`
- Create: `backend/src/main/java/com/huafen/system/dto/SalaryEditRequest.java`
- Create: `backend/src/main/java/com/huafen/system/dto/SalaryBatchSaveRequest.java`
- Create: `backend/src/main/java/com/huafen/system/service/SalaryService.java`
- Create: `backend/src/main/java/com/huafen/system/service/impl/SalaryServiceImpl.java`
- Create: `backend/src/main/java/com/huafen/system/controller/SalaryController.java`

**Steps:**
1. 创建薪酬DTO和请求类
2. 创建SalaryService：列表查询、单条编辑、批量保存、生成月度薪酬
3. 批量保存校验：单人200-400、总额≤薪酬池、至少5名正式成员
4. 创建SalaryController：GET /api/salaries, POST /api/salaries/edit, POST /api/salaries/batch

---

### Task 13: 活动服务 [backend]

**Files:**
- Create: `backend/src/main/java/com/huafen/system/dto/ActivityDTO.java`
- Create: `backend/src/main/java/com/huafen/system/dto/ActivityCreateRequest.java`
- Create: `backend/src/main/java/com/huafen/system/service/ActivityService.java`
- Create: `backend/src/main/java/com/huafen/system/service/impl/ActivityServiceImpl.java`
- Create: `backend/src/main/java/com/huafen/system/controller/ActivityController.java`

**Steps:**
1. 创建活动DTO和请求类
2. 创建ActivityService：CRUD、报名、签到、归档
3. 签到成功自动发放积分奖励
4. 创建ActivityController：完整CRUD + /signup + /signin

---

### Task 14: 系统配置与日志服务 [backend]

**Files:**
- Create: `backend/src/main/java/com/huafen/system/dto/ConfigDTO.java`
- Create: `backend/src/main/java/com/huafen/system/service/ConfigService.java`
- Create: `backend/src/main/java/com/huafen/system/service/impl/ConfigServiceImpl.java`
- Create: `backend/src/main/java/com/huafen/system/service/OperationLogService.java`
- Create: `backend/src/main/java/com/huafen/system/service/impl/OperationLogServiceImpl.java`
- Create: `backend/src/main/java/com/huafen/system/aspect/OperationLogAspect.java`
- Create: `backend/src/main/java/com/huafen/system/controller/ConfigController.java`
- Create: `backend/src/main/java/com/huafen/system/controller/LogController.java`

**Steps:**
1. 创建ConfigService：获取配置、更新配置
2. 默认配置：SALARY_POOL=2000
3. 创建OperationLogService和AOP切面自动记录操作日志
4. 创建ConfigController和LogController

---

### Task 15: 数据看板服务 [backend]

**Files:**
- Create: `backend/src/main/java/com/huafen/system/dto/DashboardDTO.java`
- Create: `backend/src/main/java/com/huafen/system/service/DashboardService.java`
- Create: `backend/src/main/java/com/huafen/system/service/impl/DashboardServiceImpl.java`
- Create: `backend/src/main/java/com/huafen/system/controller/DashboardController.java`

**Steps:**
1. 创建DashboardDTO：用户统计、报名统计、活动统计、积分统计
2. 创建DashboardService：聚合各类统计数据
3. 创建DashboardController：GET /api/dashboard

---

## Phase 4: 后端收尾与数据初始化

### Task 16: 默认账号初始化 [backend]

**Files:**
- Create: `backend/src/main/java/com/huafen/system/config/DataInitializer.java`
- Create: `backend/src/main/resources/data/interview_questions.json`

**Steps:**
1. 创建CommandLineRunner实现DataInitializer
2. 检查users表是否为空，为空则创建默认账号（BCrypt加密）
3. 初始化系统配置：SALARY_POOL=2000
4. 初始化面试题库JSON数据
5. 启动应用验证初始化成功

---

### Task 17: 后端集成测试 [backend]

**Files:**
- Create: `backend/src/test/java/com/huafen/system/AuthControllerTest.java`
- Create: `backend/src/test/java/com/huafen/system/UserControllerTest.java`

**Steps:**
1. 创建认证接口测试：注册、登录
2. 创建用户接口测试：列表、权限控制
3. 运行 `mvn test` 验证测试通过

---

## Phase 5: 前端项目初始化

### Task 18: 前端项目搭建 [frontend]

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/vite.config.ts`
- Create: `frontend/tsconfig.json`
- Create: `frontend/index.html`
- Create: `frontend/src/main.ts`
- Create: `frontend/src/App.vue`
- Create: `frontend/src/vite-env.d.ts`
- Create: `frontend/tailwind.config.js`
- Create: `frontend/postcss.config.js`
- Create: `frontend/src/styles/index.css`

**Steps:**
1. 创建Vite + Vue3 + TypeScript项目配置
2. 配置Element Plus、Tailwind CSS
3. 配置代理转发到后端8080端口
4. 运行 `cd frontend && npm install && npm run dev` 验证启动

---

### Task 19: 前端基础架构 [frontend]

**Files:**
- Create: `frontend/src/utils/request.ts`
- Create: `frontend/src/utils/auth.ts`
- Create: `frontend/src/stores/user.ts`
- Create: `frontend/src/stores/index.ts`
- Create: `frontend/src/types/index.ts`
- Create: `frontend/src/types/api.ts`

**Steps:**
1. 创建Axios封装：请求拦截器添加Token、响应拦截器处理401/403
2. 创建auth工具：getToken、setToken、removeToken
3. 创建Pinia用户Store：登录态、用户信息、权限
4. 创建TypeScript类型定义

---

### Task 20: 路由与布局 [frontend]

**Files:**
- Create: `frontend/src/router/index.ts`
- Create: `frontend/src/router/guards.ts`
- Create: `frontend/src/layouts/MainLayout.vue`
- Create: `frontend/src/layouts/components/Sidebar.vue`
- Create: `frontend/src/layouts/components/Header.vue`
- Create: `frontend/src/views/login/index.vue`

**Steps:**
1. 创建Vue Router配置，定义所有路由
2. 创建路由守卫：未登录跳转登录页、权限检查
3. 创建主布局：左侧菜单 + 顶部栏 + 内容区
4. 创建登录页面
5. 验证路由跳转正常

---

## Phase 6: 前端核心页面

### Task 21: 报名页面 [frontend]

**Files:**
- Create: `frontend/src/views/application/index.vue`
- Create: `frontend/src/api/application.ts`

**Steps:**
1. 创建动态表单报名页面
2. 表单字段：姓名、手机、邮箱、自我介绍、加入原因等
3. 创建报名API封装
4. 提交成功后显示状态

---

### Task 22: 成员管理页面 [frontend]

**Files:**
- Create: `frontend/src/views/members/index.vue`
- Create: `frontend/src/views/members/components/MemberTable.vue`
- Create: `frontend/src/views/members/components/RoleDialog.vue`
- Create: `frontend/src/api/user.ts`

**Steps:**
1. 创建成员列表页面：表格展示、搜索筛选
2. 创建角色修改弹窗
3. 权限控制：仅ADMIN/LEADER可修改角色
4. 创建用户API封装

---

### Task 23: 招募管理页面 [frontend]

**Files:**
- Create: `frontend/src/views/recruitment/index.vue`
- Create: `frontend/src/views/recruitment/components/ApplicationTable.vue`
- Create: `frontend/src/views/recruitment/components/ReviewDialog.vue`

**Steps:**
1. 创建报名列表页面：状态筛选、详情查看
2. 创建审核弹窗：通过/拒绝、审核意见
3. 状态流转展示

---

### Task 24: AI面试中心页面 [frontend]

**Files:**
- Create: `frontend/src/views/interview/index.vue`
- Create: `frontend/src/views/interview/components/InterviewList.vue`
- Create: `frontend/src/views/interview/components/InterviewDetail.vue`
- Create: `frontend/src/views/interview/config/index.vue`
- Create: `frontend/src/views/interview/config/QuestionManager.vue`
- Create: `frontend/src/api/interview.ts`

**Steps:**
1. 创建面试记录列表页面
2. 创建面试详情页：评分、报告展示
3. 创建题库配置页面：CRUD题目
4. 创建面试API封装

---

### Task 25: 活动管理页面 [frontend]

**Files:**
- Create: `frontend/src/views/activities/index.vue`
- Create: `frontend/src/views/activities/components/ActivityCard.vue`
- Create: `frontend/src/views/activities/components/ActivityForm.vue`
- Create: `frontend/src/views/activities/components/SignupList.vue`
- Create: `frontend/src/api/activity.ts`

**Steps:**
1. 创建活动列表页面：卡片式展示
2. 创建活动表单弹窗：创建/编辑
3. 创建报名签到管理页面
4. 创建活动API封装

---

## Phase 7: 薪酬与数据看板

### Task 26: 薪酬管理页面 [frontend]

**Files:**
- Create: `frontend/src/views/salary/index.vue`
- Create: `frontend/src/views/salary/components/SalaryTable.vue`
- Create: `frontend/src/views/salary/components/EditableCell.vue`
- Create: `frontend/src/api/salary.ts`

**Steps:**
1. 创建薪酬列表页面：可编辑表格
2. 创建EditableCell组件：单元格点击编辑
3. 实现修改缓存池editedRows
4. 显示未保存数量badge
5. 批量保存按钮：校验逻辑（单人200-400、总额≤2000、5名正式成员）
6. 校验失败高亮提示
7. 创建薪酬API封装

---

### Task 27: 数据看板页面 [frontend]

**Files:**
- Create: `frontend/src/views/dashboard/index.vue`
- Create: `frontend/src/views/dashboard/components/StatCard.vue`
- Create: `frontend/src/views/dashboard/components/ChartPanel.vue`
- Create: `frontend/src/api/dashboard.ts`

**Steps:**
1. 创建数据看板页面
2. 统计卡片：用户数、报名数、活动数、本月积分
3. 图表面板：使用ECharts展示趋势
4. 创建看板API封装

---

### Task 28: 系统配置页面 [frontend]

**Files:**
- Create: `frontend/src/views/settings/index.vue`
- Create: `frontend/src/views/settings/components/ConfigForm.vue`
- Create: `frontend/src/api/config.ts`

**Steps:**
1. 创建系统配置页面
2. 配置项：薪酬池总额、AI面试开关等
3. 仅ADMIN可访问
4. 创建配置API封装

---

### Task 29: 操作日志页面 [frontend]

**Files:**
- Create: `frontend/src/views/logs/index.vue`
- Create: `frontend/src/api/log.ts`

**Steps:**
1. 创建操作日志列表页面
2. 筛选：操作类型、时间范围、操作人
3. 创建日志API封装

---

### Task 30: UI主题与样式优化 [frontend]

**Files:**
- Modify: `frontend/src/styles/index.css`
- Create: `frontend/src/styles/variables.css`
- Create: `frontend/src/components/base/BaseCard.vue`
- Create: `frontend/src/components/base/BaseButton.vue`

**Steps:**
1. 定义CSS变量：主色#10b981、背景渐变、阴影
2. 创建BaseCard组件：半透明白色、柔和阴影、圆角12-16px
3. 创建BaseButton组件：统一按钮样式
4. 全局样式优化：OPPO玻璃感风格

---

## Phase 8: 集成测试与部署

### Task 31: 前后端联调测试 [general]

**Steps:**
1. 启动后端：`cd backend && mvn spring-boot:run`
2. 启动前端：`cd frontend && npm run dev`
3. 测试完整流程：注册→登录→报名→审核→面试→薪酬→活动
4. 修复联调问题

---

### Task 32: README文档 [general]

**Files:**
- Create: `README.md`
- Create: `backend/README.md`
- Create: `frontend/README.md`

**Steps:**
1. 创建项目根目录README：项目介绍、技术栈、快速开始
2. 创建后端README：环境要求、配置说明、启动命令
3. 创建前端README：环境要求、启动命令、目录结构

---

## 执行顺序建议

**可并行执行的任务组：**
- Task 1-5: 后端基础架构（顺序执行）
- Task 6-15: 后端业务模块（部分可并行）
- Task 18-20: 前端基础架构（顺序执行）
- Task 21-29: 前端页面（可并行）
- Task 30-32: 收尾工作

**依赖关系：**
- Task 6 依赖 Task 1-5
- Task 7-15 依赖 Task 6
- Task 16-17 依赖 Task 7-15
- Task 18 独立
- Task 19-20 依赖 Task 18
- Task 21-29 依赖 Task 19-20
- Task 30-32 依赖所有前置任务