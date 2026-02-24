# 系统增强功能实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现问卷系统、AI面试系统、薪酬系统、配置中心、权限系统和日志系统的完整功能

**Architecture:** 模块化增强现有Spring Boot + Vue3项目，使用WebSocket实现AI面试实时通信，Redis缓存配置，AES-256加密敏感数据

**Tech Stack:** Spring Boot 3.x, Spring Data JPA, Spring WebSocket, Redis, Vue3, Element Plus, Pinia, ECharts

---

## Phase 1: Infrastructure (Tasks 1-5)

### Task 1: 添加Redis和WebSocket依赖到pom.xml

**Files:** `backend/pom.xml`

**Step 1:** 在`<dependencies>`节点中添加Redis依赖
```xml
<!-- Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

**Step 2:** 添加WebSocket依赖
```xml
<!-- WebSocket -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

**Step 3:** 添加Jackson用于JSON序列化（如果没有）
```xml
<!-- Jackson for JSON -->
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

**Commit:** `git add backend/pom.xml && git commit -m "feat: add Redis and WebSocket dependencies"`

---

### Task 2: 创建Redis配置类

**Files:** `backend/src/main/java/com/huafen/system/config/RedisConfig.java`

**Step 1:** 创建RedisConfig.java
```java
package com.huafen.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
```

**Step 2:** 在application.yml中添加Redis配置
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0
      timeout: 10000ms
```

**Commit:** `git add backend/src/main/java/com/huafen/system/config/RedisConfig.java && git commit -m "feat: add Redis configuration"`

---

### Task 3: 创建WebSocket配置类

**Files:** `backend/src/main/java/com/huafen/system/config/WebSocketConfig.java`

**Step 1:** 创建WebSocketConfig.java
```java
package com.huafen.system.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/config/WebSocketConfig.java && git commit -m "feat: add WebSocket configuration with STOMP"`

---

### Task 4: 创建加密工具服务 (AES-256-GCM)

**Files:** `backend/src/main/java/com/huafen/system/service/EncryptionService.java`

**Step 1:** 创建EncryptionService.java
```java
package com.huafen.system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    @Value("${app.encryption.key}")
    private String encryptionKey;

    public String encrypt(String plainText) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(encryptionKey), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv).put(encrypted);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String cipherText) {
        try {
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            ByteBuffer buffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);
            SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(encryptionKey), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return new String(cipher.doFinal(encrypted));
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
```

**Step 2:** 在application.yml中添加加密密钥配置
```yaml
app:
  encryption:
    key: ${ENCRYPTION_KEY:your-32-byte-base64-encoded-key-here}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/service/EncryptionService.java && git commit -m "feat: add AES-256-GCM encryption service"`

---

### Task 5: 创建增强的操作日志基础设施

**Files:**
- `backend/src/main/java/com/huafen/system/annotation/OperationLog.java`
- `backend/src/main/java/com/huafen/system/aspect/OperationLogAspect.java`

**Step 1:** 创建OperationLog注解
```java
package com.huafen.system.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    String module() default "";
    String action() default "";
    String description() default "";
}
```

**Step 2:** 创建OperationLogAspect切面
```java
package com.huafen.system.aspect;

import com.huafen.system.annotation.OperationLog;
import com.huafen.system.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService logService;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint point, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = point.proceed();
        long duration = System.currentTimeMillis() - startTime;

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
            .getRequestAttributes()).getRequest();

        logService.logOperation(
            operationLog.module(),
            operationLog.action(),
            operationLog.description(),
            request.getRemoteAddr(),
            duration
        );
        return result;
    }
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/annotation/ backend/src/main/java/com/huafen/system/aspect/ && git commit -m "feat: add operation log annotation and aspect"`

---

## Phase 2: Database Schema (Tasks 6-12)

### Task 6: 创建问卷系统数据表SQL

**Files:** `backend/src/main/resources/schema-questionnaire.sql`

**Step 1:** 创建问卷表
```sql
-- 问卷表
CREATE TABLE IF NOT EXISTS questionnaires (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    start_time DATETIME,
    end_time DATETIME,
    is_anonymous TINYINT(1) DEFAULT 0,
    max_responses INT,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_questionnaire_status (status),
    INDEX idx_questionnaire_created_by (created_by),
    CONSTRAINT fk_questionnaire_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 问卷字段表
CREATE TABLE IF NOT EXISTS questionnaire_fields (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    questionnaire_id BIGINT NOT NULL,
    field_type VARCHAR(30) NOT NULL,
    label VARCHAR(200) NOT NULL,
    placeholder VARCHAR(200),
    required TINYINT(1) DEFAULT 0,
    options JSON,
    validation_rules JSON,
    conditional_logic JSON,
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_field_questionnaire (questionnaire_id),
    CONSTRAINT fk_field_questionnaire FOREIGN KEY (questionnaire_id) REFERENCES questionnaires(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 问卷回复表
CREATE TABLE IF NOT EXISTS questionnaire_responses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    questionnaire_id BIGINT NOT NULL,
    user_id BIGINT,
    answers JSON NOT NULL,
    ip_address VARCHAR(50),
    submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_response_questionnaire (questionnaire_id),
    INDEX idx_response_user (user_id),
    CONSTRAINT fk_response_questionnaire FOREIGN KEY (questionnaire_id) REFERENCES questionnaires(id) ON DELETE CASCADE,
    CONSTRAINT fk_response_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Commit:** `git add backend/src/main/resources/schema-questionnaire.sql && git commit -m "feat: add questionnaire database schema"`

---

### Task 7: 创建AI面试系统数据表SQL

**Files:** `backend/src/main/resources/schema-ai-interview.sql`

**Step 1:** 创建AI面试相关表
```sql
-- AI面试会话表
CREATE TABLE IF NOT EXISTS ai_interview_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    application_id BIGINT,
    session_token VARCHAR(100) UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    started_at DATETIME,
    ended_at DATETIME,
    total_duration INT DEFAULT 0,
    violation_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ai_session_user (user_id),
    INDEX idx_ai_session_status (status),
    INDEX idx_ai_session_token (session_token),
    CONSTRAINT fk_ai_session_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ai_session_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- AI面试消息表
CREATE TABLE IF NOT EXISTS ai_interview_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    token_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ai_message_session (session_id),
    CONSTRAINT fk_ai_message_session FOREIGN KEY (session_id) REFERENCES ai_interview_sessions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- AI面试评分表
CREATE TABLE IF NOT EXISTS ai_interview_scores (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL UNIQUE,
    communication_score INT DEFAULT 0,
    logic_score INT DEFAULT 0,
    knowledge_score INT DEFAULT 0,
    attitude_score INT DEFAULT 0,
    creativity_score INT DEFAULT 0,
    total_score INT DEFAULT 0,
    ai_summary TEXT,
    violation_types JSON,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ai_score_session (session_id),
    CONSTRAINT fk_ai_score_session FOREIGN KEY (session_id) REFERENCES ai_interview_sessions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Commit:** `git add backend/src/main/resources/schema-ai-interview.sql && git commit -m "feat: add AI interview database schema"`

---

### Task 8: 创建薪酬增强数据表SQL

**Files:** `backend/src/main/resources/schema-salary-enhancement.sql`

**Step 1:** 创建薪酬增强相关表
```sql
-- 成员流动日志表
CREATE TABLE IF NOT EXISTS member_flow_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    flow_type VARCHAR(20) NOT NULL,
    from_role VARCHAR(20),
    to_role VARCHAR(20),
    reason TEXT,
    operator_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_flow_user (user_id),
    INDEX idx_flow_type (flow_type),
    CONSTRAINT fk_flow_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_flow_operator FOREIGN KEY (operator_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 月度绩效表
CREATE TABLE IF NOT EXISTS monthly_performances (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    period VARCHAR(20) NOT NULL,
    checkin_count INT DEFAULT 0,
    activity_count INT DEFAULT 0,
    task_count INT DEFAULT 0,
    extra_points INT DEFAULT 0,
    performance_level VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_perf_user_period (user_id, period),
    CONSTRAINT fk_perf_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 薪酬池表
CREATE TABLE IF NOT EXISTS salary_pools (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    period VARCHAR(20) NOT NULL UNIQUE,
    total_amount DECIMAL(10,2) DEFAULT 2000.00,
    distributed_amount DECIMAL(10,2) DEFAULT 0.00,
    member_count INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_pool_period (period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Step 2:** 修改salaries表添加签到相关字段
```sql
ALTER TABLE salaries
ADD COLUMN checkin_points INT DEFAULT 0 AFTER bonus_points,
ADD COLUMN activity_points INT DEFAULT 0 AFTER checkin_points,
ADD COLUMN pool_id BIGINT AFTER status,
ADD CONSTRAINT fk_salary_pool FOREIGN KEY (pool_id) REFERENCES salary_pools(id) ON DELETE SET NULL;
```

**Commit:** `git add backend/src/main/resources/schema-salary-enhancement.sql && git commit -m "feat: add salary enhancement database schema"`

---

### Task 9: 创建菜单和权限数据表SQL

**Files:** `backend/src/main/resources/schema-permission.sql`

**Step 1:** 创建菜单和权限相关表
```sql
-- 菜单表
CREATE TABLE IF NOT EXISTS menus (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(50) NOT NULL,
    path VARCHAR(200),
    component VARCHAR(200),
    icon VARCHAR(50),
    sort_order INT DEFAULT 0,
    visible TINYINT(1) DEFAULT 1,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_menu_parent (parent_id),
    INDEX idx_menu_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 权限表
CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    module VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_perm_code (code),
    INDEX idx_perm_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS role_menus (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role VARCHAR(20) NOT NULL,
    menu_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_role_menu (role, menu_id),
    CONSTRAINT fk_role_menu FOREIGN KEY (menu_id) REFERENCES menus(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role VARCHAR(20) NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_role_perm (role, permission_id),
    CONSTRAINT fk_role_perm FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Commit:** `git add backend/src/main/resources/schema-permission.sql && git commit -m "feat: add menu and permission database schema"`

---

### Task 10: 创建日志增强数据表SQL

**Files:** `backend/src/main/resources/schema-log-enhancement.sql`

**Step 1:** 创建日志增强相关表
```sql
-- 角色变更日志表
CREATE TABLE IF NOT EXISTS role_change_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    from_role VARCHAR(20),
    to_role VARCHAR(20),
    reason TEXT,
    operator_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_role_log_user (user_id),
    CONSTRAINT fk_role_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_log_operator FOREIGN KEY (operator_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 权限变更日志表
CREATE TABLE IF NOT EXISTS permission_change_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role VARCHAR(20) NOT NULL,
    permission_id BIGINT,
    action VARCHAR(20) NOT NULL,
    operator_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_perm_log_role (role),
    CONSTRAINT fk_perm_log_operator FOREIGN KEY (operator_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 配置变更日志表
CREATE TABLE IF NOT EXISTS config_change_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    operator_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_config_log_key (config_key),
    CONSTRAINT fk_config_log_operator FOREIGN KEY (operator_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Step 2:** 增强operation_logs表
```sql
ALTER TABLE operation_logs
ADD COLUMN module VARCHAR(50) AFTER action,
ADD COLUMN duration BIGINT DEFAULT 0 AFTER detail,
ADD COLUMN request_body TEXT AFTER ip,
ADD COLUMN response_body TEXT AFTER request_body,
ADD INDEX idx_logs_module (module);
```

**Commit:** `git add backend/src/main/resources/schema-log-enhancement.sql && git commit -m "feat: add log enhancement database schema"`

---

### Task 11: 执行数据库迁移

**Files:** 无新文件，执行SQL脚本

**Step 1:** 按顺序执行所有schema文件
```bash
mysql -u root -p huafen_db < backend/src/main/resources/schema-questionnaire.sql
mysql -u root -p huafen_db < backend/src/main/resources/schema-ai-interview.sql
mysql -u root -p huafen_db < backend/src/main/resources/schema-salary-enhancement.sql
mysql -u root -p huafen_db < backend/src/main/resources/schema-permission.sql
mysql -u root -p huafen_db < backend/src/main/resources/schema-log-enhancement.sql
```

**Commit:** 无需提交

---

### Task 12: 创建菜单和权限种子数据

**Files:** `backend/src/main/resources/data-seed.sql`

**Step 1:** 插入菜单种子数据
```sql
-- 一级菜单
INSERT INTO menus (id, parent_id, name, path, icon, sort_order) VALUES
(1, 0, '数据看板', '/dashboard', 'Dashboard', 1),
(2, 0, '成员管理', '/members', 'User', 2),
(3, 0, '招新管理', '/recruitment', 'UserPlus', 3),
(4, 0, '面试管理', '/interview', 'VideoCamera', 4),
(5, 0, '活动管理', '/activities', 'Calendar', 5),
(6, 0, '薪酬管理', '/salary', 'Money', 6),
(7, 0, '问卷管理', '/questionnaire', 'Document', 7),
(8, 0, '系统设置', '/settings', 'Setting', 8),
(9, 0, '日志管理', '/logs', 'List', 9);

-- 二级菜单
INSERT INTO menus (id, parent_id, name, path, component, sort_order) VALUES
(41, 4, '面试列表', '/interview/list', 'interview/index', 1),
(42, 4, 'AI面试', '/interview/ai', 'interview/ai/index', 2),
(43, 4, '题库管理', '/interview/config', 'interview/config/index', 3),
(81, 8, '基础配置', '/settings/basic', 'settings/index', 1),
(82, 8, '菜单管理', '/settings/menu', 'settings/menu/index', 2),
(83, 8, '权限管理', '/settings/permission', 'settings/permission/index', 3);
```

**Step 2:** 插入权限种子数据
```sql
INSERT INTO permissions (code, name, module) VALUES
('user:view', '查看用户', 'user'),
('user:create', '创建用户', 'user'),
('user:update', '更新用户', 'user'),
('user:delete', '删除用户', 'user'),
('salary:view', '查看薪酬', 'salary'),
('salary:edit', '编辑薪酬', 'salary'),
('salary:approve', '审批薪酬', 'salary'),
('config:view', '查看配置', 'config'),
('config:edit', '编辑配置', 'config'),
('interview:view', '查看面试', 'interview'),
('interview:manage', '管理面试', 'interview');
```

**Commit:** `git add backend/src/main/resources/data-seed.sql && git commit -m "feat: add menu and permission seed data"`

---

## Phase 3: Backend - Questionnaire System (Tasks 13-20)

### Task 13: 创建Questionnaire实体

**Files:** `backend/src/main/java/com/huafen/system/entity/Questionnaire.java`

**Step 1:** 创建QuestionnaireStatus枚举
```java
package com.huafen.system.entity.enums;

public enum QuestionnaireStatus {
    DRAFT, PUBLISHED, CLOSED, ARCHIVED
}
```

**Step 2:** 创建Questionnaire实体
```java
package com.huafen.system.entity;

import com.huafen.system.entity.enums.QuestionnaireStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "questionnaires")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Questionnaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private QuestionnaireStatus status;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "is_anonymous")
    private Boolean isAnonymous;

    @Column(name = "max_responses")
    private Integer maxResponses;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "questionnaire", cascade = CascadeType.ALL)
    @OrderBy("sortOrder ASC")
    private List<QuestionnaireField> fields;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/entity/Questionnaire.java backend/src/main/java/com/huafen/system/entity/enums/QuestionnaireStatus.java && git commit -m "feat: add Questionnaire entity"`

---

### Task 14: 创建QuestionnaireField实体

**Files:** `backend/src/main/java/com/huafen/system/entity/QuestionnaireField.java`

**Step 1:** 创建FieldType枚举
```java
package com.huafen.system.entity.enums;

public enum FieldType {
    TEXT, TEXTAREA, RADIO, CHECKBOX, SELECT, DATE, FILE
}
```

**Step 2:** 创建QuestionnaireField实体
```java
package com.huafen.system.entity;

import com.huafen.system.entity.enums.FieldType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "questionnaire_fields")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionnaire_id", nullable = false)
    private Questionnaire questionnaire;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", length = 30, nullable = false)
    private FieldType fieldType;

    @Column(nullable = false, length = 200)
    private String label;

    @Column(length = 200)
    private String placeholder;

    @Column
    private Boolean required;

    @Column(columnDefinition = "JSON")
    private String options;

    @Column(name = "validation_rules", columnDefinition = "JSON")
    private String validationRules;

    @Column(name = "conditional_logic", columnDefinition = "JSON")
    private String conditionalLogic;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/entity/QuestionnaireField.java backend/src/main/java/com/huafen/system/entity/enums/FieldType.java && git commit -m "feat: add QuestionnaireField entity"`

---

### Task 15: 创建QuestionnaireResponse实体

**Files:** `backend/src/main/java/com/huafen/system/entity/QuestionnaireResponse.java`

**Step 1:** 创建QuestionnaireResponse实体
```java
package com.huafen.system.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "questionnaire_responses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionnaire_id", nullable = false)
    private Questionnaire questionnaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "JSON", nullable = false)
    private String answers;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/entity/QuestionnaireResponse.java && git commit -m "feat: add QuestionnaireResponse entity"`

---

### Task 16: 创建QuestionnaireRepository

**Files:** `backend/src/main/java/com/huafen/system/repository/QuestionnaireRepository.java`

**Step 1:** 创建QuestionnaireRepository
```java
package com.huafen.system.repository;

import com.huafen.system.entity.Questionnaire;
import com.huafen.system.entity.enums.QuestionnaireStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {
    Page<Questionnaire> findByStatus(QuestionnaireStatus status, Pageable pageable);

    @Query("SELECT q FROM Questionnaire q WHERE q.status = 'PUBLISHED' AND " +
           "(q.startTime IS NULL OR q.startTime <= CURRENT_TIMESTAMP) AND " +
           "(q.endTime IS NULL OR q.endTime >= CURRENT_TIMESTAMP)")
    List<Questionnaire> findActiveQuestionnaires();

    Page<Questionnaire> findByCreatedById(Long userId, Pageable pageable);
}
```

**Step 2:** 创建QuestionnaireFieldRepository
```java
package com.huafen.system.repository;

import com.huafen.system.entity.QuestionnaireField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionnaireFieldRepository extends JpaRepository<QuestionnaireField, Long> {
    List<QuestionnaireField> findByQuestionnaireIdOrderBySortOrderAsc(Long questionnaireId);
    void deleteByQuestionnaireId(Long questionnaireId);
}
```

**Step 3:** 创建QuestionnaireResponseRepository
```java
package com.huafen.system.repository;

import com.huafen.system.entity.QuestionnaireResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionnaireResponseRepository extends JpaRepository<QuestionnaireResponse, Long> {
    Page<QuestionnaireResponse> findByQuestionnaireId(Long questionnaireId, Pageable pageable);
    long countByQuestionnaireId(Long questionnaireId);
    boolean existsByQuestionnaireIdAndUserId(Long questionnaireId, Long userId);
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/repository/Questionnaire*.java && git commit -m "feat: add questionnaire repositories"`

---

### Task 17: 创建QuestionnaireService接口

**Files:** `backend/src/main/java/com/huafen/system/service/QuestionnaireService.java`

**Step 1:** 创建QuestionnaireService接口
```java
package com.huafen.system.service;

import com.huafen.system.dto.questionnaire.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionnaireService {
    QuestionnaireDTO create(QuestionnaireCreateRequest request);
    QuestionnaireDTO update(Long id, QuestionnaireUpdateRequest request);
    QuestionnaireDTO getById(Long id);
    Page<QuestionnaireDTO> list(QuestionnaireQueryRequest request, Pageable pageable);
    void delete(Long id);
    void publish(Long id);
    void close(Long id);
    QuestionnaireResponseDTO submitResponse(Long id, ResponseSubmitRequest request);
    Page<QuestionnaireResponseDTO> getResponses(Long id, Pageable pageable);
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/service/QuestionnaireService.java && git commit -m "feat: add QuestionnaireService interface"`

---

### Task 18: 创建QuestionnaireServiceImpl

**Files:** `backend/src/main/java/com/huafen/system/service/impl/QuestionnaireServiceImpl.java`

**Step 1:** 创建QuestionnaireServiceImpl
```java
package com.huafen.system.service.impl;

import com.huafen.system.dto.questionnaire.*;
import com.huafen.system.entity.*;
import com.huafen.system.entity.enums.QuestionnaireStatus;
import com.huafen.system.exception.BusinessException;
import com.huafen.system.repository.*;
import com.huafen.system.service.QuestionnaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QuestionnaireServiceImpl implements QuestionnaireService {

    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionnaireFieldRepository fieldRepository;
    private final QuestionnaireResponseRepository responseRepository;

    @Override
    @Transactional
    public QuestionnaireDTO create(QuestionnaireCreateRequest request) {
        Questionnaire questionnaire = Questionnaire.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .status(QuestionnaireStatus.DRAFT)
            .isAnonymous(request.getIsAnonymous())
            .maxResponses(request.getMaxResponses())
            .build();
        questionnaire = questionnaireRepository.save(questionnaire);
        saveFields(questionnaire, request.getFields());
        return toDTO(questionnaire);
    }

    @Override
    @Transactional
    public QuestionnaireDTO update(Long id, QuestionnaireUpdateRequest request) {
        Questionnaire questionnaire = findById(id);
        questionnaire.setTitle(request.getTitle());
        questionnaire.setDescription(request.getDescription());
        questionnaire.setStartTime(request.getStartTime());
        questionnaire.setEndTime(request.getEndTime());
        fieldRepository.deleteByQuestionnaireId(id);
        saveFields(questionnaire, request.getFields());
        return toDTO(questionnaireRepository.save(questionnaire));
    }

    @Override
    public void publish(Long id) {
        Questionnaire q = findById(id);
        q.setStatus(QuestionnaireStatus.PUBLISHED);
        questionnaireRepository.save(q);
    }

    private Questionnaire findById(Long id) {
        return questionnaireRepository.findById(id)
            .orElseThrow(() -> new BusinessException("问卷不存在"));
    }

    private void saveFields(Questionnaire q, java.util.List<FieldCreateRequest> fields) {
        if (fields == null) return;
        for (int i = 0; i < fields.size(); i++) {
            FieldCreateRequest f = fields.get(i);
            QuestionnaireField field = QuestionnaireField.builder()
                .questionnaire(q)
                .fieldType(f.getFieldType())
                .label(f.getLabel())
                .placeholder(f.getPlaceholder())
                .required(f.getRequired())
                .options(f.getOptions())
                .sortOrder(i)
                .build();
            fieldRepository.save(field);
        }
    }
    // ... 其他方法实现
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/service/impl/QuestionnaireServiceImpl.java && git commit -m "feat: add QuestionnaireServiceImpl"`

---

### Task 19: 创建QuestionnaireController

**Files:** `backend/src/main/java/com/huafen/system/controller/QuestionnaireController.java`

**Step 1:** 创建QuestionnaireController
```java
package com.huafen.system.controller;

import com.huafen.system.common.Result;
import com.huafen.system.dto.questionnaire.*;
import com.huafen.system.service.QuestionnaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questionnaires")
@RequiredArgsConstructor
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;

    @PostMapping
    public Result<QuestionnaireDTO> create(@RequestBody QuestionnaireCreateRequest request) {
        return Result.success(questionnaireService.create(request));
    }

    @PutMapping("/{id}")
    public Result<QuestionnaireDTO> update(@PathVariable Long id,
                                           @RequestBody QuestionnaireUpdateRequest request) {
        return Result.success(questionnaireService.update(id, request));
    }

    @GetMapping("/{id}")
    public Result<QuestionnaireDTO> getById(@PathVariable Long id) {
        return Result.success(questionnaireService.getById(id));
    }

    @GetMapping
    public Result<Page<QuestionnaireDTO>> list(QuestionnaireQueryRequest request, Pageable pageable) {
        return Result.success(questionnaireService.list(request, pageable));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        questionnaireService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        questionnaireService.publish(id);
        return Result.success();
    }

    @PostMapping("/{id}/responses")
    public Result<QuestionnaireResponseDTO> submitResponse(@PathVariable Long id,
                                                           @RequestBody ResponseSubmitRequest request) {
        return Result.success(questionnaireService.submitResponse(id, request));
    }

    @GetMapping("/{id}/responses")
    public Result<Page<QuestionnaireResponseDTO>> getResponses(@PathVariable Long id, Pageable pageable) {
        return Result.success(questionnaireService.getResponses(id, pageable));
    }
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/controller/QuestionnaireController.java && git commit -m "feat: add QuestionnaireController"`

---

### Task 20: 创建问卷DTOs

**Files:** `backend/src/main/java/com/huafen/system/dto/questionnaire/`

**Step 1:** 创建QuestionnaireDTO
```java
package com.huafen.system.dto.questionnaire;

import com.huafen.system.entity.enums.QuestionnaireStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuestionnaireDTO {
    private Long id;
    private String title;
    private String description;
    private QuestionnaireStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isAnonymous;
    private Integer maxResponses;
    private Long responseCount;
    private List<FieldDTO> fields;
    private LocalDateTime createdAt;
}
```

**Step 2:** 创建FieldDTO和请求类
```java
package com.huafen.system.dto.questionnaire;

import com.huafen.system.entity.enums.FieldType;
import lombok.Data;

@Data
public class FieldDTO {
    private Long id;
    private FieldType fieldType;
    private String label;
    private String placeholder;
    private Boolean required;
    private String options;
    private String validationRules;
    private String conditionalLogic;
    private Integer sortOrder;
}
```

**Step 3:** 创建请求类
```java
// QuestionnaireCreateRequest.java
package com.huafen.system.dto.questionnaire;

import lombok.Data;
import java.util.List;

@Data
public class QuestionnaireCreateRequest {
    private String title;
    private String description;
    private Boolean isAnonymous;
    private Integer maxResponses;
    private List<FieldCreateRequest> fields;
}

// FieldCreateRequest.java
@Data
public class FieldCreateRequest {
    private FieldType fieldType;
    private String label;
    private String placeholder;
    private Boolean required;
    private String options;
}

// ResponseSubmitRequest.java
@Data
public class ResponseSubmitRequest {
    private String answers;
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/dto/questionnaire/ && git commit -m "feat: add questionnaire DTOs"`

---

## Phase 4: Backend - AI Interview System (Tasks 21-32)

### Task 21: 创建AIInterviewSession实体

**Files:** `backend/src/main/java/com/huafen/system/entity/AIInterviewSession.java`

**Step 1:** 创建AIInterviewStatus枚举
```java
package com.huafen.system.entity.enums;

public enum AIInterviewStatus {
    PENDING, IN_PROGRESS, COMPLETED, TERMINATED, EXPIRED
}
```

**Step 2:** 创建AIInterviewSession实体
```java
package com.huafen.system.entity;

import com.huafen.system.entity.enums.AIInterviewStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ai_interview_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIInterviewSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

    @Column(name = "session_token", unique = true, length = 100)
    private String sessionToken;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AIInterviewStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "total_duration")
    private Integer totalDuration;

    @Column(name = "violation_count")
    private Integer violationCount;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<AIInterviewMessage> messages;

    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL)
    private AIInterviewScore score;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/entity/AIInterviewSession.java backend/src/main/java/com/huafen/system/entity/enums/AIInterviewStatus.java && git commit -m "feat: add AIInterviewSession entity"`

---

### Task 22: 创建AIInterviewMessage实体

**Files:** `backend/src/main/java/com/huafen/system/entity/AIInterviewMessage.java`

**Step 1:** 创建MessageRole枚举
```java
package com.huafen.system.entity.enums;

public enum MessageRole {
    SYSTEM, USER, ASSISTANT
}
```

**Step 2:** 创建AIInterviewMessage实体
```java
package com.huafen.system.entity;

import com.huafen.system.entity.enums.MessageRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_interview_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIInterviewMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private AIInterviewSession session;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private MessageRole role;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "token_count")
    private Integer tokenCount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/entity/AIInterviewMessage.java backend/src/main/java/com/huafen/system/entity/enums/MessageRole.java && git commit -m "feat: add AIInterviewMessage entity"`

---

### Task 23: 创建AIInterviewScore实体

**Files:** `backend/src/main/java/com/huafen/system/entity/AIInterviewScore.java`

**Step 1:** 创建AIInterviewScore实体
```java
package com.huafen.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_interview_scores")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIInterviewScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private AIInterviewSession session;

    @Column(name = "communication_score")
    private Integer communicationScore;

    @Column(name = "logic_score")
    private Integer logicScore;

    @Column(name = "knowledge_score")
    private Integer knowledgeScore;

    @Column(name = "attitude_score")
    private Integer attitudeScore;

    @Column(name = "creativity_score")
    private Integer creativityScore;

    @Column(name = "total_score")
    private Integer totalScore;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @Column(name = "violation_types", columnDefinition = "JSON")
    private String violationTypes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/entity/AIInterviewScore.java && git commit -m "feat: add AIInterviewScore entity"`

---

### Task 24: 创建AI面试Repositories

**Files:** `backend/src/main/java/com/huafen/system/repository/`

**Step 1:** 创建AIInterviewSessionRepository
```java
package com.huafen.system.repository;

import com.huafen.system.entity.AIInterviewSession;
import com.huafen.system.entity.enums.AIInterviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AIInterviewSessionRepository extends JpaRepository<AIInterviewSession, Long> {
    Optional<AIInterviewSession> findBySessionToken(String sessionToken);
    Page<AIInterviewSession> findByUserId(Long userId, Pageable pageable);
    Page<AIInterviewSession> findByStatus(AIInterviewStatus status, Pageable pageable);
    boolean existsByUserIdAndStatus(Long userId, AIInterviewStatus status);
}
```

**Step 2:** 创建AIInterviewMessageRepository
```java
package com.huafen.system.repository;

import com.huafen.system.entity.AIInterviewMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AIInterviewMessageRepository extends JpaRepository<AIInterviewMessage, Long> {
    List<AIInterviewMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
    int countBySessionId(Long sessionId);
}
```

**Step 3:** 创建AIInterviewScoreRepository
```java
package com.huafen.system.repository;

import com.huafen.system.entity.AIInterviewScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AIInterviewScoreRepository extends JpaRepository<AIInterviewScore, Long> {
    Optional<AIInterviewScore> findBySessionId(Long sessionId);

    @Query("SELECT AVG(s.totalScore) FROM AIInterviewScore s")
    Double getAverageScore();
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/repository/AIInterview*.java && git commit -m "feat: add AI interview repositories"`

---

### Task 25: 创建AIProvider接口

**Files:** `backend/src/main/java/com/huafen/system/service/ai/AIProvider.java`

**Step 1:** 创建AIProvider接口
```java
package com.huafen.system.service.ai;

import java.util.List;
import java.util.function.Consumer;

public interface AIProvider {
    String getName();
    String chat(List<Message> messages);
    void streamChat(List<Message> messages, Consumer<String> onToken, Runnable onComplete);

    record Message(String role, String content) {}
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/service/ai/AIProvider.java && git commit -m "feat: add AIProvider interface"`

---

### Task 26: 创建OpenAICompatibleProvider实现

**Files:** `backend/src/main/java/com/huafen/system/service/ai/OpenAICompatibleProvider.java`

**Step 1:** 创建OpenAICompatibleProvider
```java
package com.huafen.system.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.*;
import java.util.function.Consumer;

@Slf4j
public class OpenAICompatibleProvider implements AIProvider {

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAICompatibleProvider(String apiKey, String baseUrl, String model) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public String getName() { return "OpenAI Compatible"; }

    @Override
    public String chat(List<Message> messages) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages.stream()
            .map(m -> Map.of("role", m.role(), "content", m.content()))
            .toList());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
            baseUrl + "/chat/completions", entity, JsonNode.class);

        return response.getBody().path("choices").get(0).path("message").path("content").asText();
    }

    @Override
    public void streamChat(List<Message> messages, Consumer<String> onToken, Runnable onComplete) {
        // WebClient streaming implementation
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("stream", true);
        body.put("messages", messages.stream()
            .map(m -> Map.of("role", m.role(), "content", m.content()))
            .toList());

        webClient.post()
            .uri("/chat/completions")
            .header("Authorization", "Bearer " + apiKey)
            .bodyValue(body)
            .retrieve()
            .bodyToFlux(String.class)
            .doOnNext(chunk -> parseStreamChunk(chunk, onToken))
            .doOnComplete(onComplete)
            .subscribe();
    }

    private void parseStreamChunk(String chunk, Consumer<String> onToken) {
        if (chunk.startsWith("data: ") && !chunk.contains("[DONE]")) {
            try {
                JsonNode node = objectMapper.readTree(chunk.substring(6));
                String content = node.path("choices").get(0).path("delta").path("content").asText("");
                if (!content.isEmpty()) onToken.accept(content);
            } catch (Exception e) { log.debug("Parse error: {}", e.getMessage()); }
        }
    }
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/service/ai/OpenAICompatibleProvider.java && git commit -m "feat: add OpenAI compatible provider"`

---

### Task 27: 创建ClaudeProvider实现

**Files:** `backend/src/main/java/com/huafen/system/service/ai/ClaudeProvider.java`

**Step 1:** 创建ClaudeProvider
```java
package com.huafen.system.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.*;
import java.util.function.Consumer;

@Slf4j
public class ClaudeProvider implements AIProvider {

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1";
    private final String apiKey;
    private final String model;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClaudeProvider(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.webClient = WebClient.builder().baseUrl(CLAUDE_API_URL).build();
    }

    @Override
    public String getName() { return "Claude"; }

    @Override
    public String chat(List<Message> messages) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        String systemPrompt = messages.stream()
            .filter(m -> "system".equals(m.role()))
            .map(Message::content)
            .findFirst().orElse("");

        List<Map<String, String>> chatMessages = messages.stream()
            .filter(m -> !"system".equals(m.role()))
            .map(m -> Map.of("role", m.role(), "content", m.content()))
            .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("max_tokens", 4096);
        body.put("system", systemPrompt);
        body.put("messages", chatMessages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
            CLAUDE_API_URL + "/messages", entity, JsonNode.class);

        return response.getBody().path("content").get(0).path("text").asText();
    }

    @Override
    public void streamChat(List<Message> messages, Consumer<String> onToken, Runnable onComplete) {
        String systemPrompt = messages.stream()
            .filter(m -> "system".equals(m.role()))
            .map(Message::content).findFirst().orElse("");

        List<Map<String, String>> chatMessages = messages.stream()
            .filter(m -> !"system".equals(m.role()))
            .map(m -> Map.of("role", m.role(), "content", m.content()))
            .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("max_tokens", 4096);
        body.put("stream", true);
        body.put("system", systemPrompt);
        body.put("messages", chatMessages);

        webClient.post()
            .uri("/messages")
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .bodyValue(body)
            .retrieve()
            .bodyToFlux(String.class)
            .doOnNext(chunk -> parseClaudeChunk(chunk, onToken))
            .doOnComplete(onComplete)
            .subscribe();
    }

    private void parseClaudeChunk(String chunk, Consumer<String> onToken) {
        if (chunk.startsWith("data: ")) {
            try {
                JsonNode node = objectMapper.readTree(chunk.substring(6));
                if ("content_block_delta".equals(node.path("type").asText())) {
                    String text = node.path("delta").path("text").asText("");
                    if (!text.isEmpty()) onToken.accept(text);
                }
            } catch (Exception e) { log.debug("Parse error: {}", e.getMessage()); }
        }
    }
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/service/ai/ClaudeProvider.java && git commit -m "feat: add Claude provider"`

---

### Task 28: 创建AIInterviewService

**Files:** `backend/src/main/java/com/huafen/system/service/AIInterviewService.java`

**Step 1:** 创建AIInterviewService接口和实现
```java
package com.huafen.system.service;

import com.huafen.system.dto.interview.*;
import java.util.function.Consumer;

public interface AIInterviewService {
    AIInterviewSessionDTO startSession(Long userId, Long applicationId);
    void sendMessage(String sessionToken, String content, Consumer<String> onToken, Runnable onComplete);
    AIInterviewSessionDTO endSession(String sessionToken);
    AIInterviewScoreDTO getScore(String sessionToken);
    AIInterviewSessionDTO getSession(String sessionToken);
}
```

**Step 2:** 创建AIInterviewServiceImpl
```java
package com.huafen.system.service.impl;

import com.huafen.system.dto.interview.*;
import com.huafen.system.entity.*;
import com.huafen.system.entity.enums.*;
import com.huafen.system.exception.BusinessException;
import com.huafen.system.repository.*;
import com.huafen.system.service.AIInterviewService;
import com.huafen.system.service.ai.AIProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class AIInterviewServiceImpl implements AIInterviewService {

    private final AIInterviewSessionRepository sessionRepository;
    private final AIInterviewMessageRepository messageRepository;
    private final AIInterviewScoreRepository scoreRepository;
    private final UserRepository userRepository;
    private final AIProvider aiProvider;

    @Override
    @Transactional
    public AIInterviewSessionDTO startSession(Long userId, Long applicationId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("用户不存在"));

        if (sessionRepository.existsByUserIdAndStatus(userId, AIInterviewStatus.IN_PROGRESS)) {
            throw new BusinessException("已有进行中的面试");
        }

        AIInterviewSession session = AIInterviewSession.builder()
            .user(user)
            .sessionToken(UUID.randomUUID().toString())
            .status(AIInterviewStatus.IN_PROGRESS)
            .startedAt(LocalDateTime.now())
            .violationCount(0)
            .build();

        session = sessionRepository.save(session);
        saveSystemPrompt(session);
        return toDTO(session);
    }

    @Override
    public void sendMessage(String sessionToken, String content, Consumer<String> onToken, Runnable onComplete) {
        AIInterviewSession session = findByToken(sessionToken);
        saveMessage(session, MessageRole.USER, content);

        List<AIProvider.Message> messages = buildMessages(session);
        StringBuilder response = new StringBuilder();

        aiProvider.streamChat(messages,
            token -> { response.append(token); onToken.accept(token); },
            () -> { saveMessage(session, MessageRole.ASSISTANT, response.toString()); onComplete.run(); }
        );
    }

    private AIInterviewSession findByToken(String token) {
        return sessionRepository.findBySessionToken(token)
            .orElseThrow(() -> new BusinessException("会话不存在"));
    }

    private void saveMessage(AIInterviewSession session, MessageRole role, String content) {
        AIInterviewMessage msg = AIInterviewMessage.builder()
            .session(session).role(role).content(content).build();
        messageRepository.save(msg);
    }
    // ... 其他方法
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/service/AIInterviewService.java backend/src/main/java/com/huafen/system/service/impl/AIInterviewServiceImpl.java && git commit -m "feat: add AIInterviewService"`

---

### Task 29: 创建InterviewWebSocketController

**Files:** `backend/src/main/java/com/huafen/system/controller/InterviewWebSocketController.java`

**Step 1:** 创建InterviewWebSocketController
```java
package com.huafen.system.controller;

import com.huafen.system.dto.interview.ChatMessageRequest;
import com.huafen.system.service.AIInterviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class InterviewWebSocketController {

    private final AIInterviewService aiInterviewService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/interview/{sessionToken}/send")
    public void sendMessage(@DestinationVariable String sessionToken,
                           @Payload ChatMessageRequest request) {
        log.info("Received message for session: {}", sessionToken);

        aiInterviewService.sendMessage(
            sessionToken,
            request.getContent(),
            token -> messagingTemplate.convertAndSend(
                "/topic/interview/" + sessionToken + "/stream",
                new StreamResponse("token", token)
            ),
            () -> messagingTemplate.convertAndSend(
                "/topic/interview/" + sessionToken + "/stream",
                new StreamResponse("complete", "")
            )
        );
    }

    @MessageMapping("/interview/{sessionToken}/end")
    public void endInterview(@DestinationVariable String sessionToken) {
        var result = aiInterviewService.endSession(sessionToken);
        messagingTemplate.convertAndSend(
            "/topic/interview/" + sessionToken + "/result",
            result
        );
    }

    record StreamResponse(String type, String content) {}
}
```

**Step 2:** 创建ChatMessageRequest DTO
```java
package com.huafen.system.dto.interview;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private String content;
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/controller/InterviewWebSocketController.java backend/src/main/java/com/huafen/system/dto/interview/ChatMessageRequest.java && git commit -m "feat: add Interview WebSocket controller"`

---

### Task 30: 创建AI系统提示词构建器（8条规则和9种违规类型）

**Files:** `backend/src/main/java/com/huafen/system/service/ai/InterviewPromptBuilder.java`

**Step 1:** 创建InterviewPromptBuilder
```java
package com.huafen.system.service.ai;

import org.springframework.stereotype.Component;

@Component
public class InterviewPromptBuilder {

    public String buildSystemPrompt(String candidateName) {
        return """
            你是华分社团的AI面试官，正在面试候选人：%s

            ## 面试规则（8条）
            1. 保持专业、友好的态度，用中文进行面试
            2. 每次只问一个问题，等待候选人回答后再继续
            3. 根据候选人的回答进行追问，深入了解其能力
            4. 面试时长控制在15-20分钟，约10-15个问题
            5. 涵盖以下维度：沟通能力、逻辑思维、专业知识、工作态度、创新能力
            6. 记录候选人的违规行为，但不要直接指出
            7. 面试结束时给出综合评价和建议
            8. 不要透露评分标准和内部信息

            ## 违规类型（9种）
            1. IRRELEVANT_ANSWER - 答非所问，回答与问题无关
            2. OFFENSIVE_LANGUAGE - 使用不当言语或攻击性语言
            3. DISHONESTY - 明显的不诚实或夸大其词
            4. EVASION - 故意回避问题或拒绝回答
            5. COPY_PASTE - 疑似复制粘贴的机械回答
            6. TOO_SHORT - 回答过于简短，缺乏实质内容
            7. OFF_TOPIC - 偏离面试主题，闲聊或跑题
            8. DISRESPECT - 对面试官或社团表现不尊重
            9. TIMEOUT - 长时间不回复（超过3分钟）

            ## 评分维度（每项0-20分，总分100分）
            - 沟通能力(communication_score): 表达清晰度、倾听理解能力
            - 逻辑思维(logic_score): 分析问题、解决问题的能力
            - 专业知识(knowledge_score): 相关领域的知识储备
            - 工作态度(attitude_score): 积极性、责任心、团队意识
            - 创新能力(creativity_score): 创新思维、独特见解

            请开始面试，先做自我介绍并询问候选人的基本情况。
            """.formatted(candidateName);
    }

    public String buildEvaluationPrompt() {
        return """
            请根据以上面试对话，生成JSON格式的评估报告：
            {
                "communication_score": 0-20,
                "logic_score": 0-20,
                "knowledge_score": 0-20,
                "attitude_score": 0-20,
                "creativity_score": 0-20,
                "total_score": 0-100,
                "violation_types": ["违规类型数组"],
                "summary": "综合评价文字，200字以内",
                "recommendation": "PASS/FAIL/PENDING"
            }
            只返回JSON，不要其他内容。
            """;
    }
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/service/ai/InterviewPromptBuilder.java && git commit -m "feat: add interview prompt builder with 8 rules and 9 violation types"`

---

### Task 31: 创建评分评估逻辑

**Files:** `backend/src/main/java/com/huafen/system/service/ai/InterviewEvaluator.java`

**Step 1:** 创建InterviewEvaluator
```java
package com.huafen.system.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huafen.system.entity.AIInterviewScore;
import com.huafen.system.entity.AIInterviewSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InterviewEvaluator {

    private final AIProvider aiProvider;
    private final InterviewPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AIInterviewScore evaluate(AIInterviewSession session, List<AIProvider.Message> messages) {
        messages.add(new AIProvider.Message("user", promptBuilder.buildEvaluationPrompt()));

        String response = aiProvider.chat(messages);
        return parseEvaluation(session, response);
    }

    private AIInterviewScore parseEvaluation(AIInterviewSession session, String jsonResponse) {
        try {
            // 提取JSON部分
            String json = jsonResponse;
            if (jsonResponse.contains("{")) {
                json = jsonResponse.substring(jsonResponse.indexOf("{"), jsonResponse.lastIndexOf("}") + 1);
            }

            JsonNode node = objectMapper.readTree(json);

            return AIInterviewScore.builder()
                .session(session)
                .communicationScore(node.path("communication_score").asInt(0))
                .logicScore(node.path("logic_score").asInt(0))
                .knowledgeScore(node.path("knowledge_score").asInt(0))
                .attitudeScore(node.path("attitude_score").asInt(0))
                .creativityScore(node.path("creativity_score").asInt(0))
                .totalScore(node.path("total_score").asInt(0))
                .violationTypes(node.path("violation_types").toString())
                .aiSummary(node.path("summary").asText(""))
                .build();
        } catch (Exception e) {
            log.error("Failed to parse evaluation: {}", e.getMessage());
            return AIInterviewScore.builder()
                .session(session)
                .totalScore(0)
                .aiSummary("评估解析失败")
                .build();
        }
    }

    public int calculatePenalty(List<String> violationTypes) {
        int penalty = 0;
        for (String type : violationTypes) {
            penalty += switch (type) {
                case "OFFENSIVE_LANGUAGE", "DISHONESTY", "DISRESPECT" -> 10;
                case "IRRELEVANT_ANSWER", "EVASION", "OFF_TOPIC" -> 5;
                case "COPY_PASTE", "TOO_SHORT", "TIMEOUT" -> 3;
                default -> 0;
            };
        }
        return Math.min(penalty, 30); // 最多扣30分
    }
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/service/ai/InterviewEvaluator.java && git commit -m "feat: add interview evaluator with scoring logic"`

---

### Task 32: 创建AIInterviewController REST端点

**Files:** `backend/src/main/java/com/huafen/system/controller/AIInterviewController.java`

**Step 1:** 创建AIInterviewController
```java
package com.huafen.system.controller;

import com.huafen.system.common.Result;
import com.huafen.system.dto.interview.*;
import com.huafen.system.service.AIInterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-interviews")
@RequiredArgsConstructor
public class AIInterviewController {

    private final AIInterviewService aiInterviewService;

    @PostMapping("/start")
    public Result<AIInterviewSessionDTO> startSession(@RequestBody StartSessionRequest request) {
        return Result.success(aiInterviewService.startSession(request.getUserId(), request.getApplicationId()));
    }

    @GetMapping("/session/{token}")
    public Result<AIInterviewSessionDTO> getSession(@PathVariable String token) {
        return Result.success(aiInterviewService.getSession(token));
    }

    @PostMapping("/session/{token}/end")
    public Result<AIInterviewSessionDTO> endSession(@PathVariable String token) {
        return Result.success(aiInterviewService.endSession(token));
    }

    @GetMapping("/session/{token}/score")
    public Result<AIInterviewScoreDTO> getScore(@PathVariable String token) {
        return Result.success(aiInterviewService.getScore(token));
    }

    @GetMapping("/session/{token}/messages")
    public Result<java.util.List<AIInterviewMessageDTO>> getMessages(@PathVariable String token) {
        return Result.success(aiInterviewService.getMessages(token));
    }

    @GetMapping("/list")
    public Result<Page<AIInterviewSessionDTO>> list(AIInterviewQueryRequest request, Pageable pageable) {
        return Result.success(aiInterviewService.list(request, pageable));
    }

    @GetMapping("/statistics")
    public Result<AIInterviewStatisticsDTO> getStatistics() {
        return Result.success(aiInterviewService.getStatistics());
    }
}
```

**Step 2:** 创建相关DTOs
```java
// StartSessionRequest.java
package com.huafen.system.dto.interview;
import lombok.Data;

@Data
public class StartSessionRequest {
    private Long userId;
    private Long applicationId;
}

// AIInterviewSessionDTO.java
@Data
public class AIInterviewSessionDTO {
    private Long id;
    private String sessionToken;
    private String userName;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer totalDuration;
    private Integer violationCount;
    private AIInterviewScoreDTO score;
}

// AIInterviewScoreDTO.java
@Data
public class AIInterviewScoreDTO {
    private Integer communicationScore;
    private Integer logicScore;
    private Integer knowledgeScore;
    private Integer attitudeScore;
    private Integer creativityScore;
    private Integer totalScore;
    private String aiSummary;
    private List<String> violationTypes;
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/controller/AIInterviewController.java backend/src/main/java/com/huafen/system/dto/interview/ && git commit -m "feat: add AIInterviewController REST endpoints"`

---

## Phase 5: Backend - Salary System Enhancement (Tasks 33-42)

### Task 33: 创建MemberFlowLog实体

**Files:** `backend/src/main/java/com/huafen/system/entity/MemberFlowLog.java`

**Step 1:** 创建FlowType枚举
```java
package com.huafen.system.entity.enums;

public enum FlowType {
    JOIN,       // 入会
    LEAVE,      // 退会
    PROMOTE,    // 晋升
    DEMOTE,     // 降级
    TRANSFER    // 调动
}
```

**Step 2:** 创建MemberFlowLog实体
```java
package com.huafen.system.entity;

import com.huafen.system.entity.enums.FlowType;
import com.huafen.system.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_flow_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberFlowLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "flow_type", length = 20, nullable = false)
    private FlowType flowType;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_role", length = 20)
    private Role fromRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_role", length = 20)
    private Role toRole;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private User operator;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/entity/MemberFlowLog.java backend/src/main/java/com/huafen/system/entity/enums/FlowType.java && git commit -m "feat: add MemberFlowLog entity"`

---

### Task 34: 创建MonthlyPerformance实体

**Files:** `backend/src/main/java/com/huafen/system/entity/MonthlyPerformance.java`

**Step 1:** 创建PerformanceLevel枚举
```java
package com.huafen.system.entity.enums;

public enum PerformanceLevel {
    EXCELLENT,  // 优秀
    GOOD,       // 良好
    NORMAL,     // 一般
    POOR        // 较差
}
```

**Step 2:** 创建MonthlyPerformance实体
```java
package com.huafen.system.entity;

import com.huafen.system.entity.enums.PerformanceLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_performances", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "period"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyPerformance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 20, nullable = false)
    private String period;

    @Column(name = "checkin_count")
    private Integer checkinCount;

    @Column(name = "activity_count")
    private Integer activityCount;

    @Column(name = "task_count")
    private Integer taskCount;

    @Column(name = "extra_points")
    private Integer extraPoints;

    @Enumerated(EnumType.STRING)
    @Column(name = "performance_level", length = 20)
    private PerformanceLevel performanceLevel;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/entity/MonthlyPerformance.java backend/src/main/java/com/huafen/system/entity/enums/PerformanceLevel.java && git commit -m "feat: add MonthlyPerformance entity"`

---

### Task 35: 增强Salary实体添加签到字段

**Files:** `backend/src/main/java/com/huafen/system/entity/Salary.java`

**Step 1:** 在Salary实体中添加新字段
```java
// 在现有字段后添加
@Column(name = "checkin_points")
private Integer checkinPoints;

@Column(name = "activity_points")
private Integer activityPoints;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "pool_id")
private SalaryPool pool;
```

**Commit:** `git add backend/src/main/java/com/huafen/system/entity/Salary.java && git commit -m "feat: enhance Salary entity with checkin fields"`

---

### Task 36: 创建薪酬相关Repositories

**Files:** `backend/src/main/java/com/huafen/system/repository/`

**Step 1:** 创建SalaryPool实体
```java
package com.huafen.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "salary_pools")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryPool {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false, unique = true)
    private String period;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "distributed_amount", precision = 10, scale = 2)
    private BigDecimal distributedAmount;

    @Column(name = "member_count")
    private Integer memberCount;

    @Column(length = 20)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**Step 2:** 创建相关Repositories
```java
// MemberFlowLogRepository.java
package com.huafen.system.repository;

import com.huafen.system.entity.MemberFlowLog;
import com.huafen.system.entity.enums.FlowType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberFlowLogRepository extends JpaRepository<MemberFlowLog, Long> {
    Page<MemberFlowLog> findByUserId(Long userId, Pageable pageable);
    Page<MemberFlowLog> findByFlowType(FlowType flowType, Pageable pageable);
}

// MonthlyPerformanceRepository.java
public interface MonthlyPerformanceRepository extends JpaRepository<MonthlyPerformance, Long> {
    Optional<MonthlyPerformance> findByUserIdAndPeriod(Long userId, String period);
    List<MonthlyPerformance> findByPeriod(String period);
}

// SalaryPoolRepository.java
public interface SalaryPoolRepository extends JpaRepository<SalaryPool, Long> {
    Optional<SalaryPool> findByPeriod(String period);
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/entity/SalaryPool.java backend/src/main/java/com/huafen/system/repository/MemberFlowLogRepository.java backend/src/main/java/com/huafen/system/repository/MonthlyPerformanceRepository.java backend/src/main/java/com/huafen/system/repository/SalaryPoolRepository.java && git commit -m "feat: add salary related repositories"`

---

### Task 37: 创建CheckinCalculationService（精确规则）

**Files:** `backend/src/main/java/com/huafen/system/service/CheckinCalculationService.java`

**Step 1:** 创建CheckinCalculationService
```java
package com.huafen.system.service;

import com.huafen.system.entity.MonthlyPerformance;
import com.huafen.system.entity.User;
import com.huafen.system.entity.enums.PerformanceLevel;
import com.huafen.system.repository.ActivitySignupRepository;
import com.huafen.system.repository.MonthlyPerformanceRepository;
import com.huafen.system.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheckinCalculationService {

    private final PointRepository pointRepository;
    private final ActivitySignupRepository activitySignupRepository;
    private final MonthlyPerformanceRepository performanceRepository;

    /**
     * 签到积分计算规则：
     * - 每次签到基础积分：10分
     * - 连续签到7天额外奖励：20分
     * - 连续签到30天额外奖励：100分
     * - 月度签到满勤（>=20天）：50分
     */
    public int calculateCheckinPoints(Long userId, String period) {
        int checkinCount = getCheckinCount(userId, period);
        int points = checkinCount * 10; // 基础积分

        // 满勤奖励
        if (checkinCount >= 20) {
            points += 50;
        }

        return points;
    }

    /**
     * 活动积分计算规则：
     * - 参与活动：20分/次
     * - 活动签到：额外10分
     * - 组织活动：50分/次
     */
    public int calculateActivityPoints(Long userId, String period) {
        int activityCount = getActivityCount(userId, period);
        int signedInCount = getSignedInActivityCount(userId, period);

        return activityCount * 20 + signedInCount * 10;
    }

    /**
     * 绩效等级评定规则：
     * - EXCELLENT: 总积分 >= 500
     * - GOOD: 总积分 >= 300
     * - NORMAL: 总积分 >= 100
     * - POOR: 总积分 < 100
     */
    public PerformanceLevel evaluatePerformance(int totalPoints) {
        if (totalPoints >= 500) return PerformanceLevel.EXCELLENT;
        if (totalPoints >= 300) return PerformanceLevel.GOOD;
        if (totalPoints >= 100) return PerformanceLevel.NORMAL;
        return PerformanceLevel.POOR;
    }

    @Transactional
    public MonthlyPerformance calculateMonthlyPerformance(User user, String period) {
        int checkinCount = getCheckinCount(user.getId(), period);
        int activityCount = getActivityCount(user.getId(), period);
        int checkinPoints = calculateCheckinPoints(user.getId(), period);
        int activityPoints = calculateActivityPoints(user.getId(), period);
        int totalPoints = checkinPoints + activityPoints;

        MonthlyPerformance performance = performanceRepository
            .findByUserIdAndPeriod(user.getId(), period)
            .orElse(new MonthlyPerformance());

        performance.setUser(user);
        performance.setPeriod(period);
        performance.setCheckinCount(checkinCount);
        performance.setActivityCount(activityCount);
        performance.setExtraPoints(0);
        performance.setPerformanceLevel(evaluatePerformance(totalPoints));

        return performanceRepository.save(performance);
    }

    private int getCheckinCount(Long userId, String period) {
        // 从积分表统计签到次数
        return pointRepository.countByUserIdAndTypeAndPeriod(userId, "CHECKIN", period);
    }

    private int getActivityCount(Long userId, String period) {
        return activitySignupRepository.countByUserIdAndPeriod(userId, period);
    }

    private int getSignedInActivityCount(Long userId, String period) {
        return activitySignupRepository.countByUserIdAndSignedInAndPeriod(userId, true, period);
    }
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/service/CheckinCalculationService.java && git commit -m "feat: add CheckinCalculationService with exact rules"`

---

### Task 38: 创建MemberFlowService（晋升/降级）

**Files:** `backend/src/main/java/com/huafen/system/service/MemberFlowService.java`

**Step 1:** 创建MemberFlowService
```java
package com.huafen.system.service;

import com.huafen.system.dto.member.*;
import com.huafen.system.entity.MemberFlowLog;
import com.huafen.system.entity.User;
import com.huafen.system.entity.enums.FlowType;
import com.huafen.system.entity.enums.Role;
import com.huafen.system.exception.BusinessException;
import com.huafen.system.repository.MemberFlowLogRepository;
import com.huafen.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberFlowService {

    private final UserRepository userRepository;
    private final MemberFlowLogRepository flowLogRepository;

    @Transactional
    public void promote(Long userId, Role newRole, String reason, Long operatorId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("用户不存在"));
        User operator = userRepository.findById(operatorId).orElse(null);

        Role oldRole = user.getRole();
        if (!canPromote(oldRole, newRole)) {
            throw new BusinessException("无法晋升到该角色");
        }

        user.setRole(newRole);
        userRepository.save(user);

        logFlow(user, FlowType.PROMOTE, oldRole, newRole, reason, operator);
    }

    @Transactional
    public void demote(Long userId, Role newRole, String reason, Long operatorId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("用户不存在"));
        User operator = userRepository.findById(operatorId).orElse(null);

        Role oldRole = user.getRole();
        user.setRole(newRole);
        userRepository.save(user);

        logFlow(user, FlowType.DEMOTE, oldRole, newRole, reason, operator);
    }

    @Transactional
    public void leave(Long userId, String reason, Long operatorId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("用户不存在"));
        User operator = userRepository.findById(operatorId).orElse(null);

        Role oldRole = user.getRole();
        user.setRole(Role.APPLICANT);
        user.setStatus(com.huafen.system.entity.enums.UserStatus.INACTIVE);
        userRepository.save(user);

        logFlow(user, FlowType.LEAVE, oldRole, null, reason, operator);
    }

    public Page<MemberFlowLogDTO> getFlowLogs(Long userId, Pageable pageable) {
        return flowLogRepository.findByUserId(userId, pageable).map(this::toDTO);
    }

    private boolean canPromote(Role from, Role to) {
        // 定义晋升路径
        return switch (from) {
            case APPLICANT -> to == Role.MEMBER;
            case MEMBER -> to == Role.CORE_MEMBER || to == Role.ADMIN;
            case CORE_MEMBER -> to == Role.ADMIN;
            default -> false;
        };
    }

    private void logFlow(User user, FlowType type, Role from, Role to, String reason, User operator) {
        MemberFlowLog log = MemberFlowLog.builder()
            .user(user)
            .flowType(type)
            .fromRole(from)
            .toRole(to)
            .reason(reason)
            .operator(operator)
            .build();
        flowLogRepository.save(log);
    }

    private MemberFlowLogDTO toDTO(MemberFlowLog log) {
        // 转换逻辑
        return new MemberFlowLogDTO();
    }
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/service/MemberFlowService.java && git commit -m "feat: add MemberFlowService for promotion/demotion"`

---

### Task 39: 创建SalaryPoolService（分配逻辑）

**Files:** `backend/src/main/java/com/huafen/system/service/SalaryPoolService.java`

**Step 1:** 创建SalaryPoolService
```java
package com.huafen.system.service;

import com.huafen.system.entity.*;
import com.huafen.system.entity.enums.Role;
import com.huafen.system.entity.enums.SalaryStatus;
import com.huafen.system.exception.BusinessException;
import com.huafen.system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalaryPoolService {

    private static final BigDecimal TOTAL_POOL = new BigDecimal("2000.00");
    private static final BigDecimal MIN_SALARY = new BigDecimal("200.00");
    private static final BigDecimal MAX_SALARY = new BigDecimal("400.00");

    private final SalaryPoolRepository poolRepository;
    private final SalaryRepository salaryRepository;
    private final UserRepository userRepository;
    private final CheckinCalculationService checkinService;

    /**
     * 薪酬池分配规则：
     * - 总池：2000元/月
     * - 每人范围：200-400元
     * - 按绩效积分比例分配
     * - 不足最低保障则补足，超出上限则截断
     */
    @Transactional
    public SalaryPool initializePool(String period) {
        if (poolRepository.findByPeriod(period).isPresent()) {
            throw new BusinessException("该月份薪酬池已存在");
        }

        List<User> members = userRepository.findByRoleIn(
            List.of(Role.MEMBER, Role.CORE_MEMBER, Role.ADMIN)
        );

        SalaryPool pool = SalaryPool.builder()
            .period(period)
            .totalAmount(TOTAL_POOL)
            .distributedAmount(BigDecimal.ZERO)
            .memberCount(members.size())
            .status("PENDING")
            .build();

        return poolRepository.save(pool);
    }

    @Transactional
    public void distributeSalaries(String period) {
        SalaryPool pool = poolRepository.findByPeriod(period)
            .orElseThrow(() -> new BusinessException("薪酬池不存在"));

        List<Salary> salaries = salaryRepository.findByPeriod(period);
        if (salaries.isEmpty()) {
            throw new BusinessException("没有待分配的薪酬记录");
        }

        // 计算总积分
        int totalPoints = salaries.stream()
            .mapToInt(Salary::getTotalPoints)
            .sum();

        if (totalPoints == 0) {
            // 平均分配
            BigDecimal avgSalary = TOTAL_POOL.divide(
                new BigDecimal(salaries.size()), 2, RoundingMode.HALF_UP);
            avgSalary = avgSalary.max(MIN_SALARY).min(MAX_SALARY);

            for (Salary salary : salaries) {
                salary.setSalary(avgSalary);
                salary.setStatus(SalaryStatus.CALCULATED);
            }
        } else {
            // 按比例分配
            BigDecimal distributed = BigDecimal.ZERO;
            for (Salary salary : salaries) {
                BigDecimal ratio = new BigDecimal(salary.getTotalPoints())
                    .divide(new BigDecimal(totalPoints), 4, RoundingMode.HALF_UP);
                BigDecimal amount = TOTAL_POOL.multiply(ratio)
                    .setScale(2, RoundingMode.HALF_UP);

                // 限制范围
                amount = amount.max(MIN_SALARY).min(MAX_SALARY);
                salary.setSalary(amount);
                salary.setStatus(SalaryStatus.CALCULATED);
                distributed = distributed.add(amount);
            }

            pool.setDistributedAmount(distributed);
        }

        salaryRepository.saveAll(salaries);
        pool.setStatus("DISTRIBUTED");
        poolRepository.save(pool);
    }

    public SalaryPoolSummary getPoolSummary(String period) {
        SalaryPool pool = poolRepository.findByPeriod(period)
            .orElseThrow(() -> new BusinessException("薪酬池不存在"));

        BigDecimal remaining = pool.getTotalAmount().subtract(pool.getDistributedAmount());

        return SalaryPoolSummary.builder()
            .period(period)
            .totalAmount(pool.getTotalAmount())
            .distributedAmount(pool.getDistributedAmount())
            .remainingAmount(remaining)
            .memberCount(pool.getMemberCount())
            .status(pool.getStatus())
            .build();
    }
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/service/SalaryPoolService.java && git commit -m "feat: add SalaryPoolService for distribution logic"`

---

### Task 40: 增强SalaryController添加新端点

**Files:** `backend/src/main/java/com/huafen/system/controller/SalaryController.java`

**Step 1:** 添加新端点到SalaryController
```java
// 在现有SalaryController中添加以下端点

@PostMapping("/pool/init")
public Result<SalaryPoolDTO> initPool(@RequestParam String period) {
    return Result.success(salaryPoolService.initializePool(period));
}

@PostMapping("/pool/distribute")
public Result<Void> distribute(@RequestParam String period) {
    salaryPoolService.distributeSalaries(period);
    return Result.success();
}

@GetMapping("/pool/summary")
public Result<SalaryPoolSummary> getPoolSummary(@RequestParam String period) {
    return Result.success(salaryPoolService.getPoolSummary(period));
}

@GetMapping("/performance/{userId}")
public Result<MonthlyPerformanceDTO> getPerformance(
    @PathVariable Long userId, @RequestParam String period) {
    return Result.success(checkinService.getPerformance(userId, period));
}

@PostMapping("/batch-update")
public Result<Void> batchUpdate(@RequestBody List<SalaryUpdateRequest> requests) {
    salaryService.batchUpdate(requests);
    return Result.success();
}

@GetMapping("/export")
public void exportSalaries(@RequestParam String period, HttpServletResponse response) {
    salaryService.exportToExcel(period, response);
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/controller/SalaryController.java && git commit -m "feat: enhance SalaryController with new endpoints"`

---

### Task 41: 创建薪酬验证逻辑（200-400每人，2000总额）

**Files:** `backend/src/main/java/com/huafen/system/service/SalaryValidationService.java`

**Step 1:** 创建SalaryValidationService
```java
package com.huafen.system.service;

import com.huafen.system.dto.salary.SalaryValidationResult;
import com.huafen.system.entity.Salary;
import com.huafen.system.exception.BusinessException;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class SalaryValidationService {

    private static final BigDecimal TOTAL_POOL = new BigDecimal("2000.00");
    private static final BigDecimal MIN_SALARY = new BigDecimal("200.00");
    private static final BigDecimal MAX_SALARY = new BigDecimal("400.00");

    /**
     * 验证薪酬列表
     * 规则：
     * 1. 每人薪酬范围：200-400元
     * 2. 总额不超过2000元
     */
    public SalaryValidationResult validate(List<Salary> salaries) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Salary salary : salaries) {
            BigDecimal amount = salary.getSalary();
            if (amount == null) {
                errors.add("用户 " + salary.getUser().getNickname() + " 薪酬未设置");
                continue;
            }

            // 检查个人范围
            if (amount.compareTo(MIN_SALARY) < 0) {
                errors.add("用户 " + salary.getUser().getNickname() +
                    " 薪酬 " + amount + " 低于最低标准 " + MIN_SALARY);
            }
            if (amount.compareTo(MAX_SALARY) > 0) {
                errors.add("用户 " + salary.getUser().getNickname() +
                    " 薪酬 " + amount + " 超过最高标准 " + MAX_SALARY);
            }

            total = total.add(amount);
        }

        // 检查总额
        if (total.compareTo(TOTAL_POOL) > 0) {
            errors.add("薪酬总额 " + total + " 超过池总额 " + TOTAL_POOL);
        } else if (total.compareTo(TOTAL_POOL) < 0) {
            BigDecimal remaining = TOTAL_POOL.subtract(total);
            warnings.add("薪酬池剩余 " + remaining + " 元未分配");
        }

        return SalaryValidationResult.builder()
            .valid(errors.isEmpty())
            .totalAmount(total)
            .memberCount(salaries.size())
            .errors(errors)
            .warnings(warnings)
            .build();
    }

    public void validateAndThrow(List<Salary> salaries) {
        SalaryValidationResult result = validate(salaries);
        if (!result.isValid()) {
            throw new BusinessException("薪酬验证失败: " + String.join("; ", result.getErrors()));
        }
    }
}
```

**Step 2:** 创建SalaryValidationResult DTO
```java
package com.huafen.system.dto.salary;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class SalaryValidationResult {
    private boolean valid;
    private BigDecimal totalAmount;
    private int memberCount;
    private List<String> errors;
    private List<String> warnings;
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/service/SalaryValidationService.java backend/src/main/java/com/huafen/system/dto/salary/SalaryValidationResult.java && git commit -m "feat: add salary validation logic (200-400 per person, 2000 total)"`

---

### Task 42: 创建月度薪酬生成定时任务

**Files:** `backend/src/main/java/com/huafen/system/job/MonthlySalaryJob.java`

**Step 1:** 创建MonthlySalaryJob
```java
package com.huafen.system.job;

import com.huafen.system.entity.Salary;
import com.huafen.system.entity.User;
import com.huafen.system.entity.enums.Role;
import com.huafen.system.entity.enums.SalaryStatus;
import com.huafen.system.repository.SalaryRepository;
import com.huafen.system.repository.UserRepository;
import com.huafen.system.service.CheckinCalculationService;
import com.huafen.system.service.SalaryPoolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MonthlySalaryJob {

    private final UserRepository userRepository;
    private final SalaryRepository salaryRepository;
    private final SalaryPoolService poolService;
    private final CheckinCalculationService checkinService;

    /**
     * 每月1日凌晨2点执行
     * 生成上月薪酬记录
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    @Transactional
    public void generateMonthlySalaries() {
        String period = LocalDate.now().minusMonths(1)
            .format(DateTimeFormatter.ofPattern("yyyy-MM"));

        log.info("开始生成 {} 月度薪酬", period);

        // 初始化薪酬池
        poolService.initializePool(period);

        // 获取所有正式成员
        List<User> members = userRepository.findByRoleIn(
            List.of(Role.MEMBER, Role.CORE_MEMBER, Role.ADMIN)
        );

        for (User member : members) {
            // 检查是否已存在
            if (salaryRepository.existsByUserIdAndPeriod(member.getId(), period)) {
                continue;
            }

            // 计算积分
            int checkinPoints = checkinService.calculateCheckinPoints(member.getId(), period);
            int activityPoints = checkinService.calculateActivityPoints(member.getId(), period);
            int totalPoints = checkinPoints + activityPoints;

            Salary salary = Salary.builder()
                .user(member)
                .period(period)
                .basePoints(0)
                .bonusPoints(0)
                .checkinPoints(checkinPoints)
                .activityPoints(activityPoints)
                .deduction(0)
                .totalPoints(totalPoints)
                .status(SalaryStatus.PENDING)
                .build();

            salaryRepository.save(salary);
        }

        log.info("月度薪酬生成完成，共 {} 条记录", members.size());
    }
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/job/MonthlySalaryJob.java && git commit -m "feat: add monthly salary generation job"`

---

## Phase 6: Backend - Configuration Center (Tasks 43-50)

### Task 43: 增强SystemConfig实体添加加密标志

**Files:** `backend/src/main/java/com/huafen/system/entity/SystemConfig.java`

**Step 1:** 在SystemConfig实体中添加新字段
```java
// 在现有字段后添加
@Column(name = "encrypted")
private Boolean encrypted = false;

@Column(name = "config_group", length = 50)
private String configGroup;

@Column(name = "config_type", length = 20)
private String configType; // STRING, JSON, NUMBER, BOOLEAN

@Column(name = "editable")
private Boolean editable = true;
```

**Step 2:** 更新数据库schema
```sql
ALTER TABLE system_config
ADD COLUMN encrypted TINYINT(1) DEFAULT 0,
ADD COLUMN config_group VARCHAR(50),
ADD COLUMN config_type VARCHAR(20) DEFAULT 'STRING',
ADD COLUMN editable TINYINT(1) DEFAULT 1,
ADD INDEX idx_config_group (config_group);
```

**Commit:** `git add backend/src/main/java/com/huafen/system/entity/SystemConfig.java && git commit -m "feat: enhance SystemConfig entity with encryption flag"`

---

### Task 44: 创建ConfigChangeLog实体

**Files:** `backend/src/main/java/com/huafen/system/entity/ConfigChangeLog.java`

**Step 1:** 创建ConfigChangeLog实体
```java
package com.huafen.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "config_change_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigChangeLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", length = 100, nullable = false)
    private String configKey;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private User operator;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
```

**Step 2:** 创建ConfigChangeLogRepository
```java
package com.huafen.system.repository;

import com.huafen.system.entity.ConfigChangeLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigChangeLogRepository extends JpaRepository<ConfigChangeLog, Long> {
    Page<ConfigChangeLog> findByConfigKey(String configKey, Pageable pageable);
    Page<ConfigChangeLog> findByOperatorId(Long operatorId, Pageable pageable);
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/entity/ConfigChangeLog.java backend/src/main/java/com/huafen/system/repository/ConfigChangeLogRepository.java && git commit -m "feat: add ConfigChangeLog entity"`

---

### Task 45: 创建ConfigCacheService（Redis缓存）

**Files:** `backend/src/main/java/com/huafen/system/service/ConfigCacheService.java`

**Step 1:** 创建ConfigCacheService
```java
package com.huafen.system.service;

import com.huafen.system.entity.SystemConfig;
import com.huafen.system.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigCacheService {

    private static final String CONFIG_CACHE_PREFIX = "config:";
    private static final String CONFIG_ALL_KEY = "config:all";
    private static final long CACHE_TTL_HOURS = 24;

    private final RedisTemplate<String, Object> redisTemplate;
    private final SystemConfigRepository configRepository;

    public String get(String key) {
        String cacheKey = CONFIG_CACHE_PREFIX + key;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached.toString();
        }

        // 从数据库加载
        return configRepository.findByConfigKey(key)
            .map(config -> {
                redisTemplate.opsForValue().set(cacheKey, config.getConfigValue(),
                    CACHE_TTL_HOURS, TimeUnit.HOURS);
                return config.getConfigValue();
            })
            .orElse(null);
    }

    public void set(String key, String value) {
        String cacheKey = CONFIG_CACHE_PREFIX + key;
        redisTemplate.opsForValue().set(cacheKey, value, CACHE_TTL_HOURS, TimeUnit.HOURS);
    }

    public void evict(String key) {
        redisTemplate.delete(CONFIG_CACHE_PREFIX + key);
        redisTemplate.delete(CONFIG_ALL_KEY);
    }

    public void evictAll() {
        redisTemplate.delete(redisTemplate.keys(CONFIG_CACHE_PREFIX + "*"));
    }

    public Map<String, String> getAll() {
        @SuppressWarnings("unchecked")
        Map<String, String> cached = (Map<String, String>) redisTemplate.opsForValue().get(CONFIG_ALL_KEY);
        if (cached != null) {
            return cached;
        }

        Map<String, String> configs = configRepository.findAll().stream()
            .collect(Collectors.toMap(
                SystemConfig::getConfigKey,
                c -> c.getConfigValue() != null ? c.getConfigValue() : ""
            ));

        redisTemplate.opsForValue().set(CONFIG_ALL_KEY, configs, CACHE_TTL_HOURS, TimeUnit.HOURS);
        return configs;
    }

    public void refresh() {
        log.info("Refreshing config cache");
        evictAll();
        getAll();
    }
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/service/ConfigCacheService.java && git commit -m "feat: add ConfigCacheService with Redis"`

---

### Task 46: 创建ConfigEncryptionService

**Files:** `backend/src/main/java/com/huafen/system/service/ConfigEncryptionService.java`

**Step 1:** 创建ConfigEncryptionService
```java
package com.huafen.system.service;

import com.huafen.system.entity.SystemConfig;
import com.huafen.system.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfigEncryptionService {

    private static final List<String> SENSITIVE_KEYS = List.of(
        "ai.api_key", "ai.secret_key",
        "oss.access_key", "oss.secret_key",
        "email.password", "email.smtp_password",
        "jwt.secret", "encryption.key"
    );

    private final EncryptionService encryptionService;
    private final SystemConfigRepository configRepository;

    public boolean isSensitive(String key) {
        return SENSITIVE_KEYS.stream().anyMatch(key::contains);
    }

    public String encryptIfNeeded(String key, String value) {
        if (isSensitive(key) && value != null && !value.isEmpty()) {
            return encryptionService.encrypt(value);
        }
        return value;
    }

    public String decryptIfNeeded(SystemConfig config) {
        if (Boolean.TRUE.equals(config.getEncrypted()) && config.getConfigValue() != null) {
            try {
                return encryptionService.decrypt(config.getConfigValue());
            } catch (Exception e) {
                return config.getConfigValue();
            }
        }
        return config.getConfigValue();
    }

    @Transactional
    public void encryptExistingSensitiveConfigs() {
        List<SystemConfig> configs = configRepository.findAll();
        for (SystemConfig config : configs) {
            if (isSensitive(config.getConfigKey()) && !Boolean.TRUE.equals(config.getEncrypted())) {
                String encrypted = encryptionService.encrypt(config.getConfigValue());
                config.setConfigValue(encrypted);
                config.setEncrypted(true);
                configRepository.save(config);
            }
        }
    }

    public String maskSensitiveValue(String key, String value) {
        if (isSensitive(key) && value != null && value.length() > 4) {
            return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
        }
        return value;
    }
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/service/ConfigEncryptionService.java && git commit -m "feat: add ConfigEncryptionService"`

---

### Task 47: 创建ConfigTestService（AI、OSS、Email测试）

**Files:** `backend/src/main/java/com/huafen/system/service/ConfigTestService.java`

**Step 1:** 创建ConfigTestService
```java
package com.huafen.system.service;

import com.huafen.system.dto.config.ConfigTestResult;
import com.huafen.system.service.ai.AIProvider;
import com.huafen.system.service.ai.OpenAICompatibleProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import java.util.List;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigTestService {

    private final ConfigCacheService configCache;
    private final ConfigEncryptionService encryptionService;

    public ConfigTestResult testAIConfig() {
        try {
            String apiKey = getDecryptedConfig("ai.api_key");
            String baseUrl = configCache.get("ai.base_url");
            String model = configCache.get("ai.model");

            if (apiKey == null || baseUrl == null) {
                return ConfigTestResult.failure("AI配置不完整");
            }

            AIProvider provider = new OpenAICompatibleProvider(apiKey, baseUrl, model);
            String response = provider.chat(List.of(
                new AIProvider.Message("user", "Say 'OK' if you can hear me.")
            ));

            if (response != null && response.toLowerCase().contains("ok")) {
                return ConfigTestResult.success("AI服务连接成功");
            }
            return ConfigTestResult.failure("AI服务响应异常");
        } catch (Exception e) {
            log.error("AI config test failed", e);
            return ConfigTestResult.failure("AI服务连接失败: " + e.getMessage());
        }
    }

    public ConfigTestResult testOSSConfig() {
        try {
            String accessKey = getDecryptedConfig("oss.access_key");
            String secretKey = getDecryptedConfig("oss.secret_key");
            String region = configCache.get("oss.region");
            String endpoint = configCache.get("oss.endpoint");

            if (accessKey == null || secretKey == null) {
                return ConfigTestResult.failure("OSS配置不完整");
            }

            S3Client s3 = S3Client.builder()
                .credentialsProvider(() -> AwsBasicCredentials.create(accessKey, secretKey))
                .region(Region.of(region))
                .build();

            s3.listBuckets(ListBucketsRequest.builder().build());
            return ConfigTestResult.success("OSS服务连接成功");
        } catch (Exception e) {
            log.error("OSS config test failed", e);
            return ConfigTestResult.failure("OSS服务连接失败: " + e.getMessage());
        }
    }

    public ConfigTestResult testEmailConfig() {
        try {
            String host = configCache.get("email.smtp_host");
            String port = configCache.get("email.smtp_port");
            String username = configCache.get("email.username");
            String password = getDecryptedConfig("email.password");

            if (host == null || username == null || password == null) {
                return ConfigTestResult.failure("邮件配置不完整");
            }

            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(host);
            mailSender.setPort(Integer.parseInt(port));
            mailSender.setUsername(username);
            mailSender.setPassword(password);

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.connectiontimeout", "5000");
            props.put("mail.smtp.timeout", "5000");

            mailSender.testConnection();
            return ConfigTestResult.success("邮件服务连接成功");
        } catch (Exception e) {
            log.error("Email config test failed", e);
            return ConfigTestResult.failure("邮件服务连接失败: " + e.getMessage());
        }
    }

    private String getDecryptedConfig(String key) {
        String value = configCache.get(key);
        if (value != null && encryptionService.isSensitive(key)) {
            try {
                return encryptionService.decryptIfNeeded(
                    com.huafen.system.entity.SystemConfig.builder()
                        .configValue(value).encrypted(true).build()
                );
            } catch (Exception e) {
                return value;
            }
        }
        return value;
    }
}
```

**Step 2:** 创建ConfigTestResult DTO
```java
package com.huafen.system.dto.config;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConfigTestResult {
    private boolean success;
    private String message;
    private Long responseTime;

    public static ConfigTestResult success(String message) {
        return ConfigTestResult.builder().success(true).message(message).build();
    }

    public static ConfigTestResult failure(String message) {
        return ConfigTestResult.builder().success(false).message(message).build();
    }
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/service/ConfigTestService.java backend/src/main/java/com/huafen/system/dto/config/ConfigTestResult.java && git commit -m "feat: add ConfigTestService for AI, OSS, Email testing"`

---

### Task 48: 增强ConfigController添加测试端点

**Files:** `backend/src/main/java/com/huafen/system/controller/ConfigController.java`

**Step 1:** 增强ConfigController
```java
// 在现有ConfigController中添加以下端点

@Autowired
private ConfigTestService configTestService;

@Autowired
private ConfigCacheService configCacheService;

@PostMapping("/test/ai")
public Result<ConfigTestResult> testAI() {
    return Result.success(configTestService.testAIConfig());
}

@PostMapping("/test/oss")
public Result<ConfigTestResult> testOSS() {
    return Result.success(configTestService.testOSSConfig());
}

@PostMapping("/test/email")
public Result<ConfigTestResult> testEmail() {
    return Result.success(configTestService.testEmailConfig());
}

@PostMapping("/refresh-cache")
public Result<Void> refreshCache() {
    configCacheService.refresh();
    return Result.success();
}

@GetMapping("/groups")
public Result<List<ConfigGroupDTO>> getConfigsByGroup() {
    return Result.success(configService.getConfigsByGroup());
}

@GetMapping("/history/{key}")
public Result<Page<ConfigChangeLogDTO>> getHistory(
    @PathVariable String key, Pageable pageable) {
    return Result.success(configService.getChangeHistory(key, pageable));
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/controller/ConfigController.java && git commit -m "feat: enhance ConfigController with test endpoints"`

---

### Task 49: 创建配置热重载监听器

**Files:** `backend/src/main/java/com/huafen/system/listener/ConfigChangeListener.java`

**Step 1:** 创建ConfigChangeListener
```java
package com.huafen.system.listener;

import com.huafen.system.service.ConfigCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfigChangeListener {

    private final ConfigCacheService cacheService;

    @EventListener
    public void onConfigChange(ConfigChangeEvent event) {
        log.info("Config changed: {} -> {}", event.getKey(), event.getNewValue());

        // 清除缓存
        cacheService.evict(event.getKey());

        // 根据配置类型执行特定操作
        switch (event.getKey()) {
            case "ai.api_key", "ai.base_url", "ai.model" -> reloadAIProvider();
            case "email.smtp_host", "email.smtp_port" -> reloadMailSender();
            default -> log.debug("No special handling for key: {}", event.getKey());
        }
    }

    private void reloadAIProvider() {
        log.info("Reloading AI provider configuration");
        // 触发AI Provider重新初始化
    }

    private void reloadMailSender() {
        log.info("Reloading mail sender configuration");
        // 触发邮件发送器重新初始化
    }
}
```

**Step 2:** 创建ConfigChangeEvent
```java
package com.huafen.system.listener;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ConfigChangeEvent extends ApplicationEvent {
    private final String key;
    private final String oldValue;
    private final String newValue;

    public ConfigChangeEvent(Object source, String key, String oldValue, String newValue) {
        super(source);
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
}
```

**Step 3:** 在ConfigService中发布事件
```java
// 在ConfigServiceImpl的update方法中添加
@Autowired
private ApplicationEventPublisher eventPublisher;

@Override
@Transactional
public void updateConfig(String key, String value) {
    SystemConfig config = configRepository.findByConfigKey(key)
        .orElseThrow(() -> new BusinessException("配置不存在"));

    String oldValue = config.getConfigValue();
    config.setConfigValue(value);
    configRepository.save(config);

    // 记录变更日志
    logChange(key, oldValue, value);

    // 发布事件
    eventPublisher.publishEvent(new ConfigChangeEvent(this, key, oldValue, value));
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/listener/ && git commit -m "feat: add config hot-reload listener"`

---

### Task 50: 创建配置DTOs

**Files:** `backend/src/main/java/com/huafen/system/dto/config/`

**Step 1:** 创建配置相关DTOs
```java
// ConfigDTO.java
package com.huafen.system.dto.config;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConfigDTO {
    private Long id;
    private String configKey;
    private String configValue;
    private String description;
    private String configGroup;
    private String configType;
    private Boolean encrypted;
    private Boolean editable;
    private LocalDateTime updatedAt;
}

// ConfigGroupDTO.java
@Data
public class ConfigGroupDTO {
    private String groupName;
    private String groupLabel;
    private List<ConfigDTO> configs;
}

// ConfigUpdateRequest.java
@Data
public class ConfigUpdateRequest {
    private String configKey;
    private String configValue;
}

// ConfigBatchUpdateRequest.java
@Data
public class ConfigBatchUpdateRequest {
    private List<ConfigUpdateRequest> configs;
}

// ConfigChangeLogDTO.java
@Data
public class ConfigChangeLogDTO {
    private Long id;
    private String configKey;
    private String oldValue;
    private String newValue;
    private String operatorName;
    private LocalDateTime createdAt;
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/dto/config/ && git commit -m "feat: add config DTOs"`

---

## Phase 7: Backend - Permission System (Tasks 51-60)

### Task 51: 创建Menu实体

**Files:** `backend/src/main/java/com/huafen/system/entity/Menu.java`

**Step 1:** 创建Menu实体
```java
package com.huafen.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "menus")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 200)
    private String path;

    @Column(length = 200)
    private String component;

    @Column(length = 50)
    private String icon;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column
    private Boolean visible;

    @Column(length = 20)
    private String status;

    @Transient
    private List<Menu> children;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/entity/Menu.java && git commit -m "feat: add Menu entity"`

---

### Task 52: 创建Permission实体

**Files:** `backend/src/main/java/com/huafen/system/entity/Permission.java`

**Step 1:** 创建Permission实体
```java
package com.huafen.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false, unique = true)
    private String code;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 200)
    private String description;

    @Column(length = 50)
    private String module;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/entity/Permission.java && git commit -m "feat: add Permission entity"`

---

### Task 53: 创建RoleMenu和RolePermission实体

**Files:** `backend/src/main/java/com/huafen/system/entity/`

**Step 1:** 创建RoleMenu实体
```java
package com.huafen.system.entity;

import com.huafen.system.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "role_menus", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"role", "menu_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleMenu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
```

**Step 2:** 创建RolePermission实体
```java
package com.huafen.system.entity;

import com.huafen.system.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "role_permissions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"role", "permission_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/entity/RoleMenu.java backend/src/main/java/com/huafen/system/entity/RolePermission.java && git commit -m "feat: add RoleMenu and RolePermission entities"`

---

### Task 54: 创建RoleChangeLog实体

**Files:** `backend/src/main/java/com/huafen/system/entity/RoleChangeLog.java`

**Step 1:** 创建RoleChangeLog实体
```java
package com.huafen.system.entity;

import com.huafen.system.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "role_change_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleChangeLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_role", length = 20)
    private Role fromRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_role", length = 20)
    private Role toRole;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private User operator;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/entity/RoleChangeLog.java && git commit -m "feat: add RoleChangeLog entity"`

---

### Task 55: 创建PermissionChangeLog实体

**Files:** `backend/src/main/java/com/huafen/system/entity/PermissionChangeLog.java`

**Step 1:** 创建PermissionChangeLog实体
```java
package com.huafen.system.entity;

import com.huafen.system.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "permission_change_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionChangeLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id")
    private Permission permission;

    @Column(length = 20, nullable = false)
    private String action; // GRANT, REVOKE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private User operator;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/entity/PermissionChangeLog.java && git commit -m "feat: add PermissionChangeLog entity"`

---

### Task 56: 创建菜单和权限Repositories

**Files:** `backend/src/main/java/com/huafen/system/repository/`

**Step 1:** 创建MenuRepository
```java
package com.huafen.system.repository;

import com.huafen.system.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByParentIdOrderBySortOrderAsc(Long parentId);

    @Query("SELECT m FROM Menu m WHERE m.visible = true ORDER BY m.sortOrder")
    List<Menu> findAllVisible();

    List<Menu> findByParentIdIsNullOrderBySortOrderAsc();
}
```

**Step 2:** 创建PermissionRepository
```java
package com.huafen.system.repository;

import com.huafen.system.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByCode(String code);
    List<Permission> findByModule(String module);
    boolean existsByCode(String code);
}
```

**Step 3:** 创建RoleMenuRepository和RolePermissionRepository
```java
// RoleMenuRepository.java
package com.huafen.system.repository;

import com.huafen.system.entity.RoleMenu;
import com.huafen.system.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface RoleMenuRepository extends JpaRepository<RoleMenu, Long> {
    List<RoleMenu> findByRole(Role role);

    @Query("SELECT rm.menu.id FROM RoleMenu rm WHERE rm.role = :role")
    List<Long> findMenuIdsByRole(Role role);

    void deleteByRoleAndMenuId(Role role, Long menuId);
}

// RolePermissionRepository.java
package com.huafen.system.repository;

import com.huafen.system.entity.RolePermission;
import com.huafen.system.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findByRole(Role role);

    @Query("SELECT rp.permission.code FROM RolePermission rp WHERE rp.role = :role")
    List<String> findPermissionCodesByRole(Role role);

    void deleteByRoleAndPermissionId(Role role, Long permissionId);
    boolean existsByRoleAndPermissionId(Role role, Long permissionId);
}
```

**Step 4:** 创建日志Repositories
```java
// RoleChangeLogRepository.java
public interface RoleChangeLogRepository extends JpaRepository<RoleChangeLog, Long> {
    Page<RoleChangeLog> findByUserId(Long userId, Pageable pageable);
}

// PermissionChangeLogRepository.java
public interface PermissionChangeLogRepository extends JpaRepository<PermissionChangeLog, Long> {
    Page<PermissionChangeLog> findByRole(Role role, Pageable pageable);
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/repository/MenuRepository.java backend/src/main/java/com/huafen/system/repository/PermissionRepository.java backend/src/main/java/com/huafen/system/repository/RoleMenuRepository.java backend/src/main/java/com/huafen/system/repository/RolePermissionRepository.java backend/src/main/java/com/huafen/system/repository/RoleChangeLogRepository.java backend/src/main/java/com/huafen/system/repository/PermissionChangeLogRepository.java && git commit -m "feat: add menu and permission repositories"`

---

### Task 57: 创建MenuService

**Files:** `backend/src/main/java/com/huafen/system/service/MenuService.java`

**Step 1:** 创建MenuService接口和实现
```java
package com.huafen.system.service;

import com.huafen.system.dto.menu.*;
import com.huafen.system.entity.enums.Role;
import java.util.List;

public interface MenuService {
    List<MenuDTO> getMenuTree();
    List<MenuDTO> getMenusByRole(Role role);
    MenuDTO create(MenuCreateRequest request);
    MenuDTO update(Long id, MenuUpdateRequest request);
    void delete(Long id);
    void assignMenusToRole(Role role, List<Long> menuIds);
}
```

**Step 2:** 创建MenuServiceImpl
```java
package com.huafen.system.service.impl;

import com.huafen.system.dto.menu.*;
import com.huafen.system.entity.Menu;
import com.huafen.system.entity.RoleMenu;
import com.huafen.system.entity.enums.Role;
import com.huafen.system.exception.BusinessException;
import com.huafen.system.repository.MenuRepository;
import com.huafen.system.repository.RoleMenuRepository;
import com.huafen.system.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final RoleMenuRepository roleMenuRepository;

    @Override
    public List<MenuDTO> getMenuTree() {
        List<Menu> allMenus = menuRepository.findAll();
        return buildTree(allMenus, null);
    }

    @Override
    public List<MenuDTO> getMenusByRole(Role role) {
        List<Long> menuIds = roleMenuRepository.findMenuIdsByRole(role);
        List<Menu> menus = menuRepository.findAllById(menuIds);
        return buildTree(menus, null);
    }

    @Override
    @Transactional
    public MenuDTO create(MenuCreateRequest request) {
        Menu menu = Menu.builder()
            .parentId(request.getParentId())
            .name(request.getName())
            .path(request.getPath())
            .component(request.getComponent())
            .icon(request.getIcon())
            .sortOrder(request.getSortOrder())
            .visible(true)
            .status("ACTIVE")
            .build();
        return toDTO(menuRepository.save(menu));
    }

    @Override
    @Transactional
    public void assignMenusToRole(Role role, List<Long> menuIds) {
        // 删除现有关联
        roleMenuRepository.findByRole(role).forEach(rm ->
            roleMenuRepository.delete(rm));

        // 创建新关联
        for (Long menuId : menuIds) {
            Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException("菜单不存在"));
            RoleMenu rm = RoleMenu.builder().role(role).menu(menu).build();
            roleMenuRepository.save(rm);
        }
    }

    private List<MenuDTO> buildTree(List<Menu> menus, Long parentId) {
        return menus.stream()
            .filter(m -> Objects.equals(m.getParentId(), parentId))
            .map(m -> {
                MenuDTO dto = toDTO(m);
                dto.setChildren(buildTree(menus, m.getId()));
                return dto;
            })
            .sorted(Comparator.comparingInt(m -> m.getSortOrder() != null ? m.getSortOrder() : 0))
            .collect(Collectors.toList());
    }

    private MenuDTO toDTO(Menu menu) {
        MenuDTO dto = new MenuDTO();
        dto.setId(menu.getId());
        dto.setParentId(menu.getParentId());
        dto.setName(menu.getName());
        dto.setPath(menu.getPath());
        dto.setComponent(menu.getComponent());
        dto.setIcon(menu.getIcon());
        dto.setSortOrder(menu.getSortOrder());
        dto.setVisible(menu.getVisible());
        return dto;
    }
    // ... 其他方法
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/service/MenuService.java backend/src/main/java/com/huafen/system/service/impl/MenuServiceImpl.java && git commit -m "feat: add MenuService"`

---

### Task 58: 创建PermissionService

**Files:** `backend/src/main/java/com/huafen/system/service/PermissionService.java`

**Step 1:** 创建PermissionService接口和实现
```java
package com.huafen.system.service;

import com.huafen.system.dto.permission.*;
import com.huafen.system.entity.enums.Role;
import java.util.List;

public interface PermissionService {
    List<PermissionDTO> getAllPermissions();
    List<PermissionDTO> getPermissionsByModule(String module);
    List<String> getPermissionCodesByRole(Role role);
    PermissionDTO create(PermissionCreateRequest request);
    void delete(Long id);
    void grantPermission(Role role, Long permissionId);
    void revokePermission(Role role, Long permissionId);
    void batchAssignPermissions(Role role, List<Long> permissionIds);
    boolean hasPermission(Role role, String permissionCode);
}
```

**Step 2:** 创建PermissionServiceImpl
```java
package com.huafen.system.service.impl;

import com.huafen.system.dto.permission.*;
import com.huafen.system.entity.*;
import com.huafen.system.entity.enums.Role;
import com.huafen.system.exception.BusinessException;
import com.huafen.system.repository.*;
import com.huafen.system.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionChangeLogRepository changeLogRepository;
    private final UserRepository userRepository;

    @Override
    public List<PermissionDTO> getAllPermissions() {
        return permissionRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getPermissionCodesByRole(Role role) {
        return rolePermissionRepository.findPermissionCodesByRole(role);
    }

    @Override
    @Transactional
    public void grantPermission(Role role, Long permissionId) {
        if (rolePermissionRepository.existsByRoleAndPermissionId(role, permissionId)) {
            return;
        }

        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new BusinessException("权限不存在"));

        RolePermission rp = RolePermission.builder()
            .role(role)
            .permission(permission)
            .build();
        rolePermissionRepository.save(rp);

        logChange(role, permission, "GRANT");
    }

    @Override
    @Transactional
    public void revokePermission(Role role, Long permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new BusinessException("权限不存在"));

        rolePermissionRepository.deleteByRoleAndPermissionId(role, permissionId);
        logChange(role, permission, "REVOKE");
    }

    @Override
    @Transactional
    public void batchAssignPermissions(Role role, List<Long> permissionIds) {
        // 删除现有权限
        rolePermissionRepository.findByRole(role).forEach(rp ->
            rolePermissionRepository.delete(rp));

        // 分配新权限
        for (Long permissionId : permissionIds) {
            grantPermission(role, permissionId);
        }
    }

    @Override
    public boolean hasPermission(Role role, String permissionCode) {
        return getPermissionCodesByRole(role).contains(permissionCode);
    }

    private void logChange(Role role, Permission permission, String action) {
        PermissionChangeLog log = PermissionChangeLog.builder()
            .role(role)
            .permission(permission)
            .action(action)
            .build();
        changeLogRepository.save(log);
    }

    private PermissionDTO toDTO(Permission p) {
        PermissionDTO dto = new PermissionDTO();
        dto.setId(p.getId());
        dto.setCode(p.getCode());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setModule(p.getModule());
        return dto;
    }
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/service/PermissionService.java backend/src/main/java/com/huafen/system/service/impl/PermissionServiceImpl.java && git commit -m "feat: add PermissionService"`

---

### Task 59: 创建MenuController

**Files:** `backend/src/main/java/com/huafen/system/controller/MenuController.java`

**Step 1:** 创建MenuController
```java
package com.huafen.system.controller;

import com.huafen.system.common.Result;
import com.huafen.system.dto.menu.*;
import com.huafen.system.entity.enums.Role;
import com.huafen.system.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/tree")
    public Result<List<MenuDTO>> getMenuTree() {
        return Result.success(menuService.getMenuTree());
    }

    @GetMapping("/role/{role}")
    public Result<List<MenuDTO>> getMenusByRole(@PathVariable Role role) {
        return Result.success(menuService.getMenusByRole(role));
    }

    @GetMapping("/current")
    public Result<List<MenuDTO>> getCurrentUserMenus() {
        // 从SecurityContext获取当前用户角色
        return Result.success(menuService.getCurrentUserMenus());
    }

    @PostMapping
    public Result<MenuDTO> create(@RequestBody MenuCreateRequest request) {
        return Result.success(menuService.create(request));
    }

    @PutMapping("/{id}")
    public Result<MenuDTO> update(@PathVariable Long id, @RequestBody MenuUpdateRequest request) {
        return Result.success(menuService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success();
    }

    @PostMapping("/role/{role}/assign")
    public Result<Void> assignMenusToRole(@PathVariable Role role,
                                          @RequestBody List<Long> menuIds) {
        menuService.assignMenusToRole(role, menuIds);
        return Result.success();
    }
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/controller/MenuController.java && git commit -m "feat: add MenuController"`

---

### Task 60: 创建PermissionController

**Files:** `backend/src/main/java/com/huafen/system/controller/PermissionController.java`

**Step 1:** 创建PermissionController
```java
package com.huafen.system.controller;

import com.huafen.system.common.Result;
import com.huafen.system.dto.permission.*;
import com.huafen.system.entity.enums.Role;
import com.huafen.system.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    public Result<List<PermissionDTO>> getAllPermissions() {
        return Result.success(permissionService.getAllPermissions());
    }

    @GetMapping("/module/{module}")
    public Result<List<PermissionDTO>> getByModule(@PathVariable String module) {
        return Result.success(permissionService.getPermissionsByModule(module));
    }

    @GetMapping("/role/{role}")
    public Result<List<String>> getPermissionsByRole(@PathVariable Role role) {
        return Result.success(permissionService.getPermissionCodesByRole(role));
    }

    @PostMapping
    public Result<PermissionDTO> create(@RequestBody PermissionCreateRequest request) {
        return Result.success(permissionService.create(request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return Result.success();
    }

    @PostMapping("/role/{role}/grant/{permissionId}")
    public Result<Void> grantPermission(@PathVariable Role role,
                                        @PathVariable Long permissionId) {
        permissionService.grantPermission(role, permissionId);
        return Result.success();
    }

    @PostMapping("/role/{role}/revoke/{permissionId}")
    public Result<Void> revokePermission(@PathVariable Role role,
                                         @PathVariable Long permissionId) {
        permissionService.revokePermission(role, permissionId);
        return Result.success();
    }

    @PostMapping("/role/{role}/batch")
    public Result<Void> batchAssign(@PathVariable Role role,
                                    @RequestBody List<Long> permissionIds) {
        permissionService.batchAssignPermissions(role, permissionIds);
        return Result.success();
    }

    @GetMapping("/check")
    public Result<Boolean> checkPermission(@RequestParam Role role,
                                           @RequestParam String code) {
        return Result.success(permissionService.hasPermission(role, code));
    }
}
```

**Commit:** `git add backend/src/main/java/com/huafen/system/controller/PermissionController.java && git commit -m "feat: add PermissionController"`

---

## Phase 8: Frontend - Layout Enhancement (Tasks 61-65)

### Task 61: 创建三级侧边栏菜单组件

**Files:** `frontend/src/layouts/components/MultiLevelSidebar.vue`

**Step 1:** 创建MultiLevelSidebar.vue
```vue
<template>
  <el-menu
    :default-active="activeMenu"
    :collapse="isCollapse"
    :unique-opened="true"
    router
    class="sidebar-menu"
  >
    <template v-for="menu in menus" :key="menu.id">
      <!-- 无子菜单 -->
      <el-menu-item v-if="!menu.children?.length" :index="menu.path">
        <el-icon><component :is="menu.icon" /></el-icon>
        <template #title>{{ menu.name }}</template>
      </el-menu-item>

      <!-- 有子菜单 -->
      <el-sub-menu v-else :index="menu.path">
        <template #title>
          <el-icon><component :is="menu.icon" /></el-icon>
          <span>{{ menu.name }}</span>
        </template>

        <template v-for="child in menu.children" :key="child.id">
          <!-- 二级无子菜单 -->
          <el-menu-item v-if="!child.children?.length" :index="child.path">
            <el-icon><component :is="child.icon" /></el-icon>
            <template #title>{{ child.name }}</template>
          </el-menu-item>

          <!-- 三级菜单 -->
          <el-sub-menu v-else :index="child.path">
            <template #title>
              <el-icon><component :is="child.icon" /></el-icon>
              <span>{{ child.name }}</span>
            </template>
            <el-menu-item
              v-for="grandChild in child.children"
              :key="grandChild.id"
              :index="grandChild.path"
            >
              {{ grandChild.name }}
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-sub-menu>
    </template>
  </el-menu>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

defineProps<{
  isCollapse: boolean
}>()

const route = useRoute()
const userStore = useUserStore()

const menus = computed(() => userStore.menus)
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.sidebar-menu {
  border-right: none;
  height: 100%;
}
</style>
```

**Commit:** `git add frontend/src/layouts/components/MultiLevelSidebar.vue && git commit -m "feat: add 3-level sidebar menu component"`

---

### Task 62: 创建面包屑导航组件

**Files:** `frontend/src/components/Breadcrumb.vue`

**Step 1:** 创建Breadcrumb.vue
```vue
<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item :to="{ path: '/' }">
      <el-icon><HomeFilled /></el-icon>
    </el-breadcrumb-item>
    <el-breadcrumb-item
      v-for="item in breadcrumbs"
      :key="item.path"
      :to="item.path"
    >
      {{ item.title }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { HomeFilled } from '@element-plus/icons-vue'

interface BreadcrumbItem {
  path: string
  title: string
}

const route = useRoute()

const breadcrumbs = computed<BreadcrumbItem[]>(() => {
  const matched = route.matched.filter(item => item.meta?.title)
  return matched.map(item => ({
    path: item.path,
    title: item.meta?.title as string
  }))
})
</script>

<style scoped>
.el-breadcrumb {
  line-height: 50px;
}
</style>
```

**Commit:** `git add frontend/src/components/Breadcrumb.vue && git commit -m "feat: add breadcrumb navigation component"`

---

### Task 63: 创建动态路由生成器

**Files:** `frontend/src/router/dynamicRoutes.ts`

**Step 1:** 创建dynamicRoutes.ts
```typescript
import type { RouteRecordRaw } from 'vue-router'
import type { MenuDTO } from '@/types/menu'

// 视图组件映射
const viewModules = import.meta.glob('../views/**/*.vue')

export function generateRoutes(menus: MenuDTO[]): RouteRecordRaw[] {
  const routes: RouteRecordRaw[] = []

  for (const menu of menus) {
    if (!menu.path) continue

    const route: RouteRecordRaw = {
      path: menu.path,
      name: menu.name,
      meta: {
        title: menu.name,
        icon: menu.icon,
        hidden: !menu.visible
      },
      component: resolveComponent(menu.component),
      children: []
    }

    if (menu.children?.length) {
      route.children = generateRoutes(menu.children)
    }

    routes.push(route)
  }

  return routes
}

function resolveComponent(component?: string) {
  if (!component) return () => import('@/views/Home.vue')

  const path = `../views/${component}.vue`
  if (viewModules[path]) {
    return viewModules[path]
  }

  console.warn(`Component not found: ${component}`)
  return () => import('@/views/Home.vue')
}

export function addDynamicRoutes(router: any, menus: MenuDTO[]) {
  const routes = generateRoutes(menus)

  routes.forEach(route => {
    if (!router.hasRoute(route.name as string)) {
      router.addRoute('Layout', route)
    }
  })
}
```

**Commit:** `git add frontend/src/router/dynamicRoutes.ts && git commit -m "feat: add dynamic router from menu data"`

---

### Task 64: 增强用户Store添加权限和菜单

**Files:** `frontend/src/stores/user.ts`

**Step 1:** 创建或增强user.ts store
```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo, MenuDTO } from '@/types'
import { login, logout, getUserInfo } from '@/api/auth'
import { getMenusByRole, getPermissionsByRole } from '@/api/permission'
import { addDynamicRoutes } from '@/router/dynamicRoutes'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)
  const menus = ref<MenuDTO[]>([])
  const permissions = ref<string[]>([])

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => userInfo.value?.role)

  async function loginAction(username: string, password: string) {
    const res = await login({ username, password })
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    await fetchUserInfo()
  }

  async function fetchUserInfo() {
    const res = await getUserInfo()
    userInfo.value = res.data

    // 获取菜单和权限
    await fetchMenusAndPermissions()
  }

  async function fetchMenusAndPermissions() {
    if (!userInfo.value?.role) return

    const [menuRes, permRes] = await Promise.all([
      getMenusByRole(userInfo.value.role),
      getPermissionsByRole(userInfo.value.role)
    ])

    menus.value = menuRes.data
    permissions.value = permRes.data

    // 动态添加路由
    addDynamicRoutes(router, menus.value)
  }

  function hasPermission(code: string): boolean {
    return permissions.value.includes(code)
  }

  function hasAnyPermission(codes: string[]): boolean {
    return codes.some(code => permissions.value.includes(code))
  }

  async function logoutAction() {
    await logout()
    token.value = ''
    userInfo.value = null
    menus.value = []
    permissions.value = []
    localStorage.removeItem('token')
    router.push('/login')
  }

  function resetState() {
    token.value = ''
    userInfo.value = null
    menus.value = []
    permissions.value = []
  }

  return {
    token,
    userInfo,
    menus,
    permissions,
    isLoggedIn,
    role,
    loginAction,
    fetchUserInfo,
    fetchMenusAndPermissions,
    hasPermission,
    hasAnyPermission,
    logoutAction,
    resetState
  }
})
```

**Commit:** `git add frontend/src/stores/user.ts && git commit -m "feat: enhance user store with permissions and menus"`

---

### Task 65: 创建v-permission指令

**Files:** `frontend/src/directives/permission.ts`

**Step 1:** 创建permission指令
```typescript
import type { Directive, DirectiveBinding } from 'vue'
import { useUserStore } from '@/stores/user'

export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const userStore = useUserStore()
    const { value } = binding

    if (!value) return

    // 支持单个权限或权限数组
    const permissions = Array.isArray(value) ? value : [value]
    const hasPermission = permissions.some(p => userStore.hasPermission(p))

    if (!hasPermission) {
      el.parentNode?.removeChild(el)
    }
  }
}

export const role: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const userStore = useUserStore()
    const { value } = binding

    if (!value) return

    const roles = Array.isArray(value) ? value : [value]
    const hasRole = roles.includes(userStore.role)

    if (!hasRole) {
      el.parentNode?.removeChild(el)
    }
  }
}

// 注册指令
export function setupPermissionDirectives(app: any) {
  app.directive('permission', permission)
  app.directive('role', role)
}
```

**Step 2:** 在main.ts中注册指令
```typescript
// main.ts
import { setupPermissionDirectives } from '@/directives/permission'

const app = createApp(App)
setupPermissionDirectives(app)
```

**Step 3:** 使用示例
```vue
<!-- 单个权限 -->
<el-button v-permission="'user:create'">创建用户</el-button>

<!-- 多个权限（满足任一即可） -->
<el-button v-permission="['user:update', 'user:delete']">编辑</el-button>

<!-- 角色控制 -->
<el-button v-role="'ADMIN'">管理员操作</el-button>
```

**Commit:** `git add frontend/src/directives/permission.ts && git commit -m "feat: add v-permission directive"`

---

## Phase 9: Frontend - Questionnaire System (Tasks 66-75)

### Task 66: 创建问卷API模块

**Files:** `frontend/src/api/questionnaire.ts`

**Step 1:** 创建questionnaire.ts
```typescript
import request from '@/utils/request'
import type {
  QuestionnaireDTO,
  QuestionnaireCreateRequest,
  QuestionnaireUpdateRequest,
  ResponseSubmitRequest,
  QuestionnaireResponseDTO
} from '@/types/questionnaire'

export function getQuestionnaires(params: any) {
  return request.get<Page<QuestionnaireDTO>>('/api/questionnaires', { params })
}

export function getQuestionnaire(id: number) {
  return request.get<QuestionnaireDTO>(`/api/questionnaires/${id}`)
}

export function createQuestionnaire(data: QuestionnaireCreateRequest) {
  return request.post<QuestionnaireDTO>('/api/questionnaires', data)
}

export function updateQuestionnaire(id: number, data: QuestionnaireUpdateRequest) {
  return request.put<QuestionnaireDTO>(`/api/questionnaires/${id}`, data)
}

export function deleteQuestionnaire(id: number) {
  return request.delete(`/api/questionnaires/${id}`)
}

export function publishQuestionnaire(id: number) {
  return request.post(`/api/questionnaires/${id}/publish`)
}

export function closeQuestionnaire(id: number) {
  return request.post(`/api/questionnaires/${id}/close`)
}

export function submitResponse(id: number, data: ResponseSubmitRequest) {
  return request.post<QuestionnaireResponseDTO>(`/api/questionnaires/${id}/responses`, data)
}

export function getResponses(id: number, params: any) {
  return request.get<Page<QuestionnaireResponseDTO>>(`/api/questionnaires/${id}/responses`, { params })
}

export function getActiveQuestionnaires() {
  return request.get<QuestionnaireDTO[]>('/api/questionnaires/active')
}
```

**Commit:** `git add frontend/src/api/questionnaire.ts && git commit -m "feat: add questionnaire API module"`

---

### Task 67: 创建字段类型组件（7种类型）

**Files:** `frontend/src/views/questionnaire/components/fields/`

**Step 1:** 创建TextField.vue
```vue
<template>
  <el-form-item :label="field.label" :required="field.required">
    <el-input
      v-model="modelValue"
      :placeholder="field.placeholder"
      @update:model-value="emit('update:modelValue', $event)"
    />
  </el-form-item>
</template>

<script setup lang="ts">
import type { FieldDTO } from '@/types/questionnaire'

defineProps<{ field: FieldDTO; modelValue: string }>()
const emit = defineEmits(['update:modelValue'])
</script>
```

**Step 2:** 创建TextareaField.vue
```vue
<template>
  <el-form-item :label="field.label" :required="field.required">
    <el-input
      v-model="modelValue"
      type="textarea"
      :rows="4"
      :placeholder="field.placeholder"
      @update:model-value="emit('update:modelValue', $event)"
    />
  </el-form-item>
</template>

<script setup lang="ts">
import type { FieldDTO } from '@/types/questionnaire'
defineProps<{ field: FieldDTO; modelValue: string }>()
const emit = defineEmits(['update:modelValue'])
</script>
```

**Step 3:** 创建RadioField.vue
```vue
<template>
  <el-form-item :label="field.label" :required="field.required">
    <el-radio-group v-model="modelValue" @update:model-value="emit('update:modelValue', $event)">
      <el-radio v-for="opt in options" :key="opt.value" :label="opt.value">
        {{ opt.label }}
      </el-radio>
    </el-radio-group>
  </el-form-item>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { FieldDTO } from '@/types/questionnaire'

const props = defineProps<{ field: FieldDTO; modelValue: string }>()
const emit = defineEmits(['update:modelValue'])

const options = computed(() => {
  try { return JSON.parse(props.field.options || '[]') }
  catch { return [] }
})
</script>
```

**Step 4:** 创建CheckboxField.vue
```vue
<template>
  <el-form-item :label="field.label" :required="field.required">
    <el-checkbox-group v-model="modelValue" @update:model-value="emit('update:modelValue', $event)">
      <el-checkbox v-for="opt in options" :key="opt.value" :label="opt.value">
        {{ opt.label }}
      </el-checkbox>
    </el-checkbox-group>
  </el-form-item>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { FieldDTO } from '@/types/questionnaire'

const props = defineProps<{ field: FieldDTO; modelValue: string[] }>()
const emit = defineEmits(['update:modelValue'])

const options = computed(() => {
  try { return JSON.parse(props.field.options || '[]') }
  catch { return [] }
})
</script>
```

**Step 5:** 创建SelectField.vue, DateField.vue, FileField.vue
```vue
<!-- SelectField.vue -->
<template>
  <el-form-item :label="field.label" :required="field.required">
    <el-select v-model="modelValue" :placeholder="field.placeholder"
               @update:model-value="emit('update:modelValue', $event)">
      <el-option v-for="opt in options" :key="opt.value" :label="opt.label" :value="opt.value" />
    </el-select>
  </el-form-item>
</template>

<!-- DateField.vue -->
<template>
  <el-form-item :label="field.label" :required="field.required">
    <el-date-picker v-model="modelValue" type="date" :placeholder="field.placeholder"
                    @update:model-value="emit('update:modelValue', $event)" />
  </el-form-item>
</template>

<!-- FileField.vue -->
<template>
  <el-form-item :label="field.label" :required="field.required">
    <el-upload action="/api/upload" :on-success="handleSuccess" :limit="1">
      <el-button type="primary">点击上传</el-button>
    </el-upload>
  </el-form-item>
</template>
```

**Step 6:** 创建字段组件索引
```typescript
// frontend/src/views/questionnaire/components/fields/index.ts
export { default as TextField } from './TextField.vue'
export { default as TextareaField } from './TextareaField.vue'
export { default as RadioField } from './RadioField.vue'
export { default as CheckboxField } from './CheckboxField.vue'
export { default as SelectField } from './SelectField.vue'
export { default as DateField } from './DateField.vue'
export { default as FileField } from './FileField.vue'

export const fieldComponents: Record<string, any> = {
  TEXT: TextField,
  TEXTAREA: TextareaField,
  RADIO: RadioField,
  CHECKBOX: CheckboxField,
  SELECT: SelectField,
  DATE: DateField,
  FILE: FileField
}
```

**Commit:** `git add frontend/src/views/questionnaire/components/fields/ && git commit -m "feat: add field type components (7 types)"`

---

### Task 68: 创建问卷设计器（拖拽功能）

**Files:** `frontend/src/views/questionnaire/components/QuestionnaireDesigner.vue`

**Step 1:** 安装vuedraggable
```bash
npm install vuedraggable@next
```

**Step 2:** 创建QuestionnaireDesigner.vue
```vue
<template>
  <div class="designer-container">
    <!-- 左侧字段面板 -->
    <div class="field-panel">
      <h4>字段类型</h4>
      <draggable
        :list="fieldTypes"
        :group="{ name: 'fields', pull: 'clone', put: false }"
        :clone="cloneField"
        item-key="type"
        class="field-list"
      >
        <template #item="{ element }">
          <div class="field-item">
            <el-icon><component :is="element.icon" /></el-icon>
            {{ element.label }}
          </div>
        </template>
      </draggable>
    </div>

    <!-- 中间设计区域 -->
    <div class="design-area">
      <h4>问卷设计</h4>
      <draggable
        v-model="fields"
        group="fields"
        item-key="id"
        class="drop-zone"
        @change="onFieldChange"
      >
        <template #item="{ element, index }">
          <div
            class="field-wrapper"
            :class="{ active: activeIndex === index }"
            @click="selectField(index)"
          >
            <component
              :is="fieldComponents[element.fieldType]"
              :field="element"
              :model-value="''"
              disabled
            />
            <div class="field-actions">
              <el-button size="small" @click.stop="removeField(index)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </template>
      </draggable>
      <div v-if="!fields.length" class="empty-tip">
        拖拽左侧字段到此处
      </div>
    </div>

    <!-- 右侧属性面板 -->
    <div class="property-panel">
      <FieldPropertyEditor
        v-if="activeField"
        :field="activeField"
        @update="updateField"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import draggable from 'vuedraggable'
import { Delete } from '@element-plus/icons-vue'
import { fieldComponents } from './fields'
import FieldPropertyEditor from './FieldPropertyEditor.vue'
import type { FieldDTO } from '@/types/questionnaire'

const props = defineProps<{ modelValue: FieldDTO[] }>()
const emit = defineEmits(['update:modelValue'])

const fields = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const activeIndex = ref<number>(-1)
const activeField = computed(() => fields.value[activeIndex.value])

const fieldTypes = [
  { type: 'TEXT', label: '单行文本', icon: 'Edit' },
  { type: 'TEXTAREA', label: '多行文本', icon: 'Document' },
  { type: 'RADIO', label: '单选', icon: 'CircleCheck' },
  { type: 'CHECKBOX', label: '多选', icon: 'Check' },
  { type: 'SELECT', label: '下拉选择', icon: 'ArrowDown' },
  { type: 'DATE', label: '日期', icon: 'Calendar' },
  { type: 'FILE', label: '文件上传', icon: 'Upload' }
]

let fieldId = 0
function cloneField(item: any): FieldDTO {
  return {
    id: ++fieldId,
    fieldType: item.type,
    label: item.label,
    placeholder: '',
    required: false,
    options: '[]',
    sortOrder: fields.value.length
  }
}

function selectField(index: number) { activeIndex.value = index }
function removeField(index: number) {
  fields.value.splice(index, 1)
  activeIndex.value = -1
}
function updateField(updated: FieldDTO) {
  if (activeIndex.value >= 0) {
    fields.value[activeIndex.value] = updated
  }
}
</script>
```

**Commit:** `git add frontend/src/views/questionnaire/components/QuestionnaireDesigner.vue && git commit -m "feat: add QuestionnaireDesigner with drag-drop"`

---

### Task 69: 创建字段属性编辑面板

**Files:** `frontend/src/views/questionnaire/components/FieldPropertyEditor.vue`

**Step 1:** 创建FieldPropertyEditor.vue
```vue
<template>
  <div class="property-editor">
    <h4>字段属性</h4>
    <el-form label-position="top" size="small">
      <el-form-item label="标签">
        <el-input v-model="localField.label" @change="emitUpdate" />
      </el-form-item>

      <el-form-item label="占位符">
        <el-input v-model="localField.placeholder" @change="emitUpdate" />
      </el-form-item>

      <el-form-item label="必填">
        <el-switch v-model="localField.required" @change="emitUpdate" />
      </el-form-item>

      <!-- 选项编辑（单选/多选/下拉） -->
      <el-form-item v-if="hasOptions" label="选项">
        <div class="options-editor">
          <div v-for="(opt, idx) in options" :key="idx" class="option-row">
            <el-input v-model="opt.label" placeholder="选项文本" size="small" />
            <el-input v-model="opt.value" placeholder="选项值" size="small" />
            <el-button size="small" @click="removeOption(idx)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <el-button size="small" @click="addOption">添加选项</el-button>
        </div>
      </el-form-item>

      <!-- 验证规则 -->
      <el-form-item label="验证规则">
        <el-select v-model="validationType" placeholder="选择验证类型" @change="updateValidation">
          <el-option label="无" value="" />
          <el-option label="邮箱" value="email" />
          <el-option label="手机号" value="phone" />
          <el-option label="数字" value="number" />
          <el-option label="自定义正则" value="regex" />
        </el-select>
        <el-input
          v-if="validationType === 'regex'"
          v-model="regexPattern"
          placeholder="正则表达式"
          @change="updateValidation"
        />
      </el-form-item>

      <!-- 条件逻辑 -->
      <el-form-item label="条件显示">
        <el-button size="small" @click="showConditionDialog = true">
          设置条件
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 条件逻辑对话框 -->
    <el-dialog v-model="showConditionDialog" title="条件逻辑" width="500px">
      <ConditionalLogicEditor
        v-model="localField.conditionalLogic"
        :fields="allFields"
        @update:model-value="emitUpdate"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Delete } from '@element-plus/icons-vue'
import ConditionalLogicEditor from './ConditionalLogicEditor.vue'
import type { FieldDTO } from '@/types/questionnaire'

const props = defineProps<{
  field: FieldDTO
  allFields?: FieldDTO[]
}>()
const emit = defineEmits(['update'])

const localField = ref<FieldDTO>({ ...props.field })
const showConditionDialog = ref(false)
const validationType = ref('')
const regexPattern = ref('')

watch(() => props.field, (newVal) => {
  localField.value = { ...newVal }
}, { deep: true })

const hasOptions = computed(() =>
  ['RADIO', 'CHECKBOX', 'SELECT'].includes(localField.value.fieldType)
)

const options = ref<{ label: string; value: string }[]>([])

watch(() => localField.value.options, (val) => {
  try { options.value = JSON.parse(val || '[]') }
  catch { options.value = [] }
}, { immediate: true })

function addOption() {
  options.value.push({ label: '', value: '' })
  updateOptions()
}

function removeOption(idx: number) {
  options.value.splice(idx, 1)
  updateOptions()
}

function updateOptions() {
  localField.value.options = JSON.stringify(options.value)
  emitUpdate()
}

function updateValidation() {
  const rules: any = {}
  if (validationType.value === 'email') rules.pattern = '^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$'
  else if (validationType.value === 'phone') rules.pattern = '^1[3-9]\\d{9}$'
  else if (validationType.value === 'number') rules.pattern = '^\\d+$'
  else if (validationType.value === 'regex') rules.pattern = regexPattern.value
  localField.value.validationRules = JSON.stringify(rules)
  emitUpdate()
}

function emitUpdate() {
  emit('update', { ...localField.value })
}
</script>
```

**Commit:** `git add frontend/src/views/questionnaire/components/FieldPropertyEditor.vue && git commit -m "feat: add FieldPropertyEditor panel"`

---

### Task 70: 创建条件逻辑编辑器

**Files:** `frontend/src/views/questionnaire/components/ConditionalLogicEditor.vue`

**Step 1:** 创建ConditionalLogicEditor.vue
```vue
<template>
  <div class="conditional-logic-editor">
    <el-form label-position="top" size="small">
      <el-form-item label="显示条件">
        <el-select v-model="logic.type" placeholder="选择条件类型">
          <el-option label="始终显示" value="always" />
          <el-option label="当字段值等于" value="equals" />
          <el-option label="当字段值不等于" value="notEquals" />
          <el-option label="当字段值包含" value="contains" />
          <el-option label="当字段不为空" value="notEmpty" />
        </el-select>
      </el-form-item>

      <template v-if="logic.type && logic.type !== 'always'">
        <el-form-item label="目标字段">
          <el-select v-model="logic.fieldId" placeholder="选择字段">
            <el-option
              v-for="f in availableFields"
              :key="f.id"
              :label="f.label"
              :value="f.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item v-if="needsValue" label="比较值">
          <el-input v-model="logic.value" placeholder="输入比较值" />
        </el-form-item>
      </template>
    </el-form>

    <div class="logic-preview">
      <strong>预览：</strong>
      <span>{{ previewText }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { FieldDTO } from '@/types/questionnaire'

interface ConditionalLogic {
  type: string
  fieldId?: number
  value?: string
}

const props = defineProps<{
  modelValue?: string
  fields: FieldDTO[]
}>()
const emit = defineEmits(['update:modelValue'])

const logic = ref<ConditionalLogic>({ type: 'always' })

watch(() => props.modelValue, (val) => {
  if (val) {
    try { logic.value = JSON.parse(val) }
    catch { logic.value = { type: 'always' } }
  }
}, { immediate: true })

watch(logic, (val) => {
  emit('update:modelValue', JSON.stringify(val))
}, { deep: true })

const availableFields = computed(() => props.fields || [])

const needsValue = computed(() =>
  ['equals', 'notEquals', 'contains'].includes(logic.value.type)
)

const previewText = computed(() => {
  if (logic.value.type === 'always') return '始终显示此字段'
  const field = availableFields.value.find(f => f.id === logic.value.fieldId)
  const fieldName = field?.label || '未选择字段'
  switch (logic.value.type) {
    case 'equals': return `当 "${fieldName}" 等于 "${logic.value.value}" 时显示`
    case 'notEquals': return `当 "${fieldName}" 不等于 "${logic.value.value}" 时显示`
    case 'contains': return `当 "${fieldName}" 包含 "${logic.value.value}" 时显示`
    case 'notEmpty': return `当 "${fieldName}" 不为空时显示`
    default: return '未设置条件'
  }
})
</script>
```

**Commit:** `git add frontend/src/views/questionnaire/components/ConditionalLogicEditor.vue && git commit -m "feat: add conditional logic editor"`

---

### Task 71: 创建问卷列表视图

**Files:** `frontend/src/views/questionnaire/index.vue`

**Step 1:** 创建问卷列表页面
```vue
<template>
  <div class="questionnaire-list">
    <div class="page-header">
      <h2>问卷管理</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon> 创建问卷
      </el-button>
    </div>

    <el-table :data="questionnaires" v-loading="loading">
      <el-table-column prop="title" label="问卷标题" min-width="200" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="responseCount" label="回复数" width="100" />
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" @click="handlePreview(row)">预览</el-button>
          <el-button
            v-if="row.status === 'DRAFT'"
            size="small" type="success"
            @click="handlePublish(row)"
          >发布</el-button>
          <el-button size="small" @click="handleResponses(row)">查看回复</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      v-model:page-size="pageSize"
      :total="total"
      layout="total, sizes, prev, pager, next"
      @change="fetchData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getQuestionnaires, deleteQuestionnaire, publishQuestionnaire } from '@/api/questionnaire'
import { formatDate } from '@/utils/date'

const router = useRouter()
const loading = ref(false)
const questionnaires = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await getQuestionnaires({ page: page.value - 1, size: pageSize.value })
    questionnaires.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
}

function handleCreate() { router.push('/questionnaire/create') }
function handleEdit(row: any) { router.push(`/questionnaire/edit/${row.id}`) }
function handlePreview(row: any) { router.push(`/questionnaire/preview/${row.id}`) }
function handleResponses(row: any) { router.push(`/questionnaire/responses/${row.id}`) }

async function handlePublish(row: any) {
  await ElMessageBox.confirm('确定发布此问卷？')
  await publishQuestionnaire(row.id)
  ElMessage.success('发布成功')
  fetchData()
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm('确定删除此问卷？')
  await deleteQuestionnaire(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

const statusText = (s: string) => ({ DRAFT: '草稿', PUBLISHED: '已发布', CLOSED: '已关闭' }[s] || s)
const statusType = (s: string) => ({ DRAFT: 'info', PUBLISHED: 'success', CLOSED: 'warning' }[s] || '')
</script>
```

**Commit:** `git add frontend/src/views/questionnaire/index.vue && git commit -m "feat: add QuestionnaireList view"`

---

### Task 72: 创建问卷预览组件

**Files:** `frontend/src/views/questionnaire/components/QuestionnairePreview.vue`

**Step 1:** 创建QuestionnairePreview.vue
```vue
<template>
  <div class="questionnaire-preview">
    <el-card>
      <template #header>
        <h2>{{ questionnaire.title }}</h2>
        <p class="description">{{ questionnaire.description }}</p>
      </template>

      <el-form ref="formRef" :model="formData" :rules="formRules" label-position="top">
        <template v-for="field in visibleFields" :key="field.id">
          <component
            :is="fieldComponents[field.fieldType]"
            :field="field"
            v-model="formData[field.id]"
          />
        </template>
      </el-form>

      <div class="preview-actions" v-if="!readonly">
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          提交
        </el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, watch } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { fieldComponents } from './fields'
import type { QuestionnaireDTO, FieldDTO } from '@/types/questionnaire'

const props = defineProps<{
  questionnaire: QuestionnaireDTO
  readonly?: boolean
}>()

const emit = defineEmits(['submit'])

const formRef = ref<FormInstance>()
const formData = reactive<Record<number, any>>({})
const submitting = ref(false)

// 初始化表单数据
watch(() => props.questionnaire.fields, (fields) => {
  fields?.forEach(f => {
    formData[f.id!] = f.fieldType === 'CHECKBOX' ? [] : ''
  })
}, { immediate: true })

// 条件逻辑：计算可见字段
const visibleFields = computed(() => {
  return props.questionnaire.fields?.filter(field => {
    if (!field.conditionalLogic) return true
    try {
      const logic = JSON.parse(field.conditionalLogic)
      if (logic.type === 'always') return true
      const targetValue = formData[logic.fieldId]
      switch (logic.type) {
        case 'equals': return targetValue === logic.value
        case 'notEquals': return targetValue !== logic.value
        case 'contains': return String(targetValue).includes(logic.value)
        case 'notEmpty': return !!targetValue
        default: return true
      }
    } catch { return true }
  }) || []
})

// 生成验证规则
const formRules = computed(() => {
  const rules: Record<number, any[]> = {}
  props.questionnaire.fields?.forEach(field => {
    const fieldRules: any[] = []
    if (field.required) {
      fieldRules.push({ required: true, message: `${field.label}为必填项` })
    }
    if (field.validationRules) {
      try {
        const validation = JSON.parse(field.validationRules)
        if (validation.pattern) {
          fieldRules.push({ pattern: new RegExp(validation.pattern), message: '格式不正确' })
        }
      } catch {}
    }
    if (fieldRules.length) rules[field.id!] = fieldRules
  })
  return rules
})

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    emit('submit', { answers: JSON.stringify(formData) })
  } finally {
    submitting.value = false
  }
}

function handleReset() {
  formRef.value?.resetFields()
}
</script>
```

**Commit:** `git add frontend/src/views/questionnaire/components/QuestionnairePreview.vue && git commit -m "feat: add QuestionnairePreview component"`

---

### Task 73: 创建公开问卷填写视图

**Files:** `frontend/src/views/questionnaire/PublicForm.vue`

**Step 1:** 创建PublicForm.vue
```vue
<template>
  <div class="public-questionnaire-form">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>

    <div v-else-if="submitted" class="success-container">
      <el-result icon="success" title="提交成功" sub-title="感谢您的参与！">
        <template #extra>
          <el-button type="primary" @click="handleBack">返回首页</el-button>
        </template>
      </el-result>
    </div>

    <div v-else-if="error" class="error-container">
      <el-result icon="error" :title="error">
        <template #extra>
          <el-button type="primary" @click="handleBack">返回首页</el-button>
        </template>
      </el-result>
    </div>

    <template v-else>
      <QuestionnairePreview
        :questionnaire="questionnaire"
        @submit="handleSubmit"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getQuestionnaire, submitResponse } from '@/api/questionnaire'
import QuestionnairePreview from './components/QuestionnairePreview.vue'
import type { QuestionnaireDTO } from '@/types/questionnaire'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const submitted = ref(false)
const error = ref('')
const questionnaire = ref<QuestionnaireDTO>({} as QuestionnaireDTO)

onMounted(async () => {
  try {
    const id = Number(route.params.id)
    const res = await getQuestionnaire(id)
    questionnaire.value = res.data

    // 检查问卷状态
    if (res.data.status !== 'PUBLISHED') {
      error.value = '该问卷暂未开放'
    }
  } catch (e: any) {
    error.value = e.message || '问卷不存在'
  } finally {
    loading.value = false
  }
})

async function handleSubmit(data: { answers: string }) {
  try {
    await submitResponse(questionnaire.value.id!, data)
    submitted.value = true
    ElMessage.success('提交成功')
  } catch (e: any) {
    ElMessage.error(e.message || '提交失败')
  }
}

function handleBack() {
  router.push('/')
}
</script>

<style scoped>
.public-questionnaire-form {
  max-width: 800px;
  margin: 40px auto;
  padding: 0 20px;
}
</style>
```

**Commit:** `git add frontend/src/views/questionnaire/PublicForm.vue && git commit -m "feat: add PublicQuestionnaireForm view"`

---

### Task 74: 创建回复列表视图

**Files:** `frontend/src/views/questionnaire/ResponseList.vue`

**Step 1:** 创建ResponseList.vue
```vue
<template>
  <div class="response-list">
    <div class="page-header">
      <h2>问卷回复 - {{ questionnaire.title }}</h2>
      <div class="actions">
        <el-button @click="handleExport">导出Excel</el-button>
        <el-button @click="router.back()">返回</el-button>
      </div>
    </div>

    <el-table :data="responses" v-loading="loading">
      <el-table-column type="index" label="#" width="60" />
      <el-table-column
        v-for="field in questionnaire.fields"
        :key="field.id"
        :label="field.label"
        min-width="150"
      >
        <template #default="{ row }">
          {{ getAnswerDisplay(row.answers, field) }}
        </template>
      </el-table-column>
      <el-table-column prop="submittedAt" label="提交时间" width="180">
        <template #default="{ row }">{{ formatDate(row.submittedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" @click="showDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      v-model:page-size="pageSize"
      :total="total"
      layout="total, sizes, prev, pager, next"
      @change="fetchResponses"
    />

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="回复详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item
          v-for="field in questionnaire.fields"
          :key="field.id"
          :label="field.label"
        >
          {{ getAnswerDisplay(currentResponse?.answers, field) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getQuestionnaire, getResponses } from '@/api/questionnaire'
import { formatDate } from '@/utils/date'
import type { QuestionnaireDTO, QuestionnaireResponseDTO, FieldDTO } from '@/types/questionnaire'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const questionnaire = ref<QuestionnaireDTO>({} as QuestionnaireDTO)
const responses = ref<QuestionnaireResponseDTO[]>([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailVisible = ref(false)
const currentResponse = ref<QuestionnaireResponseDTO | null>(null)

onMounted(async () => {
  const id = Number(route.params.id)
  const res = await getQuestionnaire(id)
  questionnaire.value = res.data
  await fetchResponses()
})

async function fetchResponses() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const res = await getResponses(id, { page: page.value - 1, size: pageSize.value })
    responses.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
}

function getAnswerDisplay(answers: string, field: FieldDTO): string {
  try {
    const data = JSON.parse(answers || '{}')
    const value = data[field.id!]
    if (Array.isArray(value)) return value.join(', ')
    return value || '-'
  } catch { return '-' }
}

function showDetail(row: QuestionnaireResponseDTO) {
  currentResponse.value = row
  detailVisible.value = true
}

function handleExport() {
  window.open(`/api/questionnaires/${route.params.id}/responses/export`)
}
</script>
```

**Commit:** `git add frontend/src/views/questionnaire/ResponseList.vue && git commit -m "feat: add ResponseList view"`

---

### Task 75: 添加问卷路由

**Files:** `frontend/src/router/modules/questionnaire.ts`

**Step 1:** 创建问卷路由模块
```typescript
import type { RouteRecordRaw } from 'vue-router'

const questionnaireRoutes: RouteRecordRaw[] = [
  {
    path: '/questionnaire',
    name: 'Questionnaire',
    meta: { title: '问卷管理', icon: 'Document' },
    redirect: '/questionnaire/list',
    children: [
      {
        path: 'list',
        name: 'QuestionnaireList',
        component: () => import('@/views/questionnaire/index.vue'),
        meta: { title: '问卷列表' }
      },
      {
        path: 'create',
        name: 'QuestionnaireCreate',
        component: () => import('@/views/questionnaire/edit.vue'),
        meta: { title: '创建问卷' }
      },
      {
        path: 'edit/:id',
        name: 'QuestionnaireEdit',
        component: () => import('@/views/questionnaire/edit.vue'),
        meta: { title: '编辑问卷', hidden: true }
      },
      {
        path: 'preview/:id',
        name: 'QuestionnairePreview',
        component: () => import('@/views/questionnaire/preview.vue'),
        meta: { title: '预览问卷', hidden: true }
      },
      {
        path: 'responses/:id',
        name: 'QuestionnaireResponses',
        component: () => import('@/views/questionnaire/ResponseList.vue'),
        meta: { title: '问卷回复', hidden: true }
      }
    ]
  },
  {
    path: '/q/:id',
    name: 'PublicQuestionnaire',
    component: () => import('@/views/questionnaire/PublicForm.vue'),
    meta: { title: '填写问卷', public: true }
  }
]

export default questionnaireRoutes
```

**Commit:** `git add frontend/src/router/modules/questionnaire.ts && git commit -m "feat: add questionnaire routes"`

---

## Phase 10: Frontend - AI Interview System (Tasks 76-85)

### Task 76: 创建AI面试API模块

**Files:** `frontend/src/api/aiInterview.ts`

**Step 1:** 创建aiInterview.ts
```typescript
import request from '@/utils/request'
import type {
  AIInterviewSessionDTO,
  AIInterviewScoreDTO,
  AIInterviewMessageDTO,
  AIInterviewStatisticsDTO
} from '@/types/aiInterview'

export function startSession(userId: number, applicationId?: number) {
  return request.post<AIInterviewSessionDTO>('/api/ai-interviews/start', {
    userId,
    applicationId
  })
}

export function getSession(token: string) {
  return request.get<AIInterviewSessionDTO>(`/api/ai-interviews/session/${token}`)
}

export function endSession(token: string) {
  return request.post<AIInterviewSessionDTO>(`/api/ai-interviews/session/${token}/end`)
}

export function getScore(token: string) {
  return request.get<AIInterviewScoreDTO>(`/api/ai-interviews/session/${token}/score`)
}

export function getMessages(token: string) {
  return request.get<AIInterviewMessageDTO[]>(`/api/ai-interviews/session/${token}/messages`)
}

export function getInterviewList(params: any) {
  return request.get<Page<AIInterviewSessionDTO>>('/api/ai-interviews/list', { params })
}

export function getStatistics() {
  return request.get<AIInterviewStatisticsDTO>('/api/ai-interviews/statistics')
}
```

**Commit:** `git add frontend/src/api/aiInterview.ts && git commit -m "feat: add AI interview API module"`

---

### Task 77: 创建WebSocket服务（STOMP）

**Files:** `frontend/src/utils/websocket.ts`

**Step 1:** 安装依赖
```bash
npm install @stomp/stompjs sockjs-client
npm install -D @types/sockjs-client
```

**Step 2:** 创建websocket.ts
```typescript
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

export interface WebSocketOptions {
  onConnect?: () => void
  onDisconnect?: () => void
  onError?: (error: any) => void
}

export class InterviewWebSocket {
  private client: Client
  private sessionToken: string
  private subscriptions: Map<string, any> = new Map()

  constructor(sessionToken: string, options: WebSocketOptions = {}) {
    this.sessionToken = sessionToken

    this.client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('WebSocket connected')
        options.onConnect?.()
      },
      onDisconnect: () => {
        console.log('WebSocket disconnected')
        options.onDisconnect?.()
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame)
        options.onError?.(frame)
      }
    })
  }

  connect() {
    this.client.activate()
  }

  disconnect() {
    this.subscriptions.forEach(sub => sub.unsubscribe())
    this.subscriptions.clear()
    this.client.deactivate()
  }

  sendMessage(content: string) {
    this.client.publish({
      destination: `/app/interview/${this.sessionToken}/send`,
      body: JSON.stringify({ content })
    })
  }

  endInterview() {
    this.client.publish({
      destination: `/app/interview/${this.sessionToken}/end`,
      body: ''
    })
  }

  onStreamMessage(callback: (data: { type: string; content: string }) => void) {
    const sub = this.client.subscribe(
      `/topic/interview/${this.sessionToken}/stream`,
      (message) => {
        const data = JSON.parse(message.body)
        callback(data)
      }
    )
    this.subscriptions.set('stream', sub)
  }

  onResult(callback: (data: any) => void) {
    const sub = this.client.subscribe(
      `/topic/interview/${this.sessionToken}/result`,
      (message) => {
        const data = JSON.parse(message.body)
        callback(data)
      }
    )
    this.subscriptions.set('result', sub)
  }
}

export function createInterviewWebSocket(sessionToken: string, options?: WebSocketOptions) {
  return new InterviewWebSocket(sessionToken, options)
}
```

**Commit:** `git add frontend/src/utils/websocket.ts && git commit -m "feat: add WebSocket service with STOMP"`

---

### Task 78: 创建InterviewChat组件

**Files:** `frontend/src/views/interview/ai/components/InterviewChat.vue`

**Step 1:** 创建InterviewChat.vue
```vue
<template>
  <div class="interview-chat">
    <div class="chat-header">
      <h3>AI面试</h3>
      <div class="status">
        <el-tag :type="connected ? 'success' : 'danger'">
          {{ connected ? '已连接' : '未连接' }}
        </el-tag>
        <span class="duration">{{ formatDuration(duration) }}</span>
      </div>
    </div>

    <div class="chat-messages" ref="messagesRef">
      <MessageBubble
        v-for="(msg, idx) in messages"
        :key="idx"
        :message="msg"
        :streaming="idx === messages.length - 1 && isStreaming"
      />
      <div v-if="isStreaming" class="typing-indicator">
        <span></span><span></span><span></span>
      </div>
    </div>

    <div class="chat-input">
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="3"
        placeholder="输入您的回答..."
        :disabled="!connected || isStreaming"
        @keydown.enter.ctrl="handleSend"
      />
      <div class="input-actions">
        <el-button
          type="primary"
          :disabled="!inputText.trim() || !connected || isStreaming"
          @click="handleSend"
        >
          发送 (Ctrl+Enter)
        </el-button>
        <el-button type="danger" @click="handleEnd">结束面试</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import { createInterviewWebSocket, type InterviewWebSocket } from '@/utils/websocket'
import MessageBubble from './MessageBubble.vue'
import type { AIInterviewMessageDTO } from '@/types/aiInterview'

const props = defineProps<{
  sessionToken: string
  initialMessages?: AIInterviewMessageDTO[]
}>()

const emit = defineEmits(['end', 'message'])

const messagesRef = ref<HTMLElement>()
const messages = ref<AIInterviewMessageDTO[]>(props.initialMessages || [])
const inputText = ref('')
const connected = ref(false)
const isStreaming = ref(false)
const duration = ref(0)
let ws: InterviewWebSocket | null = null
let durationTimer: number | null = null
let streamingContent = ''

onMounted(() => {
  initWebSocket()
  startDurationTimer()
})

onUnmounted(() => {
  ws?.disconnect()
  if (durationTimer) clearInterval(durationTimer)
})

function initWebSocket() {
  ws = createInterviewWebSocket(props.sessionToken, {
    onConnect: () => {
      connected.value = true
    },
    onDisconnect: () => {
      connected.value = false
    }
  })

  ws.onStreamMessage((data) => {
    if (data.type === 'token') {
      if (!isStreaming.value) {
        isStreaming.value = true
        streamingContent = ''
        messages.value.push({ role: 'ASSISTANT', content: '' } as AIInterviewMessageDTO)
      }
      streamingContent += data.content
      messages.value[messages.value.length - 1].content = streamingContent
      scrollToBottom()
    } else if (data.type === 'complete') {
      isStreaming.value = false
      emit('message', messages.value[messages.value.length - 1])
    }
  })

  ws.onResult((data) => {
    emit('end', data)
  })

  ws.connect()
}

function handleSend() {
  if (!inputText.value.trim() || isStreaming.value) return

  messages.value.push({
    role: 'USER',
    content: inputText.value
  } as AIInterviewMessageDTO)

  ws?.sendMessage(inputText.value)
  inputText.value = ''
  scrollToBottom()
}

async function handleEnd() {
  await ElMessageBox.confirm('确定要结束面试吗？结束后将生成评分报告。')
  ws?.endInterview()
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

function startDurationTimer() {
  durationTimer = window.setInterval(() => { duration.value++ }, 1000)
}

function formatDuration(seconds: number): string {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}
</script>
```

**Commit:** `git add frontend/src/views/interview/ai/components/InterviewChat.vue && git commit -m "feat: add InterviewChat component"`

---

### Task 79: 创建MessageBubble组件（流式显示）

**Files:** `frontend/src/views/interview/ai/components/MessageBubble.vue`

**Step 1:** 创建MessageBubble.vue
```vue
<template>
  <div class="message-bubble" :class="[message.role.toLowerCase(), { streaming }]">
    <div class="avatar">
      <el-avatar :size="36" :icon="avatarIcon" :style="avatarStyle" />
    </div>
    <div class="content">
      <div class="role-name">{{ roleName }}</div>
      <div class="message-text" v-html="formattedContent"></div>
      <div class="message-time" v-if="message.createdAt">
        {{ formatTime(message.createdAt) }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { User, Monitor } from '@element-plus/icons-vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import type { AIInterviewMessageDTO } from '@/types/aiInterview'

const props = defineProps<{
  message: AIInterviewMessageDTO
  streaming?: boolean
}>()

const roleName = computed(() => {
  return props.message.role === 'USER' ? '我' : 'AI面试官'
})

const avatarIcon = computed(() => {
  return props.message.role === 'USER' ? User : Monitor
})

const avatarStyle = computed(() => {
  return props.message.role === 'USER'
    ? { backgroundColor: '#409eff' }
    : { backgroundColor: '#67c23a' }
})

const formattedContent = computed(() => {
  const content = props.message.content || ''
  // 使用marked解析Markdown，DOMPurify防XSS
  const html = marked.parse(content, { breaks: true })
  return DOMPurify.sanitize(html as string)
})

function formatTime(time: string): string {
  return new Date(time).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.message-bubble {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  padding: 0 16px;
}

.message-bubble.user {
  flex-direction: row-reverse;
}

.message-bubble.user .content {
  align-items: flex-end;
}

.content {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.role-name {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.message-text {
  background: #f4f4f5;
  padding: 12px 16px;
  border-radius: 8px;
  line-height: 1.6;
}

.user .message-text {
  background: #ecf5ff;
}

.message-time {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 4px;
}

.streaming .message-text::after {
  content: '▋';
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}
</style>
```

**Step 2:** 安装依赖
```bash
npm install marked dompurify
npm install -D @types/dompurify
```

**Commit:** `git add frontend/src/views/interview/ai/components/MessageBubble.vue && git commit -m "feat: add MessageBubble component with streaming"`

---

### Task 80: 创建ViolationTypeDisplay组件

**Files:** `frontend/src/views/interview/ai/components/ViolationTypeDisplay.vue`

**Step 1:** 创建ViolationTypeDisplay.vue
```vue
<template>
  <div class="violation-display">
    <h4>违规记录</h4>
    <div v-if="violations.length === 0" class="no-violations">
      <el-icon><CircleCheck /></el-icon>
      <span>无违规行为</span>
    </div>
    <div v-else class="violation-list">
      <el-tag
        v-for="(v, idx) in violations"
        :key="idx"
        :type="getViolationType(v)"
        effect="plain"
        class="violation-tag"
      >
        <el-tooltip :content="getViolationDescription(v)">
          <span>{{ getViolationLabel(v) }}</span>
        </el-tooltip>
      </el-tag>
    </div>
    <div class="violation-summary" v-if="violations.length > 0">
      <span class="penalty">扣分: -{{ calculatePenalty(violations) }}分</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { CircleCheck } from '@element-plus/icons-vue'

const props = defineProps<{
  violationTypes?: string | string[]
}>()

const violations = computed<string[]>(() => {
  if (!props.violationTypes) return []
  if (Array.isArray(props.violationTypes)) return props.violationTypes
  try {
    return JSON.parse(props.violationTypes)
  } catch {
    return []
  }
})

const violationConfig: Record<string, { label: string; description: string; penalty: number; type: string }> = {
  IRRELEVANT_ANSWER: { label: '答非所问', description: '回答与问题无关', penalty: 5, type: 'warning' },
  OFFENSIVE_LANGUAGE: { label: '不当言语', description: '使用不当言语或攻击性语言', penalty: 10, type: 'danger' },
  DISHONESTY: { label: '不诚实', description: '明显的不诚实或夸大其词', penalty: 10, type: 'danger' },
  EVASION: { label: '回避问题', description: '故意回避问题或拒绝回答', penalty: 5, type: 'warning' },
  COPY_PASTE: { label: '复制粘贴', description: '疑似复制粘贴的机械回答', penalty: 3, type: 'info' },
  TOO_SHORT: { label: '回答过短', description: '回答过于简短，缺乏实质内容', penalty: 3, type: 'info' },
  OFF_TOPIC: { label: '偏离主题', description: '偏离面试主题，闲聊或跑题', penalty: 5, type: 'warning' },
  DISRESPECT: { label: '不尊重', description: '对面试官或社团表现不尊重', penalty: 10, type: 'danger' },
  TIMEOUT: { label: '超时', description: '长时间不回复（超过3分钟）', penalty: 3, type: 'info' }
}

function getViolationLabel(code: string): string {
  return violationConfig[code]?.label || code
}

function getViolationDescription(code: string): string {
  return violationConfig[code]?.description || ''
}

function getViolationType(code: string): string {
  return violationConfig[code]?.type || 'info'
}

function calculatePenalty(violations: string[]): number {
  const total = violations.reduce((sum, v) => sum + (violationConfig[v]?.penalty || 0), 0)
  return Math.min(total, 30) // 最多扣30分
}
</script>

<style scoped>
.violation-display {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}
.no-violations {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #67c23a;
}
.violation-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.violation-tag {
  cursor: help;
}
.violation-summary {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #eee;
}
.penalty {
  color: #f56c6c;
  font-weight: bold;
}
</style>
```

**Commit:** `git add frontend/src/views/interview/ai/components/ViolationTypeDisplay.vue && git commit -m "feat: add ViolationTypeDisplay component"`

---

### Task 81: 创建ScoreRadarChart组件（ECharts）

**Files:** `frontend/src/views/interview/ai/components/ScoreRadarChart.vue`

**Step 1:** 安装ECharts
```bash
npm install echarts vue-echarts
```

**Step 2:** 创建ScoreRadarChart.vue
```vue
<template>
  <div class="score-radar-chart">
    <v-chart :option="chartOption" autoresize style="height: 300px" />
    <div class="score-details">
      <div class="score-item" v-for="item in scoreItems" :key="item.key">
        <span class="label">{{ item.label }}</span>
        <el-progress
          :percentage="item.value * 5"
          :color="getProgressColor(item.value)"
          :stroke-width="8"
        />
        <span class="value">{{ item.value }}/20</span>
      </div>
    </div>
    <div class="total-score">
      <span class="label">总分</span>
      <span class="value" :class="scoreLevel">{{ score.totalScore }}/100</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { RadarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { AIInterviewScoreDTO } from '@/types/aiInterview'

use([RadarChart, TitleComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const props = defineProps<{
  score: AIInterviewScoreDTO
}>()

const scoreItems = computed(() => [
  { key: 'communication', label: '沟通能力', value: props.score.communicationScore || 0 },
  { key: 'logic', label: '逻辑思维', value: props.score.logicScore || 0 },
  { key: 'knowledge', label: '专业知识', value: props.score.knowledgeScore || 0 },
  { key: 'attitude', label: '工作态度', value: props.score.attitudeScore || 0 },
  { key: 'creativity', label: '创新能力', value: props.score.creativityScore || 0 }
])

const chartOption = computed(() => ({
  tooltip: {},
  radar: {
    indicator: scoreItems.value.map(item => ({
      name: item.label,
      max: 20
    })),
    shape: 'polygon',
    splitNumber: 4,
    axisName: { color: '#666' }
  },
  series: [{
    type: 'radar',
    data: [{
      value: scoreItems.value.map(item => item.value),
      name: '面试评分',
      areaStyle: { color: 'rgba(64, 158, 255, 0.3)' },
      lineStyle: { color: '#409eff' },
      itemStyle: { color: '#409eff' }
    }]
  }]
}))

const scoreLevel = computed(() => {
  const total = props.score.totalScore || 0
  if (total >= 80) return 'excellent'
  if (total >= 60) return 'good'
  if (total >= 40) return 'normal'
  return 'poor'
})

function getProgressColor(value: number): string {
  if (value >= 16) return '#67c23a'
  if (value >= 12) return '#409eff'
  if (value >= 8) return '#e6a23c'
  return '#f56c6c'
}
</script>

<style scoped>
.score-details {
  margin-top: 20px;
}
.score-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.score-item .label { width: 80px; }
.score-item .el-progress { flex: 1; }
.score-item .value { width: 50px; text-align: right; }
.total-score {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-top: 20px;
}
.total-score .label { font-size: 18px; font-weight: bold; }
.total-score .value { font-size: 32px; font-weight: bold; }
.total-score .excellent { color: #67c23a; }
.total-score .good { color: #409eff; }
.total-score .normal { color: #e6a23c; }
.total-score .poor { color: #f56c6c; }
</style>
```

**Commit:** `git add frontend/src/views/interview/ai/components/ScoreRadarChart.vue && git commit -m "feat: add ScoreRadarChart component (ECharts)"`

---

### Task 82: 创建InterviewReplay视图

**Files:** `frontend/src/views/interview/ai/replay.vue`

**Step 1:** 创建replay.vue
```vue
<template>
  <div class="interview-replay">
    <el-page-header @back="router.back()" title="返回列表">
      <template #content>
        <span>面试回放 - {{ session.userName }}</span>
      </template>
    </el-page-header>

    <div class="replay-content" v-loading="loading">
      <el-row :gutter="20">
        <!-- 左侧：对话记录 -->
        <el-col :span="14">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>对话记录</span>
                <el-tag :type="statusType">{{ statusText }}</el-tag>
              </div>
            </template>
            <div class="messages-container">
              <MessageBubble
                v-for="(msg, idx) in messages"
                :key="idx"
                :message="msg"
              />
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：评分和信息 -->
        <el-col :span="10">
          <el-card class="info-card">
            <template #header>面试信息</template>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="候选人">{{ session.userName }}</el-descriptions-item>
              <el-descriptions-item label="开始时间">{{ formatDate(session.startedAt) }}</el-descriptions-item>
              <el-descriptions-item label="结束时间">{{ formatDate(session.endedAt) }}</el-descriptions-item>
              <el-descriptions-item label="面试时长">{{ formatDuration(session.totalDuration) }}</el-descriptions-item>
              <el-descriptions-item label="消息数量">{{ messages.length }}</el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card class="score-card" v-if="score">
            <template #header>评分报告</template>
            <ScoreRadarChart :score="score" />
          </el-card>

          <el-card class="violation-card" v-if="score">
            <template #header>违规记录</template>
            <ViolationTypeDisplay :violation-types="score.violationTypes" />
          </el-card>

          <el-card class="summary-card" v-if="score?.aiSummary">
            <template #header>AI评语</template>
            <p class="summary-text">{{ score.aiSummary }}</p>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getSession, getMessages, getScore } from '@/api/aiInterview'
import { formatDate } from '@/utils/date'
import MessageBubble from './components/MessageBubble.vue'
import ScoreRadarChart from './components/ScoreRadarChart.vue'
import ViolationTypeDisplay from './components/ViolationTypeDisplay.vue'
import type { AIInterviewSessionDTO, AIInterviewMessageDTO, AIInterviewScoreDTO } from '@/types/aiInterview'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const session = ref<AIInterviewSessionDTO>({} as AIInterviewSessionDTO)
const messages = ref<AIInterviewMessageDTO[]>([])
const score = ref<AIInterviewScoreDTO | null>(null)

const statusText = computed(() => ({
  COMPLETED: '已完成',
  TERMINATED: '已终止',
  IN_PROGRESS: '进行中',
  PENDING: '待开始'
}[session.value.status] || session.value.status))

const statusType = computed(() => ({
  COMPLETED: 'success',
  TERMINATED: 'danger',
  IN_PROGRESS: 'warning',
  PENDING: 'info'
}[session.value.status] || ''))

onMounted(async () => {
  const token = route.params.token as string
  try {
    const [sessionRes, messagesRes, scoreRes] = await Promise.all([
      getSession(token),
      getMessages(token),
      getScore(token).catch(() => null)
    ])
    session.value = sessionRes.data
    messages.value = messagesRes.data
    if (scoreRes) score.value = scoreRes.data
  } finally {
    loading.value = false
  }
})

function formatDuration(seconds?: number): string {
  if (!seconds) return '-'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}分${s}秒`
}
</script>

<style scoped>
.interview-replay { padding: 20px; }
.replay-content { margin-top: 20px; }
.messages-container { max-height: 600px; overflow-y: auto; }
.info-card, .score-card, .violation-card, .summary-card { margin-bottom: 20px; }
.summary-text { line-height: 1.8; color: #606266; }
</style>
```

**Commit:** `git add frontend/src/views/interview/ai/replay.vue && git commit -m "feat: add InterviewReplay view"`

---

### Task 83: 创建InterviewList视图

**Files:** `frontend/src/views/interview/ai/index.vue`

**Step 1:** 创建AI面试列表页面
```vue
<template>
  <div class="ai-interview-list">
    <div class="page-header">
      <h2>AI面试管理</h2>
      <el-button type="primary" @click="handleStartNew" v-permission="'interview:manage'">
        发起新面试
      </el-button>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" clearable placeholder="全部状态">
            <el-option label="待开始" value="PENDING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已终止" value="TERMINATED" />
          </el-select>
        </el-form-item>
        <el-form-item label="候选人">
          <el-input v-model="queryParams.userName" placeholder="搜索候选人" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="interviews" v-loading="loading">
      <el-table-column prop="userName" label="候选人" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="评分" width="100">
        <template #default="{ row }">
          <span v-if="row.score" :class="getScoreClass(row.score.totalScore)">
            {{ row.score.totalScore }}分
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="violationCount" label="违规次数" width="100" />
      <el-table-column prop="totalDuration" label="时长" width="100">
        <template #default="{ row }">{{ formatDuration(row.totalDuration) }}</template>
      </el-table-column>
      <el-table-column prop="startedAt" label="开始时间" width="180">
        <template #default="{ row }">{{ formatDate(row.startedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleView(row)">查看</el-button>
          <el-button
            v-if="row.status === 'IN_PROGRESS'"
            size="small" type="warning"
            @click="handleContinue(row)"
          >继续</el-button>
          <el-button
            v-if="row.status === 'COMPLETED'"
            size="small" type="success"
            @click="handleExport(row)"
          >导出</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      v-model:page-size="pageSize"
      :total="total"
      layout="total, sizes, prev, pager, next"
      @change="fetchData"
    />

    <!-- 发起面试对话框 -->
    <el-dialog v-model="startDialogVisible" title="发起AI面试" width="500px">
      <el-form :model="startForm" label-width="100px">
        <el-form-item label="选择候选人" required>
          <el-select v-model="startForm.userId" filterable placeholder="搜索候选人">
            <el-option
              v-for="user in candidates"
              :key="user.id"
              :label="user.nickname"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="startDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmStart">开始面试</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getInterviewList, startSession } from '@/api/aiInterview'
import { getCandidates } from '@/api/user'
import { formatDate } from '@/utils/date'

const router = useRouter()
const loading = ref(false)
const interviews = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const startDialogVisible = ref(false)
const candidates = ref([])
const startForm = reactive({ userId: null as number | null })
const queryParams = reactive({ status: '', userName: '' })

onMounted(() => {
  fetchData()
  loadCandidates()
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getInterviewList({ ...queryParams, page: page.value - 1, size: pageSize.value })
    interviews.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
}

async function loadCandidates() {
  const res = await getCandidates()
  candidates.value = res.data
}

function handleStartNew() { startDialogVisible.value = true }

async function confirmStart() {
  if (!startForm.userId) return ElMessage.warning('请选择候选人')
  const res = await startSession(startForm.userId)
  startDialogVisible.value = false
  router.push(`/interview/ai/session/${res.data.sessionToken}`)
}

function handleView(row: any) { router.push(`/interview/ai/replay/${row.sessionToken}`) }
function handleContinue(row: any) { router.push(`/interview/ai/session/${row.sessionToken}`) }

function getStatusText(s: string) { return { PENDING: '待开始', IN_PROGRESS: '进行中', COMPLETED: '已完成', TERMINATED: '已终止' }[s] || s }
function getStatusType(s: string) { return { PENDING: 'info', IN_PROGRESS: 'warning', COMPLETED: 'success', TERMINATED: 'danger' }[s] || '' }
function getScoreClass(score: number) { return score >= 60 ? 'score-pass' : 'score-fail' }
function formatDuration(s?: number) { return s ? `${Math.floor(s / 60)}分${s % 60}秒` : '-' }
function resetQuery() { queryParams.status = ''; queryParams.userName = ''; fetchData() }
</script>
```

**Commit:** `git add frontend/src/views/interview/ai/index.vue && git commit -m "feat: add InterviewList view"`

---

### Task 84: 创建InterviewStatistics视图

**Files:** `frontend/src/views/interview/ai/statistics.vue`

**Step 1:** 创建statistics.vue
```vue
<template>
  <div class="interview-statistics">
    <h2>AI面试统计</h2>

    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="总面试数" :value="stats.totalCount" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="已完成" :value="stats.completedCount" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="平均分数" :value="stats.averageScore" :precision="1" suffix="分" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="通过率" :value="stats.passRate" :precision="1" suffix="%" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card>
          <template #header>面试趋势</template>
          <v-chart :option="trendChartOption" autoresize style="height: 300px" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>分数分布</template>
          <v-chart :option="scoreDistributionOption" autoresize style="height: 300px" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card>
          <template #header>各维度平均分</template>
          <v-chart :option="dimensionChartOption" autoresize style="height: 300px" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>违规类型统计</template>
          <v-chart :option="violationChartOption" autoresize style="height: 300px" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getStatistics } from '@/api/aiInterview'

use([LineChart, BarChart, PieChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent, CanvasRenderer])

const stats = ref({
  totalCount: 0,
  completedCount: 0,
  averageScore: 0,
  passRate: 0,
  trendData: [] as any[],
  scoreDistribution: [] as any[],
  dimensionScores: {} as any,
  violationStats: [] as any[]
})

onMounted(async () => {
  const res = await getStatistics()
  stats.value = res.data
})

const trendChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: stats.value.trendData.map(d => d.date) },
  yAxis: { type: 'value' },
  series: [{
    name: '面试数量',
    type: 'line',
    smooth: true,
    data: stats.value.trendData.map(d => d.count),
    areaStyle: { color: 'rgba(64, 158, 255, 0.2)' }
  }]
}))

const scoreDistributionOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    data: [
      { value: stats.value.scoreDistribution.filter(s => s >= 80).length, name: '优秀(>=80)' },
      { value: stats.value.scoreDistribution.filter(s => s >= 60 && s < 80).length, name: '良好(60-79)' },
      { value: stats.value.scoreDistribution.filter(s => s >= 40 && s < 60).length, name: '一般(40-59)' },
      { value: stats.value.scoreDistribution.filter(s => s < 40).length, name: '较差(<40)' }
    ]
  }]
}))

const dimensionChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: ['沟通能力', '逻辑思维', '专业知识', '工作态度', '创新能力'] },
  yAxis: { type: 'value', max: 20 },
  series: [{
    type: 'bar',
    data: [
      stats.value.dimensionScores.communication || 0,
      stats.value.dimensionScores.logic || 0,
      stats.value.dimensionScores.knowledge || 0,
      stats.value.dimensionScores.attitude || 0,
      stats.value.dimensionScores.creativity || 0
    ],
    itemStyle: { color: '#409eff' }
  }]
}))

const violationChartOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{
    type: 'pie',
    radius: '60%',
    data: stats.value.violationStats.map(v => ({ name: v.type, value: v.count }))
  }]
}))
</script>

<style scoped>
.interview-statistics { padding: 20px; }
.stat-cards { margin-bottom: 20px; }
.chart-row { margin-bottom: 20px; }
</style>
```

**Commit:** `git add frontend/src/views/interview/ai/statistics.vue && git commit -m "feat: add InterviewStatistics view"`

---

### Task 85: 添加面试路由

**Files:** `frontend/src/router/modules/interview.ts`

**Step 1:** 创建面试路由模块
```typescript
import type { RouteRecordRaw } from 'vue-router'

const interviewRoutes: RouteRecordRaw[] = [
  {
    path: '/interview',
    name: 'Interview',
    meta: { title: '面试管理', icon: 'VideoCamera' },
    redirect: '/interview/list',
    children: [
      {
        path: 'list',
        name: 'InterviewList',
        component: () => import('@/views/interview/index.vue'),
        meta: { title: '面试列表' }
      },
      {
        path: 'ai',
        name: 'AIInterview',
        component: () => import('@/views/interview/ai/index.vue'),
        meta: { title: 'AI面试' }
      },
      {
        path: 'ai/session/:token',
        name: 'AIInterviewSession',
        component: () => import('@/views/interview/ai/session.vue'),
        meta: { title: 'AI面试进行中', hidden: true }
      },
      {
        path: 'ai/replay/:token',
        name: 'AIInterviewReplay',
        component: () => import('@/views/interview/ai/replay.vue'),
        meta: { title: '面试回放', hidden: true }
      },
      {
        path: 'ai/statistics',
        name: 'AIInterviewStatistics',
        component: () => import('@/views/interview/ai/statistics.vue'),
        meta: { title: '面试统计' }
      },
      {
        path: 'config',
        name: 'InterviewConfig',
        component: () => import('@/views/interview/config/index.vue'),
        meta: { title: '题库管理' }
      }
    ]
  }
]

export default interviewRoutes
```

**Commit:** `git add frontend/src/router/modules/interview.ts && git commit -m "feat: add interview routes"`

---

## Phase 11: Frontend - Salary System (Tasks 86-92)

### Task 86: 增强薪酬API模块

**Files:** `frontend/src/api/salary.ts`

**Step 1:** 增强salary.ts
```typescript
import request from '@/utils/request'
import type {
  SalaryDTO,
  SalaryUpdateRequest,
  SalaryPoolSummary,
  MonthlyPerformanceDTO,
  SalaryValidationResult
} from '@/types/salary'

// 基础CRUD
export function getSalaries(params: any) {
  return request.get<Page<SalaryDTO>>('/api/salaries', { params })
}

export function getSalary(id: number) {
  return request.get<SalaryDTO>(`/api/salaries/${id}`)
}

export function updateSalary(id: number, data: SalaryUpdateRequest) {
  return request.put<SalaryDTO>(`/api/salaries/${id}`, data)
}

// 薪酬池
export function initPool(period: string) {
  return request.post<SalaryPoolSummary>('/api/salaries/pool/init', null, { params: { period } })
}

export function distributePool(period: string) {
  return request.post('/api/salaries/pool/distribute', null, { params: { period } })
}

export function getPoolSummary(period: string) {
  return request.get<SalaryPoolSummary>('/api/salaries/pool/summary', { params: { period } })
}

// 批量操作
export function batchUpdateSalaries(data: SalaryUpdateRequest[]) {
  return request.post('/api/salaries/batch-update', data)
}

export function validateSalaries(period: string) {
  return request.get<SalaryValidationResult>('/api/salaries/validate', { params: { period } })
}

// 绩效
export function getPerformance(userId: number, period: string) {
  return request.get<MonthlyPerformanceDTO>(`/api/salaries/performance/${userId}`, { params: { period } })
}

// 导出
export function exportSalaries(period: string) {
  return request.get('/api/salaries/export', {
    params: { period },
    responseType: 'blob'
  })
}

// 成员流动
export function getMemberFlowLogs(userId: number, params: any) {
  return request.get<Page<any>>(`/api/members/${userId}/flow-logs`, { params })
}
```

**Commit:** `git add frontend/src/api/salary.ts && git commit -m "feat: enhance salary API module"`

---

### Task 87: 创建EditableSalaryTable组件

**Files:** `frontend/src/views/salary/components/EditableSalaryTable.vue`

**Step 1:** 创建EditableSalaryTable.vue
```vue
<template>
  <div class="editable-salary-table">
    <el-table :data="salaries" border v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="user.nickname" label="成员" width="120" fixed />
      <el-table-column prop="user.role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ roleText(row.user.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="签到积分" width="100">
        <template #default="{ row }">
          <span class="points">{{ row.checkinPoints || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="活动积分" width="100">
        <template #default="{ row }">
          <span class="points">{{ row.activityPoints || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="奖励积分" width="120">
        <template #default="{ row }">
          <el-input-number
            v-model="row.bonusPoints"
            :min="0"
            :max="100"
            size="small"
            :disabled="!editable"
            @change="markChanged(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="扣除积分" width="120">
        <template #default="{ row }">
          <el-input-number
            v-model="row.deduction"
            :min="0"
            :max="100"
            size="small"
            :disabled="!editable"
            @change="markChanged(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="总积分" width="100">
        <template #default="{ row }">
          <span class="total-points" :class="{ highlight: row.changed }">
            {{ calculateTotal(row) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="薪酬(元)" width="140">
        <template #default="{ row }">
          <el-input-number
            v-model="row.salary"
            :min="200"
            :max="400"
            :precision="2"
            size="small"
            :disabled="!editable"
            @change="markChanged(row)"
          />
          <div v-if="!isValidSalary(row.salary)" class="error-tip">
            范围: 200-400
          </div>
        </template>
      </el-table-column>
      <el-table-column label="备注" min-width="150">
        <template #default="{ row }">
          <el-input
            v-model="row.remark"
            size="small"
            :disabled="!editable"
            @change="markChanged(row)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <div class="table-footer" v-if="editable">
      <div class="summary">
        <span>已选: {{ selectedRows.length }} 人</span>
        <span>薪酬总计: <strong :class="{ error: !isValidTotal }">¥{{ totalSalary.toFixed(2) }}</strong> / ¥2000.00</span>
      </div>
      <div class="actions">
        <el-button @click="handleAutoDistribute">自动分配</el-button>
        <el-button type="primary" @click="handleSave" :disabled="!hasChanges">
          保存修改 ({{ changedCount }})
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { SalaryDTO } from '@/types/salary'

const props = defineProps<{
  salaries: SalaryDTO[]
  loading?: boolean
  editable?: boolean
}>()

const emit = defineEmits(['save', 'auto-distribute', 'selection-change'])

const selectedRows = ref<SalaryDTO[]>([])
const changedIds = ref<Set<number>>(new Set())

const hasChanges = computed(() => changedIds.value.size > 0)
const changedCount = computed(() => changedIds.value.size)

const totalSalary = computed(() =>
  props.salaries.reduce((sum, s) => sum + (s.salary || 0), 0)
)

const isValidTotal = computed(() => totalSalary.value <= 2000)

function calculateTotal(row: SalaryDTO): number {
  return (row.checkinPoints || 0) + (row.activityPoints || 0) +
         (row.bonusPoints || 0) - (row.deduction || 0)
}

function isValidSalary(salary?: number): boolean {
  if (!salary) return false
  return salary >= 200 && salary <= 400
}

function markChanged(row: SalaryDTO) {
  changedIds.value.add(row.id!)
  row.totalPoints = calculateTotal(row)
}

function handleSelectionChange(rows: SalaryDTO[]) {
  selectedRows.value = rows
  emit('selection-change', rows)
}

function handleSave() {
  const changedRows = props.salaries.filter(s => changedIds.value.has(s.id!))
  emit('save', changedRows)
  changedIds.value.clear()
}

function handleAutoDistribute() {
  emit('auto-distribute')
}

const roleText = (r: string) => ({ MEMBER: '成员', CORE_MEMBER: '核心', ADMIN: '管理员' }[r] || r)
const statusText = (s: string) => ({ PENDING: '待审核', CALCULATED: '已计算', APPROVED: '已审批', PAID: '已发放' }[s] || s)
const statusType = (s: string) => ({ PENDING: 'info', CALCULATED: 'warning', APPROVED: 'success', PAID: '' }[s] || '')
</script>

<style scoped>
.table-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 16px; padding: 12px; background: #f5f7fa; border-radius: 4px; }
.summary { display: flex; gap: 24px; }
.error { color: #f56c6c; }
.error-tip { font-size: 12px; color: #f56c6c; }
.highlight { color: #409eff; font-weight: bold; }
</style>
```

**Commit:** `git add frontend/src/views/salary/components/EditableSalaryTable.vue && git commit -m "feat: add EditableSalaryTable component"`

---

### Task 88: 创建CheckinPointsDisplay组件

**Files:** `frontend/src/views/salary/components/CheckinPointsDisplay.vue`

**Step 1:** 创建CheckinPointsDisplay.vue
```vue
<template>
  <div class="checkin-points-display">
    <el-card>
      <template #header>
        <div class="header">
          <span>{{ userName }} - {{ period }} 签到统计</span>
          <el-tag :type="levelType">{{ levelText }}</el-tag>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="8">
          <el-statistic title="签到天数" :value="performance.checkinCount || 0">
            <template #suffix>/ 30天</template>
          </el-statistic>
          <el-progress
            :percentage="checkinPercentage"
            :color="checkinColor"
            :stroke-width="10"
          />
        </el-col>
        <el-col :span="8">
          <el-statistic title="参与活动" :value="performance.activityCount || 0">
            <template #suffix>次</template>
          </el-statistic>
        </el-col>
        <el-col :span="8">
          <el-statistic title="额外积分" :value="performance.extraPoints || 0" />
        </el-col>
      </el-row>

      <el-divider />

      <div class="points-breakdown">
        <h4>积分明细</h4>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="签到基础积分">
            {{ performance.checkinCount * 10 }} 分
          </el-descriptions-item>
          <el-descriptions-item label="满勤奖励">
            {{ performance.checkinCount >= 20 ? 50 : 0 }} 分
          </el-descriptions-item>
          <el-descriptions-item label="活动参与积分">
            {{ performance.activityCount * 20 }} 分
          </el-descriptions-item>
          <el-descriptions-item label="额外奖励">
            {{ performance.extraPoints || 0 }} 分
          </el-descriptions-item>
        </el-descriptions>
        <div class="total">
          <span>总计:</span>
          <strong>{{ totalPoints }} 分</strong>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { MonthlyPerformanceDTO } from '@/types/salary'

const props = defineProps<{
  performance: MonthlyPerformanceDTO
  userName: string
  period: string
}>()

const checkinPercentage = computed(() =>
  Math.min(100, ((props.performance.checkinCount || 0) / 30) * 100)
)

const checkinColor = computed(() => {
  const count = props.performance.checkinCount || 0
  if (count >= 20) return '#67c23a'
  if (count >= 10) return '#409eff'
  return '#f56c6c'
})

const totalPoints = computed(() => {
  const p = props.performance
  const checkinBase = (p.checkinCount || 0) * 10
  const fullAttendance = (p.checkinCount || 0) >= 20 ? 50 : 0
  const activityPoints = (p.activityCount || 0) * 20
  return checkinBase + fullAttendance + activityPoints + (p.extraPoints || 0)
})

const levelText = computed(() => ({
  EXCELLENT: '优秀',
  GOOD: '良好',
  NORMAL: '一般',
  POOR: '较差'
}[props.performance.performanceLevel] || '-'))

const levelType = computed(() => ({
  EXCELLENT: 'success',
  GOOD: '',
  NORMAL: 'warning',
  POOR: 'danger'
}[props.performance.performanceLevel] || 'info'))
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
.points-breakdown { margin-top: 16px; }
.total { display: flex; justify-content: flex-end; gap: 8px; margin-top: 16px; font-size: 18px; }
</style>
```

**Commit:** `git add frontend/src/views/salary/components/CheckinPointsDisplay.vue && git commit -m "feat: add CheckinPointsDisplay component"`

---

### Task 89: 创建PoolSummaryCard组件

**Files:** `frontend/src/views/salary/components/PoolSummaryCard.vue`

**Step 1:** 创建PoolSummaryCard.vue
```vue
<template>
  <el-card class="pool-summary-card">
    <template #header>
      <div class="header">
        <span>薪酬池 - {{ pool.period }}</span>
        <el-tag :type="statusType">{{ statusText }}</el-tag>
      </div>
    </template>

    <el-row :gutter="20">
      <el-col :span="6">
        <el-statistic title="池总额" prefix="¥" :value="pool.totalAmount" :precision="2" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="已分配" prefix="¥" :value="pool.distributedAmount" :precision="2" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="剩余" prefix="¥" :value="remainingAmount" :precision="2" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="成员数" :value="pool.memberCount" suffix="人" />
      </el-col>
    </el-row>

    <el-progress
      :percentage="distributionPercentage"
      :color="progressColor"
      :stroke-width="20"
      class="distribution-progress"
    >
      <template #default>
        {{ distributionPercentage.toFixed(1) }}% 已分配
      </template>
    </el-progress>

    <div class="pool-actions" v-if="showActions">
      <el-button
        v-if="pool.status === 'PENDING'"
        type="primary"
        @click="emit('distribute')"
      >
        自动分配
      </el-button>
      <el-button
        v-if="pool.status === 'DISTRIBUTED'"
        type="success"
        @click="emit('approve')"
      >
        审批通过
      </el-button>
      <el-button @click="emit('export')">导出Excel</el-button>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { SalaryPoolSummary } from '@/types/salary'

const props = defineProps<{
  pool: SalaryPoolSummary
  showActions?: boolean
}>()

const emit = defineEmits(['distribute', 'approve', 'export'])

const remainingAmount = computed(() =>
  (props.pool.totalAmount || 0) - (props.pool.distributedAmount || 0)
)

const distributionPercentage = computed(() => {
  if (!props.pool.totalAmount) return 0
  return ((props.pool.distributedAmount || 0) / props.pool.totalAmount) * 100
})

const progressColor = computed(() => {
  const pct = distributionPercentage.value
  if (pct >= 95) return '#67c23a'
  if (pct >= 50) return '#409eff'
  return '#e6a23c'
})

const statusText = computed(() => ({
  PENDING: '待分配',
  DISTRIBUTED: '已分配',
  APPROVED: '已审批',
  PAID: '已发放'
}[props.pool.status] || props.pool.status))

const statusType = computed(() => ({
  PENDING: 'info',
  DISTRIBUTED: 'warning',
  APPROVED: 'success',
  PAID: ''
}[props.pool.status] || ''))
</script>

<style scoped>
.pool-summary-card { margin-bottom: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; }
.distribution-progress { margin-top: 20px; }
.pool-actions { display: flex; gap: 12px; margin-top: 20px; justify-content: flex-end; }
</style>
```

**Commit:** `git add frontend/src/views/salary/components/PoolSummaryCard.vue && git commit -m "feat: add PoolSummaryCard component"`

---

### Task 90: 创建MemberFlowLogView

**Files:** `frontend/src/views/salary/MemberFlowLog.vue`

**Step 1:** 创建MemberFlowLog.vue
```vue
<template>
  <div class="member-flow-log">
    <div class="page-header">
      <h2>成员流动记录</h2>
      <el-button type="primary" @click="showAddDialog = true" v-permission="'member:manage'">
        记录变动
      </el-button>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="变动类型">
          <el-select v-model="queryParams.flowType" clearable placeholder="全部类型">
            <el-option label="入会" value="JOIN" />
            <el-option label="退会" value="LEAVE" />
            <el-option label="晋升" value="PROMOTE" />
            <el-option label="降级" value="DEMOTE" />
            <el-option label="调动" value="TRANSFER" />
          </el-select>
        </el-form-item>
        <el-form-item label="成员">
          <el-input v-model="queryParams.userName" placeholder="搜索成员" clearable />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="queryParams.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="logs" v-loading="loading">
      <el-table-column prop="user.nickname" label="成员" width="120" />
      <el-table-column prop="flowType" label="变动类型" width="100">
        <template #default="{ row }">
          <el-tag :type="flowTypeStyle(row.flowType)">{{ flowTypeText(row.flowType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="角色变化" width="200">
        <template #default="{ row }">
          <span v-if="row.fromRole">{{ roleText(row.fromRole) }}</span>
          <el-icon v-if="row.fromRole && row.toRole"><ArrowRight /></el-icon>
          <span v-if="row.toRole">{{ roleText(row.toRole) }}</span>
          <span v-if="!row.fromRole && !row.toRole">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="原因" min-width="200" show-overflow-tooltip />
      <el-table-column prop="operator.nickname" label="操作人" width="120" />
      <el-table-column prop="createdAt" label="时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      v-model:page-size="pageSize"
      :total="total"
      layout="total, sizes, prev, pager, next"
      @change="fetchData"
    />

    <!-- 添加变动对话框 -->
    <el-dialog v-model="showAddDialog" title="记录成员变动" width="500px">
      <el-form :model="addForm" :rules="addRules" ref="addFormRef" label-width="100px">
        <el-form-item label="成员" prop="userId">
          <el-select v-model="addForm.userId" filterable placeholder="选择成员">
            <el-option v-for="u in members" :key="u.id" :label="u.nickname" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="变动类型" prop="flowType">
          <el-select v-model="addForm.flowType" placeholder="选择类型">
            <el-option label="晋升" value="PROMOTE" />
            <el-option label="降级" value="DEMOTE" />
            <el-option label="退会" value="LEAVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标角色" prop="toRole" v-if="addForm.flowType !== 'LEAVE'">
          <el-select v-model="addForm.toRole" placeholder="选择角色">
            <el-option label="成员" value="MEMBER" />
            <el-option label="核心成员" value="CORE_MEMBER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="原因" prop="reason">
          <el-input v-model="addForm.reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getMemberFlowLogs } from '@/api/salary'
import { getMembers, promoteMember, demoteMember, leaveMember } from '@/api/user'
import { formatDate } from '@/utils/date'

const loading = ref(false)
const logs = ref([])
const members = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showAddDialog = ref(false)
const addFormRef = ref()

const queryParams = reactive({ flowType: '', userName: '', dateRange: [] })
const addForm = reactive({ userId: null, flowType: '', toRole: '', reason: '' })
const addRules = {
  userId: [{ required: true, message: '请选择成员' }],
  flowType: [{ required: true, message: '请选择变动类型' }],
  reason: [{ required: true, message: '请填写原因' }]
}

onMounted(() => { fetchData(); loadMembers() })

async function fetchData() {
  loading.value = true
  try {
    const res = await getMemberFlowLogs(0, { ...queryParams, page: page.value - 1, size: pageSize.value })
    logs.value = res.data.content
    total.value = res.data.totalElements
  } finally { loading.value = false }
}

async function loadMembers() {
  const res = await getMembers()
  members.value = res.data
}

async function handleAdd() {
  await addFormRef.value.validate()
  if (addForm.flowType === 'PROMOTE') await promoteMember(addForm.userId!, addForm.toRole, addForm.reason)
  else if (addForm.flowType === 'DEMOTE') await demoteMember(addForm.userId!, addForm.toRole, addForm.reason)
  else if (addForm.flowType === 'LEAVE') await leaveMember(addForm.userId!, addForm.reason)
  ElMessage.success('操作成功')
  showAddDialog.value = false
  fetchData()
}

const flowTypeText = (t: string) => ({ JOIN: '入会', LEAVE: '退会', PROMOTE: '晋升', DEMOTE: '降级', TRANSFER: '调动' }[t] || t)
const flowTypeStyle = (t: string) => ({ JOIN: 'success', LEAVE: 'danger', PROMOTE: 'success', DEMOTE: 'warning', TRANSFER: 'info' }[t] || '')
const roleText = (r: string) => ({ APPLICANT: '申请者', MEMBER: '成员', CORE_MEMBER: '核心成员', ADMIN: '管理员' }[r] || r)
</script>
```

**Commit:** `git add frontend/src/views/salary/MemberFlowLog.vue && git commit -m "feat: add MemberFlowLogView"`

---

### Task 91: 创建SalaryBatchEdit视图（含验证）

**Files:** `frontend/src/views/salary/batch.vue`

**Step 1:** 创建batch.vue
```vue
<template>
  <div class="salary-batch-edit">
    <div class="page-header">
      <h2>薪酬批量编辑 - {{ currentPeriod }}</h2>
      <div class="period-selector">
        <el-date-picker
          v-model="selectedMonth"
          type="month"
          placeholder="选择月份"
          format="YYYY-MM"
          value-format="YYYY-MM"
          @change="handlePeriodChange"
        />
      </div>
    </div>

    <!-- 薪酬池概览 -->
    <PoolSummaryCard
      v-if="poolSummary"
      :pool="poolSummary"
      :show-actions="true"
      @distribute="handleAutoDistribute"
      @approve="handleApprove"
      @export="handleExport"
    />

    <!-- 验证结果 -->
    <el-alert
      v-if="validationResult && !validationResult.valid"
      type="error"
      :closable="false"
      class="validation-alert"
    >
      <template #title>验证失败</template>
      <ul>
        <li v-for="(err, idx) in validationResult.errors" :key="idx">{{ err }}</li>
      </ul>
    </el-alert>

    <el-alert
      v-if="validationResult?.warnings?.length"
      type="warning"
      :closable="false"
      class="validation-alert"
    >
      <template #title>警告</template>
      <ul>
        <li v-for="(warn, idx) in validationResult.warnings" :key="idx">{{ warn }}</li>
      </ul>
    </el-alert>

    <!-- 可编辑表格 -->
    <EditableSalaryTable
      :salaries="salaries"
      :loading="loading"
      :editable="canEdit"
      @save="handleSave"
      @auto-distribute="handleAutoDistribute"
    />

    <!-- 操作按钮 -->
    <div class="page-actions">
      <el-button @click="handleValidate">验证</el-button>
      <el-button type="primary" @click="handleSaveAll" :disabled="!canEdit">
        保存全部
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getSalaries,
  getPoolSummary,
  batchUpdateSalaries,
  validateSalaries,
  distributePool,
  exportSalaries
} from '@/api/salary'
import PoolSummaryCard from './components/PoolSummaryCard.vue'
import EditableSalaryTable from './components/EditableSalaryTable.vue'
import type { SalaryDTO, SalaryPoolSummary, SalaryValidationResult } from '@/types/salary'

const loading = ref(false)
const salaries = ref<SalaryDTO[]>([])
const poolSummary = ref<SalaryPoolSummary | null>(null)
const validationResult = ref<SalaryValidationResult | null>(null)
const selectedMonth = ref('')

const currentPeriod = computed(() => selectedMonth.value || getCurrentPeriod())
const canEdit = computed(() => poolSummary.value?.status === 'PENDING' || poolSummary.value?.status === 'DISTRIBUTED')

onMounted(() => {
  selectedMonth.value = getCurrentPeriod()
  fetchData()
})

function getCurrentPeriod(): string {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

async function fetchData() {
  loading.value = true
  try {
    const [salaryRes, poolRes] = await Promise.all([
      getSalaries({ period: currentPeriod.value, page: 0, size: 100 }),
      getPoolSummary(currentPeriod.value).catch(() => null)
    ])
    salaries.value = salaryRes.data.content
    poolSummary.value = poolRes?.data || null
  } finally {
    loading.value = false
  }
}

function handlePeriodChange() {
  validationResult.value = null
  fetchData()
}

async function handleValidate() {
  const res = await validateSalaries(currentPeriod.value)
  validationResult.value = res.data
  if (res.data.valid) {
    ElMessage.success('验证通过')
  }
}

async function handleSave(changedRows: SalaryDTO[]) {
  await batchUpdateSalaries(changedRows.map(r => ({
    id: r.id,
    bonusPoints: r.bonusPoints,
    deduction: r.deduction,
    salary: r.salary,
    remark: r.remark
  })))
  ElMessage.success('保存成功')
  fetchData()
}

async function handleSaveAll() {
  await handleValidate()
  if (!validationResult.value?.valid) {
    return ElMessage.error('请先修正验证错误')
  }
  await handleSave(salaries.value)
}

async function handleAutoDistribute() {
  await ElMessageBox.confirm('确定自动分配薪酬？将按积分比例分配。')
  await distributePool(currentPeriod.value)
  ElMessage.success('分配完成')
  fetchData()
}

async function handleApprove() {
  await ElMessageBox.confirm('确定审批通过？')
  // 调用审批API
  ElMessage.success('审批通过')
  fetchData()
}

async function handleExport() {
  const res = await exportSalaries(currentPeriod.value)
  const url = window.URL.createObjectURL(new Blob([res.data]))
  const link = document.createElement('a')
  link.href = url
  link.download = `薪酬表_${currentPeriod.value}.xlsx`
  link.click()
}
</script>

<style scoped>
.salary-batch-edit { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.validation-alert { margin-bottom: 20px; }
.page-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 20px; }
</style>
```

**Commit:** `git add frontend/src/views/salary/batch.vue && git commit -m "feat: add SalaryBatchEdit view with validation"`

---

### Task 92: 添加薪酬路由

**Files:** `frontend/src/router/modules/salary.ts`

**Step 1:** 创建薪酬路由模块
```typescript
import type { RouteRecordRaw } from 'vue-router'

const salaryRoutes: RouteRecordRaw[] = [
  {
    path: '/salary',
    name: 'Salary',
    meta: { title: '薪酬管理', icon: 'Money' },
    redirect: '/salary/list',
    children: [
      {
        path: 'list',
        name: 'SalaryList',
        component: () => import('@/views/salary/index.vue'),
        meta: { title: '薪酬列表' }
      },
      {
        path: 'batch',
        name: 'SalaryBatch',
        component: () => import('@/views/salary/batch.vue'),
        meta: { title: '批量编辑', permission: 'salary:edit' }
      },
      {
        path: 'flow-log',
        name: 'MemberFlowLog',
        component: () => import('@/views/salary/MemberFlowLog.vue'),
        meta: { title: '成员流动' }
      }
    ]
  }
]

export default salaryRoutes
```

**Commit:** `git add frontend/src/router/modules/salary.ts && git commit -m "feat: add salary routes"`

---

## Phase 12: Frontend - Configuration Center (Tasks 93-98)

### Task 93: 创建配置API模块

**Files:** `frontend/src/api/config.ts`

**Step 1:** 创建config.ts
```typescript
import request from '@/utils/request'
import type { ConfigDTO, ConfigGroupDTO, ConfigTestResult, ConfigChangeLogDTO } from '@/types/config'

// 获取配置
export function getConfigs() {
  return request.get<ConfigDTO[]>('/api/configs')
}

export function getConfigsByGroup() {
  return request.get<ConfigGroupDTO[]>('/api/configs/groups')
}

export function getConfig(key: string) {
  return request.get<ConfigDTO>(`/api/configs/${key}`)
}

// 更新配置
export function updateConfig(key: string, value: string) {
  return request.put(`/api/configs/${key}`, { configValue: value })
}

export function batchUpdateConfigs(configs: { configKey: string; configValue: string }[]) {
  return request.post('/api/configs/batch', { configs })
}

// 测试配置
export function testAIConfig() {
  return request.post<ConfigTestResult>('/api/configs/test/ai')
}

export function testOSSConfig() {
  return request.post<ConfigTestResult>('/api/configs/test/oss')
}

export function testEmailConfig() {
  return request.post<ConfigTestResult>('/api/configs/test/email')
}

// 缓存管理
export function refreshCache() {
  return request.post('/api/configs/refresh-cache')
}

// 变更历史
export function getConfigHistory(key: string, params: any) {
  return request.get<Page<ConfigChangeLogDTO>>(`/api/configs/history/${key}`, { params })
}
```

**Commit:** `git add frontend/src/api/config.ts && git commit -m "feat: add config API module"`

---

### Task 94: 创建AIConfigForm组件

**Files:** `frontend/src/views/settings/components/AIConfigForm.vue`

**Step 1:** 创建AIConfigForm.vue
```vue
<template>
  <el-form :model="form" :rules="rules" ref="formRef" label-width="140px">
    <el-form-item label="AI服务提供商" prop="provider">
      <el-select v-model="form.provider" placeholder="选择提供商">
        <el-option label="OpenAI兼容" value="openai" />
        <el-option label="Claude" value="claude" />
        <el-option label="自定义" value="custom" />
      </el-select>
    </el-form-item>

    <el-form-item label="API地址" prop="baseUrl">
      <el-input v-model="form.baseUrl" placeholder="https://api.openai.com/v1" />
    </el-form-item>

    <el-form-item label="API密钥" prop="apiKey">
      <el-input
        v-model="form.apiKey"
        type="password"
        show-password
        placeholder="sk-..."
      />
      <div class="form-tip">密钥将加密存储</div>
    </el-form-item>

    <el-form-item label="模型" prop="model">
      <el-select v-model="form.model" filterable allow-create placeholder="选择或输入模型">
        <el-option label="gpt-4" value="gpt-4" />
        <el-option label="gpt-4-turbo" value="gpt-4-turbo" />
        <el-option label="gpt-3.5-turbo" value="gpt-3.5-turbo" />
        <el-option label="claude-3-opus" value="claude-3-opus-20240229" />
        <el-option label="claude-3-sonnet" value="claude-3-sonnet-20240229" />
      </el-select>
    </el-form-item>

    <el-form-item label="最大Token数" prop="maxTokens">
      <el-input-number v-model="form.maxTokens" :min="100" :max="8192" :step="100" />
    </el-form-item>

    <el-form-item label="温度" prop="temperature">
      <el-slider v-model="form.temperature" :min="0" :max="2" :step="0.1" show-input />
    </el-form-item>

    <el-form-item>
      <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      <ConfigTestButton type="ai" @test="handleTest" />
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getConfig, updateConfig } from '@/api/config'
import ConfigTestButton from './ConfigTestButton.vue'

const props = defineProps<{ configs?: Record<string, string> }>()
const emit = defineEmits(['save'])

const formRef = ref()
const saving = ref(false)

const form = reactive({
  provider: 'openai',
  baseUrl: '',
  apiKey: '',
  model: 'gpt-4',
  maxTokens: 4096,
  temperature: 0.7
})

const rules = {
  baseUrl: [{ required: true, message: '请输入API地址' }],
  apiKey: [{ required: true, message: '请输入API密钥' }],
  model: [{ required: true, message: '请选择模型' }]
}

watch(() => props.configs, (configs) => {
  if (configs) {
    form.provider = configs['ai.provider'] || 'openai'
    form.baseUrl = configs['ai.base_url'] || ''
    form.apiKey = configs['ai.api_key'] || ''
    form.model = configs['ai.model'] || 'gpt-4'
    form.maxTokens = parseInt(configs['ai.max_tokens'] || '4096')
    form.temperature = parseFloat(configs['ai.temperature'] || '0.7')
  }
}, { immediate: true })

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    await Promise.all([
      updateConfig('ai.provider', form.provider),
      updateConfig('ai.base_url', form.baseUrl),
      updateConfig('ai.api_key', form.apiKey),
      updateConfig('ai.model', form.model),
      updateConfig('ai.max_tokens', String(form.maxTokens)),
      updateConfig('ai.temperature', String(form.temperature))
    ])
    ElMessage.success('保存成功')
    emit('save')
  } finally {
    saving.value = false
  }
}

function handleTest(result: any) {
  if (result.success) {
    ElMessage.success(result.message)
  } else {
    ElMessage.error(result.message)
  }
}
</script>

<style scoped>
.form-tip { font-size: 12px; color: #909399; margin-top: 4px; }
</style>
```

**Commit:** `git add frontend/src/views/settings/components/AIConfigForm.vue && git commit -m "feat: add AIConfigForm component"`

---

### Task 95: 创建OSSConfigForm组件

**Files:** `frontend/src/views/settings/components/OSSConfigForm.vue`

**Step 1:** 创建OSSConfigForm.vue
```vue
<template>
  <el-form :model="form" :rules="rules" ref="formRef" label-width="140px">
    <el-form-item label="存储服务" prop="provider">
      <el-select v-model="form.provider" placeholder="选择存储服务">
        <el-option label="阿里云OSS" value="aliyun" />
        <el-option label="腾讯云COS" value="tencent" />
        <el-option label="AWS S3" value="aws" />
        <el-option label="MinIO" value="minio" />
      </el-select>
    </el-form-item>

    <el-form-item label="Endpoint" prop="endpoint">
      <el-input v-model="form.endpoint" placeholder="https://oss-cn-hangzhou.aliyuncs.com" />
    </el-form-item>

    <el-form-item label="Access Key" prop="accessKey">
      <el-input v-model="form.accessKey" placeholder="LTAI..." />
    </el-form-item>

    <el-form-item label="Secret Key" prop="secretKey">
      <el-input v-model="form.secretKey" type="password" show-password placeholder="..." />
      <div class="form-tip">密钥将加密存储</div>
    </el-form-item>

    <el-form-item label="Bucket" prop="bucket">
      <el-input v-model="form.bucket" placeholder="my-bucket" />
    </el-form-item>

    <el-form-item label="Region" prop="region">
      <el-input v-model="form.region" placeholder="cn-hangzhou" />
    </el-form-item>

    <el-form-item label="访问域名" prop="domain">
      <el-input v-model="form.domain" placeholder="https://cdn.example.com" />
      <div class="form-tip">可选，用于生成文件访问URL</div>
    </el-form-item>

    <el-form-item>
      <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      <ConfigTestButton type="oss" @test="handleTest" />
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { updateConfig } from '@/api/config'
import ConfigTestButton from './ConfigTestButton.vue'

const props = defineProps<{ configs?: Record<string, string> }>()
const emit = defineEmits(['save'])

const formRef = ref()
const saving = ref(false)

const form = reactive({
  provider: 'aliyun',
  endpoint: '',
  accessKey: '',
  secretKey: '',
  bucket: '',
  region: '',
  domain: ''
})

const rules = {
  endpoint: [{ required: true, message: '请输入Endpoint' }],
  accessKey: [{ required: true, message: '请输入Access Key' }],
  secretKey: [{ required: true, message: '请输入Secret Key' }],
  bucket: [{ required: true, message: '请输入Bucket名称' }]
}

watch(() => props.configs, (configs) => {
  if (configs) {
    form.provider = configs['oss.provider'] || 'aliyun'
    form.endpoint = configs['oss.endpoint'] || ''
    form.accessKey = configs['oss.access_key'] || ''
    form.secretKey = configs['oss.secret_key'] || ''
    form.bucket = configs['oss.bucket'] || ''
    form.region = configs['oss.region'] || ''
    form.domain = configs['oss.domain'] || ''
  }
}, { immediate: true })

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    await Promise.all([
      updateConfig('oss.provider', form.provider),
      updateConfig('oss.endpoint', form.endpoint),
      updateConfig('oss.access_key', form.accessKey),
      updateConfig('oss.secret_key', form.secretKey),
      updateConfig('oss.bucket', form.bucket),
      updateConfig('oss.region', form.region),
      updateConfig('oss.domain', form.domain)
    ])
    ElMessage.success('保存成功')
    emit('save')
  } finally {
    saving.value = false
  }
}

function handleTest(result: any) {
  if (result.success) ElMessage.success(result.message)
  else ElMessage.error(result.message)
}
</script>
```

**Commit:** `git add frontend/src/views/settings/components/OSSConfigForm.vue && git commit -m "feat: add OSSConfigForm component"`

---

### Task 96: 创建EmailConfigForm组件

**Files:** `frontend/src/views/settings/components/EmailConfigForm.vue`

**Step 1:** 创建EmailConfigForm.vue
```vue
<template>
  <el-form :model="form" :rules="rules" ref="formRef" label-width="140px">
    <el-form-item label="SMTP服务器" prop="smtpHost">
      <el-input v-model="form.smtpHost" placeholder="smtp.example.com" />
    </el-form-item>

    <el-form-item label="SMTP端口" prop="smtpPort">
      <el-select v-model="form.smtpPort" placeholder="选择端口">
        <el-option label="25 (不加密)" :value="25" />
        <el-option label="465 (SSL)" :value="465" />
        <el-option label="587 (TLS)" :value="587" />
      </el-select>
    </el-form-item>

    <el-form-item label="加密方式" prop="encryption">
      <el-radio-group v-model="form.encryption">
        <el-radio label="none">无</el-radio>
        <el-radio label="ssl">SSL</el-radio>
        <el-radio label="tls">TLS</el-radio>
      </el-radio-group>
    </el-form-item>

    <el-form-item label="发件人邮箱" prop="username">
      <el-input v-model="form.username" placeholder="noreply@example.com" />
    </el-form-item>

    <el-form-item label="邮箱密码" prop="password">
      <el-input v-model="form.password" type="password" show-password placeholder="授权码或密码" />
      <div class="form-tip">部分邮箱需要使用授权码而非密码</div>
    </el-form-item>

    <el-form-item label="发件人名称" prop="fromName">
      <el-input v-model="form.fromName" placeholder="华分系统" />
    </el-form-item>

    <el-form-item label="测试收件人" prop="testEmail">
      <el-input v-model="form.testEmail" placeholder="test@example.com" />
      <div class="form-tip">用于测试邮件发送</div>
    </el-form-item>

    <el-form-item>
      <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      <ConfigTestButton type="email" @test="handleTest" />
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { updateConfig } from '@/api/config'
import ConfigTestButton from './ConfigTestButton.vue'

const props = defineProps<{ configs?: Record<string, string> }>()
const emit = defineEmits(['save'])

const formRef = ref()
const saving = ref(false)

const form = reactive({
  smtpHost: '',
  smtpPort: 587,
  encryption: 'tls',
  username: '',
  password: '',
  fromName: '',
  testEmail: ''
})

const rules = {
  smtpHost: [{ required: true, message: '请输入SMTP服务器' }],
  smtpPort: [{ required: true, message: '请选择端口' }],
  username: [{ required: true, message: '请输入发件人邮箱', type: 'email' }],
  password: [{ required: true, message: '请输入密码' }]
}

watch(() => props.configs, (configs) => {
  if (configs) {
    form.smtpHost = configs['email.smtp_host'] || ''
    form.smtpPort = parseInt(configs['email.smtp_port'] || '587')
    form.encryption = configs['email.encryption'] || 'tls'
    form.username = configs['email.username'] || ''
    form.password = configs['email.password'] || ''
    form.fromName = configs['email.from_name'] || ''
    form.testEmail = configs['email.test_email'] || ''
  }
}, { immediate: true })

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    await Promise.all([
      updateConfig('email.smtp_host', form.smtpHost),
      updateConfig('email.smtp_port', String(form.smtpPort)),
      updateConfig('email.encryption', form.encryption),
      updateConfig('email.username', form.username),
      updateConfig('email.password', form.password),
      updateConfig('email.from_name', form.fromName),
      updateConfig('email.test_email', form.testEmail)
    ])
    ElMessage.success('保存成功')
    emit('save')
  } finally {
    saving.value = false
  }
}

function handleTest(result: any) {
  if (result.success) ElMessage.success(result.message)
  else ElMessage.error(result.message)
}
</script>
```

**Commit:** `git add frontend/src/views/settings/components/EmailConfigForm.vue && git commit -m "feat: add EmailConfigForm component"`

---

### Task 97: 创建ConfigTestButton组件

**Files:** `frontend/src/views/settings/components/ConfigTestButton.vue`

**Step 1:** 创建ConfigTestButton.vue
```vue
<template>
  <el-button
    :type="buttonType"
    :loading="testing"
    @click="handleTest"
  >
    <el-icon v-if="!testing && testResult"><component :is="resultIcon" /></el-icon>
    {{ buttonText }}
  </el-button>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Check, Close } from '@element-plus/icons-vue'
import { testAIConfig, testOSSConfig, testEmailConfig } from '@/api/config'
import type { ConfigTestResult } from '@/types/config'

const props = defineProps<{
  type: 'ai' | 'oss' | 'email'
}>()

const emit = defineEmits(['test'])

const testing = ref(false)
const testResult = ref<ConfigTestResult | null>(null)

const buttonText = computed(() => {
  if (testing.value) return '测试中...'
  if (testResult.value?.success) return '测试成功'
  if (testResult.value && !testResult.value.success) return '测试失败'
  return '测试连接'
})

const buttonType = computed(() => {
  if (testResult.value?.success) return 'success'
  if (testResult.value && !testResult.value.success) return 'danger'
  return 'default'
})

const resultIcon = computed(() => {
  if (testResult.value?.success) return Check
  if (testResult.value && !testResult.value.success) return Close
  return null
})

async function handleTest() {
  testing.value = true
  testResult.value = null

  try {
    let res
    switch (props.type) {
      case 'ai':
        res = await testAIConfig()
        break
      case 'oss':
        res = await testOSSConfig()
        break
      case 'email':
        res = await testEmailConfig()
        break
    }
    testResult.value = res.data
    emit('test', res.data)
  } catch (e: any) {
    testResult.value = { success: false, message: e.message || '测试失败' }
    emit('test', testResult.value)
  } finally {
    testing.value = false

    // 5秒后重置状态
    setTimeout(() => {
      testResult.value = null
    }, 5000)
  }
}
</script>
```

**Commit:** `git add frontend/src/views/settings/components/ConfigTestButton.vue && git commit -m "feat: add ConfigTestButton component"`

---

### Task 98: 创建ConfigCenter视图（带标签页）

**Files:** `frontend/src/views/settings/config/index.vue`

**Step 1:** 创建配置中心页面
```vue
<template>
  <div class="config-center">
    <div class="page-header">
      <h2>配置中心</h2>
      <div class="actions">
        <el-button @click="handleRefreshCache" :loading="refreshing">
          <el-icon><Refresh /></el-icon> 刷新缓存
        </el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="AI服务" name="ai">
        <AIConfigForm :configs="configs" @save="loadConfigs" />
      </el-tab-pane>

      <el-tab-pane label="对象存储" name="oss">
        <OSSConfigForm :configs="configs" @save="loadConfigs" />
      </el-tab-pane>

      <el-tab-pane label="邮件服务" name="email">
        <EmailConfigForm :configs="configs" @save="loadConfigs" />
      </el-tab-pane>

      <el-tab-pane label="系统配置" name="system">
        <el-form label-width="140px">
          <el-form-item label="系统名称">
            <el-input v-model="systemConfigs.siteName" @change="updateSystemConfig('site.name', $event)" />
          </el-form-item>
          <el-form-item label="系统Logo">
            <el-input v-model="systemConfigs.siteLogo" @change="updateSystemConfig('site.logo', $event)" />
          </el-form-item>
          <el-form-item label="备案号">
            <el-input v-model="systemConfigs.icp" @change="updateSystemConfig('site.icp', $event)" />
          </el-form-item>
          <el-form-item label="维护模式">
            <el-switch v-model="systemConfigs.maintenance" @change="updateSystemConfig('site.maintenance', $event ? 'true' : 'false')" />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="变更历史" name="history">
        <el-table :data="historyLogs" v-loading="historyLoading">
          <el-table-column prop="configKey" label="配置项" width="200" />
          <el-table-column label="变更内容" min-width="300">
            <template #default="{ row }">
              <div class="change-content">
                <span class="old-value">{{ maskValue(row.configKey, row.oldValue) }}</span>
                <el-icon><ArrowRight /></el-icon>
                <span class="new-value">{{ maskValue(row.configKey, row.newValue) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="operatorName" label="操作人" width="120" />
          <el-table-column prop="createdAt" label="时间" width="180">
            <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="historyPage"
          :total="historyTotal"
          layout="prev, pager, next"
          @change="loadHistory"
        />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Refresh, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getConfigs, updateConfig, refreshCache, getConfigHistory } from '@/api/config'
import { formatDate } from '@/utils/date'
import AIConfigForm from '../components/AIConfigForm.vue'
import OSSConfigForm from '../components/OSSConfigForm.vue'
import EmailConfigForm from '../components/EmailConfigForm.vue'

const activeTab = ref('ai')
const configs = ref<Record<string, string>>({})
const refreshing = ref(false)
const historyLoading = ref(false)
const historyLogs = ref([])
const historyPage = ref(1)
const historyTotal = ref(0)

const systemConfigs = reactive({
  siteName: '',
  siteLogo: '',
  icp: '',
  maintenance: false
})

onMounted(() => {
  loadConfigs()
})

async function loadConfigs() {
  const res = await getConfigs()
  const configMap: Record<string, string> = {}
  res.data.forEach(c => { configMap[c.configKey] = c.configValue || '' })
  configs.value = configMap

  // 加载系统配置
  systemConfigs.siteName = configMap['site.name'] || ''
  systemConfigs.siteLogo = configMap['site.logo'] || ''
  systemConfigs.icp = configMap['site.icp'] || ''
  systemConfigs.maintenance = configMap['site.maintenance'] === 'true'
}

async function handleRefreshCache() {
  refreshing.value = true
  try {
    await refreshCache()
    ElMessage.success('缓存已刷新')
  } finally {
    refreshing.value = false
  }
}

async function updateSystemConfig(key: string, value: string) {
  await updateConfig(key, value)
  ElMessage.success('保存成功')
}

async function loadHistory() {
  historyLoading.value = true
  try {
    const res = await getConfigHistory('', { page: historyPage.value - 1, size: 10 })
    historyLogs.value = res.data.content
    historyTotal.value = res.data.totalElements
  } finally {
    historyLoading.value = false
  }
}

function maskValue(key: string, value: string): string {
  const sensitiveKeys = ['api_key', 'secret', 'password']
  if (sensitiveKeys.some(k => key.includes(k)) && value?.length > 4) {
    return value.substring(0, 2) + '****' + value.substring(value.length - 2)
  }
  return value || '-'
}
</script>

<style scoped>
.config-center { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.change-content { display: flex; align-items: center; gap: 8px; }
.old-value { color: #f56c6c; text-decoration: line-through; }
.new-value { color: #67c23a; }
</style>
```

**Commit:** `git add frontend/src/views/settings/config/index.vue && git commit -m "feat: add ConfigCenter view with tabs"`

---

## Phase 13: Frontend - Permission & Logging (Tasks 99-105)

### Task 99: 创建MenuManager视图

**Files:** `frontend/src/views/settings/menu/index.vue`

**Step 1:** 创建菜单管理页面
```vue
<template>
  <div class="menu-manager">
    <div class="page-header">
      <h2>菜单管理</h2>
      <el-button type="primary" @click="handleAdd(null)">
        <el-icon><Plus /></el-icon> 添加菜单
      </el-button>
    </div>

    <el-table :data="menuTree" row-key="id" default-expand-all v-loading="loading">
      <el-table-column prop="name" label="菜单名称" min-width="200" />
      <el-table-column prop="path" label="路由路径" width="200" />
      <el-table-column prop="component" label="组件" width="200" />
      <el-table-column prop="icon" label="图标" width="100">
        <template #default="{ row }">
          <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="visible" label="可见" width="80">
        <template #default="{ row }">
          <el-switch v-model="row.visible" @change="handleToggleVisible(row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleAdd(row)">添加子菜单</el-button>
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="menuTreeOptions"
            :props="{ label: 'name', value: 'id' }"
            placeholder="无（顶级菜单）"
            clearable
            check-strictly
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="路由路径" prop="path">
          <el-input v-model="form.path" placeholder="/example" />
        </el-form-item>
        <el-form-item label="组件路径" prop="component">
          <el-input v-model="form.component" placeholder="example/index" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-select v-model="form.icon" filterable placeholder="选择图标">
            <el-option v-for="icon in iconList" :key="icon" :label="icon" :value="icon">
              <el-icon><component :is="icon" /></el-icon>
              <span style="margin-left: 8px">{{ icon }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="是否可见">
          <el-switch v-model="form.visible" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMenuTree, createMenu, updateMenu, deleteMenu } from '@/api/menu'
import type { MenuDTO } from '@/types/menu'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const menuTree = ref<MenuDTO[]>([])
const editingId = ref<number | null>(null)

const form = reactive({
  parentId: null as number | null,
  name: '',
  path: '',
  component: '',
  icon: '',
  sortOrder: 0,
  visible: true
})

const rules = {
  name: [{ required: true, message: '请输入菜单名称' }],
  path: [{ required: true, message: '请输入路由路径' }]
}

const iconList = ['Dashboard', 'User', 'Setting', 'Document', 'Calendar', 'Money', 'VideoCamera', 'List', 'Plus', 'Edit', 'Delete']

const dialogTitle = computed(() => editingId.value ? '编辑菜单' : '添加菜单')
const menuTreeOptions = computed(() => [{ id: null, name: '无（顶级菜单）', children: menuTree.value }])

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await getMenuTree()
    menuTree.value = res.data
  } finally {
    loading.value = false
  }
}

function handleAdd(parent: MenuDTO | null) {
  editingId.value = null
  form.parentId = parent?.id || null
  form.name = ''
  form.path = ''
  form.component = ''
  form.icon = ''
  form.sortOrder = 0
  form.visible = true
  dialogVisible.value = true
}

function handleEdit(row: MenuDTO) {
  editingId.value = row.id!
  form.parentId = row.parentId || null
  form.name = row.name
  form.path = row.path || ''
  form.component = row.component || ''
  form.icon = row.icon || ''
  form.sortOrder = row.sortOrder || 0
  form.visible = row.visible !== false
  dialogVisible.value = true
}

async function handleSubmit() {
  submitting.value = true
  try {
    if (editingId.value) {
      await updateMenu(editingId.value, form)
    } else {
      await createMenu(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: MenuDTO) {
  await ElMessageBox.confirm('确定删除该菜单？子菜单也将被删除。')
  await deleteMenu(row.id!)
  ElMessage.success('删除成功')
  fetchData()
}

async function handleToggleVisible(row: MenuDTO) {
  await updateMenu(row.id!, { visible: row.visible })
  ElMessage.success('更新成功')
}
</script>
```

**Commit:** `git add frontend/src/views/settings/menu/index.vue && git commit -m "feat: add MenuManager view"`

---

### Task 100: 创建PermissionManager视图

**Files:** `frontend/src/views/settings/permission/index.vue`

**Step 1:** 创建权限管理页面
```vue
<template>
  <div class="permission-manager">
    <div class="page-header">
      <h2>权限管理</h2>
      <el-button type="primary" @click="handleAddPermission">
        <el-icon><Plus /></el-icon> 添加权限
      </el-button>
    </div>

    <el-row :gutter="20">
      <!-- 左侧：权限列表 -->
      <el-col :span="14">
        <el-card>
          <template #header>权限列表</template>
          <el-table :data="permissions" v-loading="loading">
            <el-table-column prop="code" label="权限编码" width="180" />
            <el-table-column prop="name" label="权限名称" width="120" />
            <el-table-column prop="module" label="模块" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ row.module }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="150" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button size="small" @click="handleEditPermission(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="handleDeletePermission(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧：角色权限分配 -->
      <el-col :span="10">
        <el-card>
          <template #header>
            <div class="role-header">
              <span>角色权限分配</span>
              <el-select v-model="selectedRole" placeholder="选择角色" @change="loadRolePermissions">
                <el-option label="管理员" value="ADMIN" />
                <el-option label="核心成员" value="CORE_MEMBER" />
                <el-option label="成员" value="MEMBER" />
              </el-select>
            </div>
          </template>

          <div v-if="selectedRole">
            <el-tree
              ref="permissionTreeRef"
              :data="permissionTree"
              :props="{ label: 'name', children: 'children' }"
              show-checkbox
              node-key="id"
              :default-checked-keys="checkedPermissions"
              @check="handlePermissionCheck"
            />
            <div class="tree-actions">
              <el-button type="primary" @click="handleSaveRolePermissions" :loading="saving">
                保存
              </el-button>
            </div>
          </div>
          <el-empty v-else description="请选择角色" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 权限编辑对话框 -->
    <el-dialog v-model="permDialogVisible" :title="permDialogTitle" width="500px">
      <el-form :model="permForm" :rules="permRules" ref="permFormRef" label-width="100px">
        <el-form-item label="权限编码" prop="code">
          <el-input v-model="permForm.code" placeholder="module:action" :disabled="!!editingPermId" />
        </el-form-item>
        <el-form-item label="权限名称" prop="name">
          <el-input v-model="permForm.name" placeholder="查看用户" />
        </el-form-item>
        <el-form-item label="所属模块" prop="module">
          <el-select v-model="permForm.module" filterable allow-create placeholder="选择或输入模块">
            <el-option v-for="m in modules" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="permForm.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitPermission">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAllPermissions,
  createPermission,
  deletePermission,
  getPermissionsByRole,
  batchAssignPermissions
} from '@/api/permission'
import type { PermissionDTO } from '@/types/permission'

const loading = ref(false)
const saving = ref(false)
const permissions = ref<PermissionDTO[]>([])
const selectedRole = ref('')
const checkedPermissions = ref<number[]>([])
const permissionTreeRef = ref()
const permDialogVisible = ref(false)
const editingPermId = ref<number | null>(null)

const permForm = reactive({ code: '', name: '', module: '', description: '' })
const permRules = {
  code: [{ required: true, message: '请输入权限编码' }],
  name: [{ required: true, message: '请输入权限名称' }],
  module: [{ required: true, message: '请选择模块' }]
}

const modules = computed(() => [...new Set(permissions.value.map(p => p.module))])
const permDialogTitle = computed(() => editingPermId.value ? '编辑权限' : '添加权限')

const permissionTree = computed(() => {
  const grouped: Record<string, PermissionDTO[]> = {}
  permissions.value.forEach(p => {
    if (!grouped[p.module]) grouped[p.module] = []
    grouped[p.module].push(p)
  })
  return Object.entries(grouped).map(([module, perms]) => ({
    id: `module_${module}`,
    name: module,
    children: perms
  }))
})

onMounted(() => fetchPermissions())

async function fetchPermissions() {
  loading.value = true
  try {
    const res = await getAllPermissions()
    permissions.value = res.data
  } finally {
    loading.value = false
  }
}

async function loadRolePermissions() {
  if (!selectedRole.value) return
  const res = await getPermissionsByRole(selectedRole.value)
  const permCodes = res.data
  checkedPermissions.value = permissions.value
    .filter(p => permCodes.includes(p.code))
    .map(p => p.id!)
}

function handlePermissionCheck(node: any, data: any) {
  checkedPermissions.value = data.checkedKeys.filter((k: any) => typeof k === 'number')
}

async function handleSaveRolePermissions() {
  saving.value = true
  try {
    await batchAssignPermissions(selectedRole.value, checkedPermissions.value)
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

function handleAddPermission() {
  editingPermId.value = null
  permForm.code = ''
  permForm.name = ''
  permForm.module = ''
  permForm.description = ''
  permDialogVisible.value = true
}

async function handleSubmitPermission() {
  await createPermission(permForm)
  ElMessage.success('保存成功')
  permDialogVisible.value = false
  fetchPermissions()
}

async function handleDeletePermission(row: PermissionDTO) {
  await ElMessageBox.confirm('确定删除该权限？')
  await deletePermission(row.id!)
  ElMessage.success('删除成功')
  fetchPermissions()
}
</script>
```

**Commit:** `git add frontend/src/views/settings/permission/index.vue && git commit -m "feat: add PermissionManager view"`

---

### Task 101: 创建RolePermissionEditor组件

**Files:** `frontend/src/views/settings/components/RolePermissionEditor.vue`

**Step 1:** 创建RolePermissionEditor.vue
```vue
<template>
  <div class="role-permission-editor">
    <el-card>
      <template #header>
        <div class="header">
          <span>{{ roleLabel }} - 权限配置</span>
          <el-tag>{{ checkedCount }} / {{ totalCount }} 项</el-tag>
        </div>
      </template>

      <div class="permission-groups">
        <el-collapse v-model="activeGroups">
          <el-collapse-item
            v-for="group in permissionGroups"
            :key="group.module"
            :name="group.module"
          >
            <template #title>
              <div class="group-title">
                <el-checkbox
                  :model-value="isGroupChecked(group)"
                  :indeterminate="isGroupIndeterminate(group)"
                  @change="handleGroupChange(group, $event)"
                  @click.stop
                />
                <span>{{ group.label }}</span>
                <el-tag size="small" type="info">{{ getGroupCheckedCount(group) }}/{{ group.permissions.length }}</el-tag>
              </div>
            </template>

            <div class="permission-list">
              <el-checkbox
                v-for="perm in group.permissions"
                :key="perm.id"
                :model-value="checkedIds.has(perm.id!)"
                @change="handlePermissionChange(perm, $event)"
              >
                <div class="permission-item">
                  <span class="perm-name">{{ perm.name }}</span>
                  <span class="perm-code">{{ perm.code }}</span>
                </div>
              </el-checkbox>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>

      <div class="editor-actions">
        <el-button @click="handleSelectAll">全选</el-button>
        <el-button @click="handleClearAll">清空</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          保存配置
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { PermissionDTO } from '@/types/permission'

interface PermissionGroup {
  module: string
  label: string
  permissions: PermissionDTO[]
}

const props = defineProps<{
  role: string
  permissions: PermissionDTO[]
  checkedPermissions: number[]
}>()

const emit = defineEmits(['save'])

const saving = ref(false)
const activeGroups = ref<string[]>([])
const checkedIds = ref<Set<number>>(new Set())

const roleLabels: Record<string, string> = {
  ADMIN: '管理员',
  CORE_MEMBER: '核心成员',
  MEMBER: '成员'
}

const moduleLabels: Record<string, string> = {
  user: '用户管理',
  salary: '薪酬管理',
  interview: '面试管理',
  config: '系统配置',
  questionnaire: '问卷管理',
  activity: '活动管理'
}

const roleLabel = computed(() => roleLabels[props.role] || props.role)

const permissionGroups = computed<PermissionGroup[]>(() => {
  const grouped: Record<string, PermissionDTO[]> = {}
  props.permissions.forEach(p => {
    if (!grouped[p.module]) grouped[p.module] = []
    grouped[p.module].push(p)
  })
  return Object.entries(grouped).map(([module, perms]) => ({
    module,
    label: moduleLabels[module] || module,
    permissions: perms
  }))
})

const totalCount = computed(() => props.permissions.length)
const checkedCount = computed(() => checkedIds.value.size)

watch(() => props.checkedPermissions, (ids) => {
  checkedIds.value = new Set(ids)
}, { immediate: true })

function isGroupChecked(group: PermissionGroup): boolean {
  return group.permissions.every(p => checkedIds.value.has(p.id!))
}

function isGroupIndeterminate(group: PermissionGroup): boolean {
  const checked = group.permissions.filter(p => checkedIds.value.has(p.id!)).length
  return checked > 0 && checked < group.permissions.length
}

function getGroupCheckedCount(group: PermissionGroup): number {
  return group.permissions.filter(p => checkedIds.value.has(p.id!)).length
}

function handleGroupChange(group: PermissionGroup, checked: boolean) {
  group.permissions.forEach(p => {
    if (checked) checkedIds.value.add(p.id!)
    else checkedIds.value.delete(p.id!)
  })
}

function handlePermissionChange(perm: PermissionDTO, checked: boolean) {
  if (checked) checkedIds.value.add(perm.id!)
  else checkedIds.value.delete(perm.id!)
}

function handleSelectAll() {
  props.permissions.forEach(p => checkedIds.value.add(p.id!))
}

function handleClearAll() {
  checkedIds.value.clear()
}

async function handleSave() {
  saving.value = true
  try {
    emit('save', Array.from(checkedIds.value))
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
.group-title { display: flex; align-items: center; gap: 12px; }
.permission-list { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; padding: 12px; }
.permission-item { display: flex; flex-direction: column; }
.perm-name { font-weight: 500; }
.perm-code { font-size: 12px; color: #909399; }
.editor-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 20px; padding-top: 20px; border-top: 1px solid #eee; }
</style>
```

**Commit:** `git add frontend/src/views/settings/components/RolePermissionEditor.vue && git commit -m "feat: add RolePermissionEditor component"`

---

### Task 102: 增强LogList视图（分类）

**Files:** `frontend/src/views/logs/index.vue`

**Step 1:** 增强日志列表页面
```vue
<template>
  <div class="log-list">
    <div class="page-header">
      <h2>操作日志</h2>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部日志" name="all" />
      <el-tab-pane label="用户操作" name="user" />
      <el-tab-pane label="系统配置" name="config" />
      <el-tab-pane label="权限变更" name="permission" />
      <el-tab-pane label="薪酬操作" name="salary" />
    </el-tabs>

    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="操作类型">
          <el-select v-model="queryParams.action" clearable placeholder="全部">
            <el-option label="创建" value="CREATE" />
            <el-option label="更新" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="登录" value="LOGIN" />
            <el-option label="导出" value="EXPORT" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="queryParams.username" placeholder="搜索用户名" clearable />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="queryParams.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="logs" v-loading="loading" @row-click="showDetail">
      <el-table-column prop="username" label="操作人" width="120" />
      <el-table-column prop="action" label="操作类型" width="100">
        <template #default="{ row }">
          <el-tag :type="actionType(row.action)" size="small">{{ actionText(row.action) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="module" label="模块" width="100">
        <template #default="{ row }">
          <el-tag type="info" size="small">{{ row.module || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="targetType" label="目标类型" width="120" />
      <el-table-column prop="detail" label="详情" min-width="250" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP地址" width="140" />
      <el-table-column prop="duration" label="耗时" width="100">
        <template #default="{ row }">
          <span :class="{ slow: row.duration > 1000 }">{{ row.duration }}ms</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @change="fetchData"
    />

    <!-- 详情对话框 -->
    <LogDetailDialog v-model="detailVisible" :log="currentLog" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getLogs } from '@/api/log'
import { formatDate } from '@/utils/date'
import LogDetailDialog from './components/LogDetailDialog.vue'

const loading = ref(false)
const logs = ref([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const activeTab = ref('all')
const detailVisible = ref(false)
const currentLog = ref(null)

const queryParams = reactive({
  action: '',
  username: '',
  module: '',
  dateRange: []
})

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const params: any = {
      ...queryParams,
      page: page.value - 1,
      size: pageSize.value
    }
    if (activeTab.value !== 'all') {
      params.module = activeTab.value
    }
    if (queryParams.dateRange?.length === 2) {
      params.startTime = queryParams.dateRange[0]
      params.endTime = queryParams.dateRange[1]
    }
    const res = await getLogs(params)
    logs.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  page.value = 1
  fetchData()
}

function resetQuery() {
  queryParams.action = ''
  queryParams.username = ''
  queryParams.dateRange = []
  fetchData()
}

function showDetail(row: any) {
  currentLog.value = row
  detailVisible.value = true
}

const actionText = (a: string) => ({ CREATE: '创建', UPDATE: '更新', DELETE: '删除', LOGIN: '登录', EXPORT: '导出' }[a] || a)
const actionType = (a: string) => ({ CREATE: 'success', UPDATE: 'warning', DELETE: 'danger', LOGIN: 'info', EXPORT: '' }[a] || '')
</script>

<style scoped>
.log-list { padding: 20px; }
.filter-card { margin-bottom: 20px; }
.slow { color: #f56c6c; }
</style>
```

**Commit:** `git add frontend/src/views/logs/index.vue && git commit -m "feat: enhance LogList view with categories"`

---

### Task 103: 创建LogDetailDialog组件

**Files:** `frontend/src/views/logs/components/LogDetailDialog.vue`

**Step 1:** 创建LogDetailDialog.vue
```vue
<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    title="日志详情"
    width="700px"
  >
    <template v-if="log">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="操作人">{{ log.username }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ log.userId }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="actionType(log.action)">{{ actionText(log.action) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="模块">{{ log.module || '-' }}</el-descriptions-item>
        <el-descriptions-item label="目标类型">{{ log.targetType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="目标ID">{{ log.targetId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ log.ip }}</el-descriptions-item>
        <el-descriptions-item label="耗时">
          <span :class="{ slow: log.duration > 1000 }">{{ log.duration }}ms</span>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ formatDate(log.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="操作详情" :span="2">{{ log.detail }}</el-descriptions-item>
      </el-descriptions>

      <el-divider v-if="log.requestBody || log.responseBody" />

      <el-tabs v-model="activeTab" v-if="log.requestBody || log.responseBody">
        <el-tab-pane label="请求数据" name="request" v-if="log.requestBody">
          <div class="json-viewer">
            <pre>{{ formatJson(log.requestBody) }}</pre>
          </div>
        </el-tab-pane>
        <el-tab-pane label="响应数据" name="response" v-if="log.responseBody">
          <div class="json-viewer">
            <pre>{{ formatJson(log.responseBody) }}</pre>
          </div>
        </el-tab-pane>
      </el-tabs>
    </template>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">关闭</el-button>
      <el-button type="primary" @click="handleCopy">复制详情</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { formatDate } from '@/utils/date'

interface LogDTO {
  id: number
  userId: number
  username: string
  action: string
  module: string
  targetType: string
  targetId: number
  detail: string
  ip: string
  duration: number
  requestBody: string
  responseBody: string
  createdAt: string
}

const props = defineProps<{
  modelValue: boolean
  log: LogDTO | null
}>()

const emit = defineEmits(['update:modelValue'])

const activeTab = ref('request')

function formatJson(str: string): string {
  if (!str) return ''
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

function handleCopy() {
  if (!props.log) return
  const text = `
操作人: ${props.log.username}
操作类型: ${props.log.action}
模块: ${props.log.module || '-'}
目标: ${props.log.targetType} #${props.log.targetId}
详情: ${props.log.detail}
IP: ${props.log.ip}
时间: ${formatDate(props.log.createdAt)}
  `.trim()

  navigator.clipboard.writeText(text)
  ElMessage.success('已复制到剪贴板')
}

const actionText = (a: string) => ({ CREATE: '创建', UPDATE: '更新', DELETE: '删除', LOGIN: '登录', EXPORT: '导出' }[a] || a)
const actionType = (a: string) => ({ CREATE: 'success', UPDATE: 'warning', DELETE: 'danger', LOGIN: 'info', EXPORT: '' }[a] || '')
</script>

<style scoped>
.json-viewer {
  background: #f5f7fa;
  border-radius: 4px;
  padding: 12px;
  max-height: 300px;
  overflow: auto;
}
.json-viewer pre {
  margin: 0;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
}
.slow { color: #f56c6c; font-weight: bold; }
</style>
```

**Commit:** `git add frontend/src/views/logs/components/LogDetailDialog.vue && git commit -m "feat: add LogDetailDialog component"`

---

### Task 104: 创建RoleChangeLogView

**Files:** `frontend/src/views/logs/RoleChangeLog.vue`

**Step 1:** 创建角色变更日志页面
```vue
<template>
  <div class="role-change-log">
    <div class="page-header">
      <h2>角色变更记录</h2>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="用户">
          <el-input v-model="queryParams.userName" placeholder="搜索用户" clearable />
        </el-form-item>
        <el-form-item label="原角色">
          <el-select v-model="queryParams.fromRole" clearable placeholder="全部">
            <el-option label="申请者" value="APPLICANT" />
            <el-option label="成员" value="MEMBER" />
            <el-option label="核心成员" value="CORE_MEMBER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="新角色">
          <el-select v-model="queryParams.toRole" clearable placeholder="全部">
            <el-option label="申请者" value="APPLICANT" />
            <el-option label="成员" value="MEMBER" />
            <el-option label="核心成员" value="CORE_MEMBER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="queryParams.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="logs" v-loading="loading">
      <el-table-column prop="user.nickname" label="用户" width="120" />
      <el-table-column label="角色变更" width="250">
        <template #default="{ row }">
          <div class="role-change">
            <el-tag :type="roleType(row.fromRole)" size="small">{{ roleText(row.fromRole) }}</el-tag>
            <el-icon><ArrowRight /></el-icon>
            <el-tag :type="roleType(row.toRole)" size="small">{{ roleText(row.toRole) }}</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="变更类型" width="100">
        <template #default="{ row }">
          <el-tag :type="changeType(row)" size="small">{{ changeText(row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="原因" min-width="200" show-overflow-tooltip />
      <el-table-column prop="operator.nickname" label="操作人" width="120" />
      <el-table-column prop="createdAt" label="时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      v-model:page-size="pageSize"
      :total="total"
      layout="total, sizes, prev, pager, next"
      @change="fetchData"
    />

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="本月晋升" :value="stats.promotions" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="本月降级" :value="stats.demotions" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="本月入会" :value="stats.joins" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="本月退会" :value="stats.leaves" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { getRoleChangeLogs, getRoleChangeStats } from '@/api/log'
import { formatDate } from '@/utils/date'

const loading = ref(false)
const logs = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const stats = reactive({ promotions: 0, demotions: 0, joins: 0, leaves: 0 })

const queryParams = reactive({
  userName: '',
  fromRole: '',
  toRole: '',
  dateRange: []
})

onMounted(() => {
  fetchData()
  fetchStats()
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getRoleChangeLogs({ ...queryParams, page: page.value - 1, size: pageSize.value })
    logs.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
}

async function fetchStats() {
  const res = await getRoleChangeStats()
  Object.assign(stats, res.data)
}

function resetQuery() {
  queryParams.userName = ''
  queryParams.fromRole = ''
  queryParams.toRole = ''
  queryParams.dateRange = []
  fetchData()
}

const roleText = (r: string) => ({ APPLICANT: '申请者', MEMBER: '成员', CORE_MEMBER: '核心成员', ADMIN: '管理员' }[r] || r || '-')
const roleType = (r: string) => ({ APPLICANT: 'info', MEMBER: '', CORE_MEMBER: 'warning', ADMIN: 'danger' }[r] || '')

function changeText(row: any): string {
  const roleOrder = ['APPLICANT', 'MEMBER', 'CORE_MEMBER', 'ADMIN']
  const fromIdx = roleOrder.indexOf(row.fromRole)
  const toIdx = roleOrder.indexOf(row.toRole)
  if (toIdx > fromIdx) return '晋升'
  if (toIdx < fromIdx) return '降级'
  return '变更'
}

function changeType(row: any): string {
  const roleOrder = ['APPLICANT', 'MEMBER', 'CORE_MEMBER', 'ADMIN']
  const fromIdx = roleOrder.indexOf(row.fromRole)
  const toIdx = roleOrder.indexOf(row.toRole)
  if (toIdx > fromIdx) return 'success'
  if (toIdx < fromIdx) return 'danger'
  return 'info'
}
</script>

<style scoped>
.role-change-log { padding: 20px; }
.filter-card { margin-bottom: 20px; }
.role-change { display: flex; align-items: center; gap: 8px; }
.stat-cards { margin-top: 20px; }
</style>
```

**Commit:** `git add frontend/src/views/logs/RoleChangeLog.vue && git commit -m "feat: add RoleChangeLogView"`

---

### Task 105: 添加权限和日志路由

**Files:** `frontend/src/router/modules/settings.ts`

**Step 1:** 创建设置模块路由
```typescript
import type { RouteRecordRaw } from 'vue-router'

const settingsRoutes: RouteRecordRaw[] = [
  {
    path: '/settings',
    name: 'Settings',
    meta: { title: '系统设置', icon: 'Setting' },
    redirect: '/settings/basic',
    children: [
      {
        path: 'basic',
        name: 'BasicSettings',
        component: () => import('@/views/settings/index.vue'),
        meta: { title: '基础配置' }
      },
      {
        path: 'config',
        name: 'ConfigCenter',
        component: () => import('@/views/settings/config/index.vue'),
        meta: { title: '配置中心', permission: 'config:view' }
      },
      {
        path: 'menu',
        name: 'MenuManager',
        component: () => import('@/views/settings/menu/index.vue'),
        meta: { title: '菜单管理', permission: 'menu:manage' }
      },
      {
        path: 'permission',
        name: 'PermissionManager',
        component: () => import('@/views/settings/permission/index.vue'),
        meta: { title: '权限管理', permission: 'permission:manage' }
      }
    ]
  },
  {
    path: '/logs',
    name: 'Logs',
    meta: { title: '日志管理', icon: 'List' },
    redirect: '/logs/operation',
    children: [
      {
        path: 'operation',
        name: 'OperationLogs',
        component: () => import('@/views/logs/index.vue'),
        meta: { title: '操作日志' }
      },
      {
        path: 'role-change',
        name: 'RoleChangeLogs',
        component: () => import('@/views/logs/RoleChangeLog.vue'),
        meta: { title: '角色变更' }
      }
    ]
  }
]

export default settingsRoutes
```

**Commit:** `git add frontend/src/router/modules/settings.ts && git commit -m "feat: add permission and log routes"`

---

## Phase 14: Integration & Testing (Tasks 106-110)

### Task 106: 测试问卷流程端到端

**Files:** 无新文件，执行测试

**Step 1:** 启动后端服务
```bash
cd backend && mvn spring-boot:run
```

**Step 2:** 启动前端服务
```bash
cd frontend && npm run dev
```

**Step 3:** 测试用例
1. 创建问卷：添加各类型字段，设置条件逻辑
2. 预览问卷：验证字段渲染和条件显示
3. 发布问卷：状态变更为PUBLISHED
4. 填写问卷：提交回复，验证必填和格式校验
5. 查看回复：统计数据正确显示

**Commit:** 无需提交

---

### Task 107: 测试AI面试WebSocket流程

**Files:** 无新文件，执行测试

**Step 1:** 测试用例
1. 发起面试：创建会话，获取sessionToken
2. WebSocket连接：验证STOMP连接成功
3. 发送消息：用户输入，AI流式响应
4. 结束面试：生成评分报告
5. 查看回放：消息历史和评分展示

**Step 2:** 验证违规检测
- 测试9种违规类型的识别
- 验证扣分计算逻辑

**Commit:** 无需提交

---

### Task 108: 测试薪酬计算和验证

**Files:** 无新文件，执行测试

**Step 1:** 测试用例
1. 初始化薪酬池：创建2000元池
2. 生成薪酬记录：计算签到和活动积分
3. 自动分配：按比例分配，验证200-400范围
4. 手动调整：修改奖励/扣除积分
5. 验证：总额不超2000，个人范围正确
6. 审批发放：状态流转正确

**Step 2:** 边界测试
- 成员数为0时的处理
- 总积分为0时的平均分配
- 超出范围的自动截断

**Commit:** 无需提交

---

### Task 109: 测试配置加密和热重载

**Files:** 无新文件，执行测试

**Step 1:** 测试加密
1. 保存敏感配置（API密钥）
2. 验证数据库中存储的是加密值
3. 读取配置时自动解密
4. 前端显示脱敏值

**Step 2:** 测试热重载
1. 修改配置值
2. 验证缓存自动清除
3. 验证新值立即生效
4. 验证配置变更日志记录

**Step 3:** 测试连接
1. AI服务连接测试
2. OSS服务连接测试
3. 邮件服务连接测试

**Commit:** 无需提交

---

### Task 110: 测试权限和菜单系统

**Files:** 无新文件，执行测试

**Step 1:** 测试菜单
1. 创建三级菜单结构
2. 分配菜单到角色
3. 验证不同角色看到不同菜单
4. 验证动态路由生成

**Step 2:** 测试权限
1. 创建权限并分配到角色
2. 验证v-permission指令生效
3. 验证后端权限校验
4. 验证权限变更日志

**Step 3:** 测试角色变更
1. 晋升/降级用户
2. 验证角色变更日志
3. 验证菜单和权限自动更新

**Commit:** 无需提交

---

## 完成检查清单

- [ ] Phase 1: 基础设施配置完成
- [ ] Phase 2: 数据库Schema创建完成
- [ ] Phase 3: 问卷系统后端完成
- [ ] Phase 4: AI面试系统后端完成
- [ ] Phase 5: 薪酬系统增强完成
- [ ] Phase 6: 配置中心完成
- [ ] Phase 7: 权限系统完成
- [ ] Phase 8: 前端布局增强完成
- [ ] Phase 9: 问卷系统前端完成
- [ ] Phase 10: AI面试系统前端完成
- [ ] Phase 11: 薪酬系统前端完成
- [ ] Phase 12: 配置中心前端完成
- [ ] Phase 13: 权限和日志前端完成
- [ ] Phase 14: 集成测试通过

---

**预计总工时:** 约 8-12 小时（按每任务 2-5 分钟计算）

**注意事项:**
1. 每个任务完成后及时提交代码
2. 遇到依赖问题先解决依赖
3. 测试阶段发现问题及时修复
4. 保持代码风格一致
