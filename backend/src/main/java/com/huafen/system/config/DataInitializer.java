package com.huafen.system.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huafen.system.entity.*;
import com.huafen.system.entity.enums.Role;
import com.huafen.system.entity.enums.UserStatus;
import com.huafen.system.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 数据初始化器
 * 初始化默认账号、系统配置、面试题库
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SystemConfigRepository configRepository;
    private final InterviewQuestionRepository questionRepository;
    private final MenuRepository menuRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) {
        initUsers();
        initConfigs();
        initQuestions();
        initMenusAndPermissions();
    }

    /**
     * 初始化默认用户
     */
    private void initUsers() {
        if (userRepository.count() == 0) {
            log.info("初始化默认用户...");
            String encodedPassword = passwordEncoder.encode("admin123");

            // 创建管理员
            User admin = User.builder()
                    .username("admin")
                    .password(encodedPassword)
                    .nickname("系统管理员")
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(admin);

            // 创建组长
            User leader = User.builder()
                    .username("leader")
                    .password(encodedPassword)
                    .nickname("组长")
                    .role(Role.LEADER)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(leader);

            // 创建副组长
            User teacher = User.builder()
                    .username("teacher")
                    .password(encodedPassword)
                    .nickname("副组长")
                    .role(Role.VICE_LEADER)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(teacher);

            // 创建实习成员
            User intern = User.builder()
                    .username("intern")
                    .password(encodedPassword)
                    .nickname("实习成员")
                    .role(Role.INTERN)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(intern);

            log.info("默认用户初始化完成，共创建4个账号");
        }
    }

    /**
     * 初始化系统配置
     */
    private void initConfigs() {
        if (configRepository.count() == 0) {
            log.info("初始化系统配置...");

            // 工资池配置
            SystemConfig salaryPool = SystemConfig.builder()
                    .configKey("SALARY_POOL")
                    .configValue("2000")
                    .description("工资池总额")
                    .build();
            configRepository.save(salaryPool);

            // AI面试开关
            SystemConfig aiInterview = SystemConfig.builder()
                    .configKey("AI_INTERVIEW_ENABLED")
                    .configValue("true")
                    .description("是否启用AI面试")
                    .build();
            configRepository.save(aiInterview);

            // 签到积分
            SystemConfig checkinPoints = SystemConfig.builder()
                    .configKey("CHECKIN_POINTS")
                    .configValue("10")
                    .description("每日签到获得的积分")
                    .build();
            configRepository.save(checkinPoints);

            log.info("系统配置初始化完成");
        }
    }

    /**
     * 初始化面试题库
     */
    private void initQuestions() {
        if (questionRepository.count() == 0) {
            log.info("初始化面试题库...");
            try {
                ClassPathResource resource = new ClassPathResource("data/interview_questions.json");
                InputStream inputStream = resource.getInputStream();
                List<Map<String, Object>> questions = objectMapper.readValue(
                        inputStream, new TypeReference<List<Map<String, Object>>>() {});

                for (Map<String, Object> q : questions) {
                    InterviewQuestion question = InterviewQuestion.builder()
                            .category((String) q.get("category"))
                            .question((String) q.get("question"))
                            .score((Integer) q.get("score"))
                            .sortOrder((Integer) q.get("sortOrder"))
                            .enabled(true)
                            .build();

                    // 处理选择题的选项
                    if (q.containsKey("options")) {
                        question.setOptions(objectMapper.writeValueAsString(q.get("options")));
                        question.setAnswer((String) q.get("answer"));
                    }

                    // 处理问答题的关键词
                    if (q.containsKey("keywords")) {
                        question.setAnswer(objectMapper.writeValueAsString(q.get("keywords")));
                    }

                    questionRepository.save(question);
                }

                log.info("面试题库初始化完成，共导入{}道题目", questions.size());
            } catch (Exception e) {
                log.error("初始化面试题库失败", e);
            }
        }
    }

    /**
     * 初始化菜单和权限数据
     */
    private void initMenusAndPermissions() {
        if (menuRepository.count() == 0) {
            log.info("初始化菜单和权限数据...");

            // 创建一级菜单
            Menu dashboard = createMenu(null, "仪表盘", "/dashboard", "Dashboard", "Odometer", 1);
            Menu members = createMenu(null, "成员管理", "/members", null, "User", 2);
            Menu recruitment = createMenu(null, "招新管理", "/recruitment", null, "UserFilled", 3);
            Menu activities = createMenu(null, "活动管理", "/activities", "Activities", "Calendar", 4);
            Menu salary = createMenu(null, "薪酬管理", "/salary", null, "Money", 5);
            Menu questionnaire = createMenu(null, "问卷管理", "/questionnaire", null, "Document", 6);
            Menu interview = createMenu(null, "面试管理", "/interview", null, "ChatDotRound", 7);
            Menu settings = createMenu(null, "系统设置", "/settings", null, "Setting", 8);
            Menu logs = createMenu(null, "日志管理", "/logs", null, "Tickets", 9);

            // 成员管理子菜单
            Menu memberList = createMenu(members.getId(), "成员列表", "/members/list", "MemberList", null, 1);
            Menu memberFlow = createMenu(members.getId(), "成员流转", "/members/flow", "MemberFlow", null, 2);

            // 招新管理子菜单
            Menu applicationList = createMenu(recruitment.getId(), "报名列表", "/recruitment/applications", "ApplicationList", null, 1);
            Menu interviewConfig = createMenu(recruitment.getId(), "面试配置", "/recruitment/interview-config", "InterviewConfig", null, 2);

            // 薪酬管理子菜单
            Menu salaryList = createMenu(salary.getId(), "薪酬列表", "/salary/list", "SalaryList", null, 1);
            Menu salaryBatch = createMenu(salary.getId(), "批量编辑", "/salary/batch", "SalaryBatchEdit", null, 2);

            // 问卷管理子菜单
            Menu questionnaireList = createMenu(questionnaire.getId(), "问卷列表", "/questionnaire/list", "QuestionnaireList", null, 1);
            Menu questionnaireDesign = createMenu(questionnaire.getId(), "问卷设计", "/questionnaire/design", "QuestionnaireDesigner", null, 2);

            // 面试管理子菜单
            Menu aiInterviewList = createMenu(interview.getId(), "面试列表", "/interview/list", "InterviewListPage", null, 1);
            Menu aiInterviewStats = createMenu(interview.getId(), "面试统计", "/interview/stats", "InterviewStatistics", null, 2);

            // 系统设置子菜单
            Menu configCenter = createMenu(settings.getId(), "配置中心", "/settings/config", "ConfigCenter", null, 1);
            Menu menuManage = createMenu(settings.getId(), "菜单管理", "/settings/menus", "MenuManager", null, 2);
            Menu permissionManage = createMenu(settings.getId(), "权限管理", "/settings/permissions", "PermissionManager", null, 3);

            // 日志管理子菜单
            Menu operationLogs = createMenu(logs.getId(), "操作日志", "/logs/operation", "OperationLogs", null, 1);
            Menu roleChangeLogs = createMenu(logs.getId(), "角色变更", "/logs/role-change", "RoleChangeLogs", null, 2);

            // 创建权限
            createPermission(memberList, "查看成员", "member:view");
            createPermission(memberList, "编辑成员", "member:edit");
            createPermission(memberList, "删除成员", "member:delete");
            createPermission(memberFlow, "成员流转", "member:flow");

            createPermission(applicationList, "查看报名", "application:view");
            createPermission(applicationList, "审核报名", "application:review");

            createPermission(salaryList, "查看薪酬", "salary:view");
            createPermission(salaryBatch, "编辑薪酬", "salary:edit");
            createPermission(salaryBatch, "生成薪酬", "salary:generate");

            createPermission(questionnaireList, "查看问卷", "questionnaire:view");
            createPermission(questionnaireDesign, "创建问卷", "questionnaire:create");
            createPermission(questionnaireDesign, "编辑问卷", "questionnaire:edit");
            createPermission(questionnaireList, "删除问卷", "questionnaire:delete");
            createPermission(questionnaireList, "发布问卷", "questionnaire:publish");

            createPermission(aiInterviewList, "查看面试", "interview:view");
            createPermission(aiInterviewStats, "面试统计", "interview:stats");

            createPermission(configCenter, "查看配置", "config:view");
            createPermission(configCenter, "编辑配置", "config:edit");
            createPermission(menuManage, "菜单管理", "menu:manage");
            createPermission(permissionManage, "权限管理", "permission:manage");

            createPermission(operationLogs, "查看日志", "log:view");
            createPermission(roleChangeLogs, "角色日志", "log:role");

            // 为角色分配菜单
            List<Menu> allMenus = menuRepository.findAll();
            List<Menu> leaderMenus = allMenus.stream()
                    .filter(m -> !m.getPath().startsWith("/settings") || m.getPath().equals("/settings/config"))
                    .toList();
            List<Menu> memberMenus = allMenus.stream()
                    .filter(m -> Arrays.asList("/dashboard", "/activities", "/salary/list", "/questionnaire/list").contains(m.getPath())
                            || m.getParentId() == null && Arrays.asList("仪表盘", "活动管理").contains(m.getName()))
                    .toList();

            // ADMIN 拥有所有菜单
            assignMenusToRole(Role.ADMIN.name(), allMenus);
            // LEADER 拥有大部分菜单
            assignMenusToRole(Role.LEADER.name(), leaderMenus);
            // VICE_LEADER 同 LEADER
            assignMenusToRole(Role.VICE_LEADER.name(), leaderMenus);
            // MEMBER 只有基础菜单
            assignMenusToRole(Role.MEMBER.name(), memberMenus);

            // 为角色分配权限
            List<Permission> allPermissions = permissionRepository.findAll();
            assignPermissionsToRole(Role.ADMIN.name(), allPermissions);

            List<Permission> leaderPermissions = allPermissions.stream()
                    .filter(p -> !p.getCode().startsWith("menu:") && !p.getCode().startsWith("permission:"))
                    .toList();
            assignPermissionsToRole(Role.LEADER.name(), leaderPermissions);
            assignPermissionsToRole(Role.VICE_LEADER.name(), leaderPermissions);

            List<Permission> memberPermissions = allPermissions.stream()
                    .filter(p -> p.getCode().endsWith(":view"))
                    .toList();
            assignPermissionsToRole(Role.MEMBER.name(), memberPermissions);

            log.info("菜单和权限数据初始化完成");
        }
    }

    private Menu createMenu(Long parentId, String name, String path, String component, String icon, int sortOrder) {
        Menu menu = Menu.builder()
                .parentId(parentId)
                .name(name)
                .path(path)
                .component(component)
                .icon(icon)
                .sortOrder(sortOrder)
                .visible(true)
                .status(true)
                .build();
        return menuRepository.save(menu);
    }

    private Permission createPermission(Menu menu, String name, String code) {
        Permission permission = Permission.builder()
                .menu(menu)
                .name(name)
                .code(code)
                .build();
        return permissionRepository.save(permission);
    }

    private void assignMenusToRole(String role, List<Menu> menus) {
        for (Menu menu : menus) {
            RoleMenu roleMenu = RoleMenu.builder()
                    .role(role)
                    .menu(menu)
                    .build();
            roleMenuRepository.save(roleMenu);
        }
    }

    private void assignPermissionsToRole(String role, List<Permission> permissions) {
        for (Permission permission : permissions) {
            RolePermission rolePermission = RolePermission.builder()
                    .role(role)
                    .permission(permission)
                    .build();
            rolePermissionRepository.save(rolePermission);
        }
    }
}
