1. [backend] 添加Redis和WebSocket依赖
   在pom.xml中添加spring-boot-starter-data-redis, spring-boot-starter-websocket, lettuce-core依赖

2. [backend] 创建Redis配置类
   创建RedisConfig.java配置RedisTemplate和缓存管理器

3. [backend] 创建WebSocket配置类
   创建WebSocketConfig.java配置STOMP消息代理，端点/ws

4. [backend] 创建加密工具服务
   创建ConfigEncryptionService.java实现AES-256-GCM加密解密

5. [backend] 创建数据库Schema - 问卷系统表
   创建questionnaire, questionnaire_field, questionnaire_response表

6. [backend] 创建数据库Schema - AI面试系统表
   创建ai_interview_session, ai_interview_message, ai_interview_score表

7. [backend] 创建数据库Schema - 薪酬增强表
   创建member_flow_log, monthly_performance表，增强salary表

8. [backend] 创建数据库Schema - 权限菜单表
   创建menu, permission, role_menu, role_permission, role_change_log, permission_change_log表

9. [backend] 问卷系统实体类 (deps: 5)
   创建Questionnaire, QuestionnaireField, QuestionnaireResponse实体

10. [backend] 问卷系统Repository和Service (deps: 9)
    创建QuestionnaireRepository, QuestionnaireService及实现

11. [backend] 问卷系统Controller和DTO (deps: 10)
    创建QuestionnaireController, 相关DTO类

12. [backend] AI面试实体类 (deps: 6)
    创建AIInterviewSession, AIInterviewMessage, AIInterviewScore实体

13. [backend] AI Provider接口和实现 (deps: 12)
    创建AIProvider接口, OpenAICompatibleProvider, ClaudeProvider实现

14. [backend] AI面试Service (deps: 13)
    创建AIInterviewService，包含会话管理、消息处理、评分逻辑

15. [backend] AI面试WebSocket控制器 (deps: 3, 14)
    创建InterviewWebSocketController处理实时消息

16. [backend] AI面试REST控制器 (deps: 14)
    创建AIInterviewController提供REST API

17. [backend] 薪酬系统实体增强 (deps: 7)
    创建MemberFlowLog, MonthlyPerformance实体，增强Salary实体

18. [backend] 签到积分计算服务 (deps: 17)
    创建CheckinCalculationService实现精确的签到积分规则

19. [backend] 成员流转服务 (deps: 18)
    创建MemberFlowService实现转正、降级、开除逻辑

20. [backend] 薪酬池分配服务 (deps: 19)
    创建SalaryPoolService实现2000迷你币分配，200-400校验

21. [backend] 薪酬Controller增强 (deps: 20)
    增强SalaryController添加批量编辑、校验、月度生成接口

22. [backend] 配置中心实体增强 (deps: 4)
    增强SystemConfig实体添加加密标志，创建ConfigChangeLog实体

23. [backend] 配置缓存服务 (deps: 22)
    创建ConfigCacheService实现Redis缓存和热更新

24. [backend] 配置测试服务 (deps: 23)
    创建ConfigTestService实现AI、OSS、邮件连接测试

25. [backend] 配置Controller增强 (deps: 24)
    增强ConfigController添加测试连接、加密配置接口

26. [backend] 权限菜单实体 (deps: 8)
    创建Menu, Permission, RoleMenu, RolePermission等实体

27. [backend] 权限变更日志实体 (deps: 26)
    创建RoleChangeLog, PermissionChangeLog实体

28. [backend] 菜单权限Service (deps: 27)
    创建MenuService, PermissionService及实现

29. [backend] 菜单权限Controller (deps: 28)
    创建MenuController, PermissionController

30. [backend] 操作日志增强 (deps: 27)
    增强OperationLog添加日志分类，创建日志切面

31. [backend] 初始化菜单权限数据 (deps: 29)
    在DataInitializer中添加默认菜单和权限数据

32. [frontend] 三级侧边栏菜单组件 (deps: 31)
    创建SidebarMenu.vue支持三级菜单展开收起

33. [frontend] 面包屑导航组件
    创建Breadcrumb.vue根据路由自动生成面包屑

34. [frontend] 动态路由生成
    创建dynamicRouter.ts从菜单数据生成Vue Router路由

35. [frontend] 用户Store增强 (deps: 34)
    增强userStore添加permissions和menus状态管理

36. [frontend] 权限指令v-permission (deps: 35)
    创建permission.ts指令实现按钮级权限控制

37. [frontend] 问卷API模块
    创建questionnaire.ts API调用模块

38. [frontend] 问卷字段类型组件 (deps: 37)
    创建7种字段类型渲染组件：单选、多选、填空、日期、数字、下拉、分组

39. [frontend] 问卷设计器组件 (deps: 38)
    创建QuestionnaireDesigner.vue拖拽式表单设计器

40. [frontend] 字段属性编辑器 (deps: 39)
    创建FieldPropertyEditor.vue配置字段属性、校验、条件逻辑

41. [frontend] 问卷列表和预览页面 (deps: 40)
    创建QuestionnaireList.vue, QuestionnairePreview.vue

42. [frontend] 公开问卷填写页面 (deps: 41)
    创建PublicQuestionnaireForm.vue无需登录的问卷填写

43. [frontend] 问卷响应列表页面 (deps: 42)
    创建ResponseList.vue查看问卷提交数据

44. [frontend] AI面试API模块
    创建interview.ts API调用模块

45. [frontend] WebSocket服务 (deps: 44)
    创建websocket.ts STOMP客户端服务

46. [frontend] 面试聊天组件 (deps: 45)
    创建InterviewChat.vue实时聊天界面

47. [frontend] 消息气泡组件 (deps: 46)
    创建MessageBubble.vue支持流式消息显示

48. [frontend] 评分雷达图组件 (deps: 47)
    创建ScoreRadarChart.vue ECharts雷达图展示4维评分

49. [frontend] 面试回放页面 (deps: 48)
    创建InterviewReplay.vue只读模式查看历史面试

50. [frontend] 面试列表和统计页面 (deps: 49)
    创建InterviewList.vue, InterviewStatistics.vue

51. [frontend] 薪酬API模块增强
    增强salary.ts添加批量编辑、校验、流转接口

52. [frontend] 可编辑薪酬表格组件 (deps: 51)
    创建EditableSalaryTable.vue单元格编辑、实时校验

53. [frontend] 签到积分展示组件 (deps: 52)
    创建CheckinPointsDisplay.vue展示签到积分规则

54. [frontend] 薪酬池汇总卡片 (deps: 53)
    创建PoolSummaryCard.vue展示2000池分配情况

55. [frontend] 成员流转日志页面 (deps: 54)
    创建MemberFlowLogView.vue查看转正降级记录

56. [frontend] 薪酬批量编辑页面 (deps: 55)
    创建SalaryBatchEdit.vue带校验的批量编辑

57. [frontend] 配置API模块
    创建config.ts API调用模块

58. [frontend] AI配置表单组件 (deps: 57)
    创建AIConfigForm.vue配置AI服务商、密钥、模型

59. [frontend] OSS配置表单组件 (deps: 58)
    创建OSSConfigForm.vue配置七牛/阿里/腾讯OSS

60. [frontend] 邮件配置表单组件 (deps: 59)
    创建EmailConfigForm.vue配置SMTP服务

61. [frontend] 连接测试按钮组件 (deps: 60)
    创建ConfigTestButton.vue测试AI/OSS/邮件连接

62. [frontend] 配置中心页面 (deps: 61)
    创建ConfigCenter.vue Tab页切换各配置

63. [frontend] 菜单管理页面 (deps: 36)
    创建MenuManager.vue三级菜单可视化管理

64. [frontend] 权限管理页面 (deps: 63)
    创建PermissionManager.vue权限分配管理

65. [frontend] 日志列表增强 (deps: 64)
    增强LogList.vue添加日志分类筛选

66. [frontend] 角色变更日志页面 (deps: 65)
    创建RoleChangeLogView.vue查看角色变更历史

67. [frontend] 路由配置更新 (deps: 66)
    更新router/index.ts添加所有新页面路由

68. [general] 端到端测试 - 问卷流程 (deps: 43)
    测试问卷设计、发布、填写、查看响应完整流程

69. [general] 端到端测试 - AI面试流程 (deps: 50)
    测试AI面试WebSocket通信、评分、回放功能

70. [general] 端到端测试 - 薪酬流程 (deps: 56)
    测试薪酬计算、批量编辑、校验、成员流转

71. [general] 端到端测试 - 配置和权限 (deps: 67)
    测试配置加密热更新、动态菜单、权限控制
