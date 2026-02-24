package com.huafen.system.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huafen.system.entity.InterviewQuestion;
import com.huafen.system.entity.SystemConfig;
import com.huafen.system.entity.User;
import com.huafen.system.entity.enums.Role;
import com.huafen.system.entity.enums.UserStatus;
import com.huafen.system.repository.InterviewQuestionRepository;
import com.huafen.system.repository.SystemConfigRepository;
import com.huafen.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.InputStream;
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
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) {
        initUsers();
        initConfigs();
        initQuestions();
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
}
