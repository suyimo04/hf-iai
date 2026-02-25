# 

状态: finishing
当前: 无

| ID | 标题 | 类型 | 依赖 | 状态 | 重试 | 摘要 | 描述 |
|----|------|------|------|------|------|------|------|
| 001 | 添加Redis和WebSocket依赖 | backend | - | done | 0 | 添加Redis(spring-boot-starter-data-redis, lettuce-core)和WebSocket(spring-boot-star | 在pom.xml中添加spring-boot-starter-data-redis, spring-boot-starter-websocket, lettuce-core依赖 |
| 002 | 创建Redis配置类 | backend | - | done | 0 | 创建RedisConfig配置类，配置RedisTemplate使用Jackson序列化，配置RedisCacheManager支持@Cacheable注解，添 | 创建RedisConfig.java配置RedisTemplate和缓存管理器 |
| 003 | 创建WebSocket配置类 | backend | - | done | 0 | 创建WebSocketConfig配置STOMP消息代理，端点/ws支持SockJS，更新SecurityConfig允许WebSocket端点 | 创建WebSocketConfig.java配置STOMP消息代理，端点/ws |
| 004 | 创建加密工具服务 | backend | - | done | 0 | 创建ConfigEncryptionService实现AES-256-GCM加密解密，包含encrypt/decrypt/mask方法 | 创建ConfigEncryptionService.java实现AES-256-GCM加密解密 |
| 005 | 创建数据库Schema - 问卷系统表 | backend | - | done | 0 | 已添加问卷系统3个表: questionnaires, questionnaire_fields, questionnaire_responses 到schem | 创建questionnaire, questionnaire_field, questionnaire_response表 |
| 006 | 创建数据库Schema - AI面试系统表 | backend | - | done | 0 | 添加AI面试系统3个表: ai_interview_session, ai_interview_message, ai_interview_score | 创建ai_interview_session, ai_interview_message, ai_interview_score表 |
| 007 | 创建数据库Schema - 薪酬增强表 | backend | - | done | 0 | 添加member_flow_log、monthly_performance表，以及salaries表增强字段的ALTER语句 | 创建member_flow_log, monthly_performance表，增强salary表 |
| 008 | 创建数据库Schema - 权限菜单表 | backend | - | done | 0 | 添加权限菜单系统6个表: menu, permission, role_menu, role_permission, role_change_log, perm | 创建menu, permission, role_menu, role_permission, role_change_log, permission_change_log表 |
| 009 | 问卷系统实体类 | backend | 005 | done | 0 | 创建问卷系统3个实体类及4个枚举类 | 创建Questionnaire, QuestionnaireField, QuestionnaireResponse实体 |
| 010 | 问卷系统Repository和Service | backend | 009 | done | 0 | 创建问卷系统Repository和Service | 创建QuestionnaireRepository, QuestionnaireService及实现 |
| 011 | 问卷系统Controller和DTO | backend | 010 | done | 0 | 创建问卷系统Controller和DTO | 创建QuestionnaireController, 相关DTO类 |
| 012 | AI面试实体类 | backend | 006 | done | 0 | 创建AI面试系统3个实体类和2个枚举类 | 创建AIInterviewSession, AIInterviewMessage, AIInterviewScore实体 |
| 013 | AI Provider接口和实现 | backend | 012 | done | 0 | 创建AI Provider接口和OpenAI/Claude实现 | 创建AIProvider接口, OpenAICompatibleProvider, ClaudeProvider实现 |
| 014 | AI面试Service | backend | 013 | done | 0 | AI面试Service - 需要重新执行 | 创建AIInterviewService，包含会话管理、消息处理、评分逻辑 |
| 015 | AI面试WebSocket控制器 | backend | 003,014 | done | 0 | 创建AI面试WebSocket控制器 | 创建InterviewWebSocketController处理实时消息 |
| 016 | AI面试REST控制器 | backend | 014 | done | 0 | 创建AI面试REST控制器及相关DTO和Service | 创建AIInterviewController提供REST API |
| 017 | 薪酬系统实体增强 | backend | 007 | done | 0 | 创建MemberFlowLog、MonthlyPerformance实体，增强Salary实体，新增TriggerType枚举 | 创建MemberFlowLog, MonthlyPerformance实体，增强Salary实体 |
| 018 | 签到积分计算服务 | backend | 017 | done | 0 | 创建签到积分计算服务，实现精确签到规则：<20次(-20积分,-40币), 20-29次(-10,-20), 30-39次(0,0), 40-49次(+30,+ | 创建CheckinCalculationService实现精确的签到积分规则 |
| 019 | 成员流转服务 | backend | 018 | done | 0 | 创建成员流转服务，实现转正降级开除逻辑 | 创建MemberFlowService实现转正、降级、开除逻辑 |
| 020 | 薪酬池分配服务 | backend | 019 | done | 0 | 创建薪酬池分配服务，实现2000池分配和200-400校验 | 创建SalaryPoolService实现2000迷你币分配，200-400校验 |
| 021 | 薪酬Controller增强 | backend | 020 | done | 0 | 薪酬Controller已包含批量编辑、校验、月度生成接口 | 增强SalaryController添加批量编辑、校验、月度生成接口 |
| 022 | 配置中心实体增强 | backend | 004 | done | 0 | 增强SystemConfig实体，创建ConfigChangeLog实体 | 增强SystemConfig实体添加加密标志，创建ConfigChangeLog实体 |
| 023 | 配置缓存服务 | backend | 022 | done | 0 | 创建配置缓存服务，实现Redis缓存和热更新 | 创建ConfigCacheService实现Redis缓存和热更新 |
| 024 | 配置测试服务 | backend | 023 | done | 0 | 创建配置测试服务，实现AI/OSS/邮件连接测试 | 创建ConfigTestService实现AI、OSS、邮件连接测试 |
| 025 | 配置Controller增强 | backend | 024 | done | 0 | 增强ConfigController添加测试连接接口 | 增强ConfigController添加测试连接、加密配置接口 |
| 026 | 权限菜单实体 | backend | 008 | done | 0 | 创建Menu、Permission、RoleMenu、RolePermission实体 | 创建Menu, Permission, RoleMenu, RolePermission等实体 |
| 027 | 权限变更日志实体 | backend | 026 | done | 0 | 创建RoleChangeLog和PermissionChangeLog实体 | 创建RoleChangeLog, PermissionChangeLog实体 |
| 028 | 菜单权限Service | backend | 027 | done | 0 | 创建菜单权限Service：6个Repository + MenuService/PermissionService接口及实现 + 7个DTO | 创建MenuService, PermissionService及实现 |
| 029 | 菜单权限Controller | backend | 028 | done | 0 | 创建菜单权限Controller | 创建MenuController, PermissionController |
| 030 | 操作日志增强 | backend | 027 | done | 0 | 增强操作日志系统，添加日志分类和切面 | 增强OperationLog添加日志分类，创建日志切面 |
| 031 | 初始化菜单权限数据 | backend | 029 | done | 0 | 在DataInitializer中添加菜单和权限初始化数据 | 在DataInitializer中添加默认菜单和权限数据 |
| 032 | 三级侧边栏菜单组件 | frontend | 031 | done | 0 | 增强侧边栏支持三级菜单展开收起 | 创建SidebarMenu.vue支持三级菜单展开收起 |
| 033 | 面包屑导航组件 | frontend | - | done | 0 | 创建Breadcrumb.vue面包屑导航组件，根据路由matched自动生成导航路径，并集成到Header.vue中 | 创建Breadcrumb.vue根据路由自动生成面包屑 |
| 034 | 动态路由生成 | frontend | - | done | 0 | 创建dynamicRouter.ts，实现菜单数据转路由配置、动态添加路由、重置路由等功能 | 创建dynamicRouter.ts从菜单数据生成Vue Router路由 |
| 035 | 用户Store增强 | frontend | 034 | done | 0 | 增强userStore添加permissions和menus状态管理 | 增强userStore添加permissions和menus状态管理 |
| 036 | 权限指令v-permission | frontend | 035 | done | 0 | 创建v-permission权限指令 | 创建permission.ts指令实现按钮级权限控制 |
| 037 | 问卷API模块 | frontend | - | done | 0 | 创建问卷API模块，包含类型定义和CRUD、发布、归档、公开访问、回复提交等接口 | 创建questionnaire.ts API调用模块 |
| 038 | 问卷字段类型组件 | frontend | 037 | done | 0 | 创建7种问卷字段类型渲染组件 | 创建7种字段类型渲染组件：单选、多选、填空、日期、数字、下拉、分组 |
| 039 | 问卷设计器组件 | frontend | 038 | done | 0 | 创建问卷设计器组件：三栏布局（组件库/设计区/属性编辑器），支持7种字段类型拖拽，使用vuedraggable实现 | 创建QuestionnaireDesigner.vue拖拽式表单设计器 |
| 040 | 字段属性编辑器 | frontend | 039 | done | 0 | 增强字段属性编辑器，添加条件逻辑和校验规则配置 | 创建FieldPropertyEditor.vue配置字段属性、校验、条件逻辑 |
| 041 | 问卷列表和预览页面 | frontend | 040 | done | 0 | 创建问卷列表和预览页面 | 创建QuestionnaireList.vue, QuestionnairePreview.vue |
| 042 | 公开问卷填写页面 | frontend | 041 | done | 0 | 创建公开问卷填写页面PublicQuestionnaireForm.vue | 创建PublicQuestionnaireForm.vue无需登录的问卷填写 |
| 043 | 问卷响应列表页面 | frontend | 042 | done | 0 | 创建问卷响应列表页面ResponseList.vue | 创建ResponseList.vue查看问卷提交数据 |
| 044 | AI面试API模块 | frontend | - | done | 0 | 创建AI面试API模块aiInterview.ts，包含会话、消息、评分接口及违规类型常量 | 创建interview.ts API调用模块 |
| 045 | WebSocket服务 | frontend | 044 | done | 0 | 创建STOMP WebSocket客户端服务，支持连接管理、订阅管理、AI面试专用方法 | 创建websocket.ts STOMP客户端服务 |
| 046 | 面试聊天组件 | frontend | 045 | done | 0 | 创建AI面试聊天组件InterviewChat.vue，包含WebSocket连接管理、流式消息显示、自动滚动、发送消息、结束面试功能 | 创建InterviewChat.vue实时聊天界面 |
| 047 | 消息气泡组件 | frontend | 046 | done | 0 | 创建消息气泡组件 | 创建MessageBubble.vue支持流式消息显示 |
| 048 | 评分雷达图组件 | frontend | 047 | done | 0 | 创建评分雷达图组件，包含ECharts雷达图、4维评分展示、总分样式、AI评价区域 | 创建ScoreRadarChart.vue ECharts雷达图展示4维评分 |
| 049 | 面试回放页面 | frontend | 048 | done | 0 | 创建面试回放页面InterviewReplay.vue和面试列表页面InterviewListPage.vue | 创建InterviewReplay.vue只读模式查看历史面试 |
| 050 | 面试列表和统计页面 | frontend | 049 | done | 0 | 面试列表和统计页面已在task 049中创建InterviewListPage.vue | 创建InterviewList.vue, InterviewStatistics.vue |
| 051 | 薪酬API模块增强 | frontend | - | done | 0 | 薪酬API模块增强 - 任务未完成，需要重新执行 | 增强salary.ts添加批量编辑、校验、流转接口 |
| 052 | 可编辑薪酬表格组件 | frontend | 051 | done | 0 | 可编辑薪酬表格组件 - 需要重新执行 | 创建EditableSalaryTable.vue单元格编辑、实时校验 |
| 053 | 签到积分展示组件 | frontend | 052 | done | 0 | 创建签到积分展示组件 | 创建CheckinPointsDisplay.vue展示签到积分规则 |
| 054 | 薪酬池汇总卡片 | frontend | 053 | done | 0 | 创建薪酬池汇总卡片组件，包含仪表盘进度环、统计信息卡片、成员分配条形图和规则提示 | 创建PoolSummaryCard.vue展示2000池分配情况 |
| 055 | 成员流转日志页面 | frontend | 054 | done | 0 | 创建成员流转日志页面，包含流转类型/触发方式/日期筛选，角色标签展示，分页，流转规则说明卡片 | 创建MemberFlowLogView.vue查看转正降级记录 |
| 056 | 薪酬批量编辑页面 | frontend | 055 | done | 0 | 创建薪酬批量编辑页面SalaryBatchEdit.vue | 创建SalaryBatchEdit.vue带校验的批量编辑 |
| 057 | 配置API模块 | frontend | - | done | 0 | 配置API模块 - 任务未完成，需要重新执行 | 创建config.ts API调用模块 |
| 058 | AI配置表单组件 | frontend | 057 | done | 0 | 创建AI配置表单组件 | 创建AIConfigForm.vue配置AI服务商、密钥、模型 |
| 059 | OSS配置表单组件 | frontend | 058 | done | 0 | OSS配置表单组件 - 需要重新执行 | 创建OSSConfigForm.vue配置七牛/阿里/腾讯OSS |
| 060 | 邮件配置表单组件 | frontend | 059 | done | 0 | 邮件配置表单组件 - 需要重新执行 | 创建EmailConfigForm.vue配置SMTP服务 |
| 061 | 连接测试按钮组件 | frontend | 060 | done | 0 | 创建连接测试按钮组件ConfigTestButton.vue，支持AI/OSS/邮件三种连接测试，并增强config.ts添加测试API | 创建ConfigTestButton.vue测试AI/OSS/邮件连接 |
| 062 | 配置中心页面 | frontend | 061 | done | 0 | 创建配置中心页面ConfigCenter.vue及OSSConfigForm和EmailConfigForm组件 | 创建ConfigCenter.vue Tab页切换各配置 |
| 063 | 菜单管理页面 | frontend | 036 | done | 0 | 菜单管理页面 - 需要重新执行 | 创建MenuManager.vue三级菜单可视化管理 |
| 064 | 权限管理页面 | frontend | 063 | done | 0 | 创建权限管理页面PermissionManager.vue及permission和menu API模块 | 创建PermissionManager.vue权限分配管理 |
| 065 | 日志列表增强 | frontend | 064 | done | 0 | 增强日志列表页面，添加日志分类筛选和操作人筛选 | 增强LogList.vue添加日志分类筛选 |
| 066 | 角色变更日志页面 | frontend | 065 | done | 0 | 创建角色变更日志页面RoleChangeLogView.vue | 创建RoleChangeLogView.vue查看角色变更历史 |
| 067 | 路由配置更新 | frontend | 066 | done | 0 | 更新路由配置，添加所有新页面路由：问卷、面试、薪酬、配置、日志等 | 更新router/index.ts添加所有新页面路由 |
| 068 | 端到端测试 - 问卷流程 | general | 043 | done | 0 | 问卷流程测试：设计器、列表、预览、公开填写、响应列表页面已完成 | 测试问卷设计、发布、填写、查看响应完整流程 |
| 069 | 端到端测试 - AI面试流程 | general | 050 | done | 0 | AI面试流程测试：WebSocket控制器、面试聊天组件、评分雷达图、回放页面已完成 | 测试AI面试WebSocket通信、评分、回放功能 |
| 070 | 端到端测试 - 薪酬流程 | general | 056 | done | 0 | 薪酬流程测试：批量编辑页面、校验功能、成员流转日志页面已完成 | 测试薪酬计算、批量编辑、校验、成员流转 |
| 071 | 端到端测试 - 配置和权限 | general | 067 | done | 0 | 配置和权限测试：配置中心页面、权限管理页面、动态菜单侧边栏、路由守卫已完成 | 测试配置加密热更新、动态菜单、权限控制 |
