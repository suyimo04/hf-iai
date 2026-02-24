-- HF-IAI 数据库Schema
-- 创建时间: 2026-02-24

-- 1. 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'APPLICANT',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_role (role),
    INDEX idx_users_status (status),
    INDEX idx_users_email (email),
    INDEX idx_users_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 报名表
CREATE TABLE IF NOT EXISTS applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    form_data JSON,
    reviewer_id BIGINT,
    review_comment TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_applications_user_id (user_id),
    INDEX idx_applications_status (status),
    INDEX idx_applications_reviewer_id (reviewer_id),
    CONSTRAINT fk_applications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_applications_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 面试记录表
CREATE TABLE IF NOT EXISTS interviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    application_id BIGINT NOT NULL,
    score INT,
    answers JSON,
    report TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_interviews_user_id (user_id),
    INDEX idx_interviews_application_id (application_id),
    INDEX idx_interviews_status (status),
    CONSTRAINT fk_interviews_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_interviews_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 面试题库表
CREATE TABLE IF NOT EXISTS interview_questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category VARCHAR(50) NOT NULL,
    question TEXT NOT NULL,
    question_type VARCHAR(20) NOT NULL,
    options JSON,
    answer TEXT,
    keywords JSON,
    score INT DEFAULT 10,
    sort_order INT DEFAULT 0,
    enabled TINYINT(1) DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_questions_category (category),
    INDEX idx_questions_type (question_type),
    INDEX idx_questions_enabled (enabled),
    INDEX idx_questions_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 积分流水表
CREATE TABLE IF NOT EXISTS points (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    amount INT NOT NULL,
    description VARCHAR(255),
    related_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_points_user_id (user_id),
    INDEX idx_points_type (type),
    INDEX idx_points_created_at (created_at),
    CONSTRAINT fk_points_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 薪酬记录表
CREATE TABLE IF NOT EXISTS salaries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    period VARCHAR(20) NOT NULL,
    base_points INT DEFAULT 0,
    bonus_points INT DEFAULT 0,
    deduction INT DEFAULT 0,
    total_points INT DEFAULT 0,
    coins INT DEFAULT 0,
    salary DECIMAL(10,2) DEFAULT 0.00,
    remark TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_salaries_user_id (user_id),
    INDEX idx_salaries_period (period),
    INDEX idx_salaries_status (status),
    UNIQUE INDEX idx_salaries_user_period (user_id, period),
    CONSTRAINT fk_salaries_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 活动表
CREATE TABLE IF NOT EXISTS activities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    location VARCHAR(255),
    max_participants INT,
    points_reward INT DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_activities_status (status),
    INDEX idx_activities_start_time (start_time),
    INDEX idx_activities_created_by (created_by),
    CONSTRAINT fk_activities_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. 活动报名签到表
CREATE TABLE IF NOT EXISTS activity_signups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    signed_in TINYINT(1) DEFAULT 0,
    sign_in_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_signups_activity_id (activity_id),
    INDEX idx_signups_user_id (user_id),
    UNIQUE INDEX idx_signups_activity_user (activity_id, user_id),
    CONSTRAINT fk_signups_activity FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE,
    CONSTRAINT fk_signups_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. 系统配置表
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    description VARCHAR(255),
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. 操作日志表
CREATE TABLE IF NOT EXISTS operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    username VARCHAR(50),
    action VARCHAR(50) NOT NULL,
    target_type VARCHAR(50),
    target_id BIGINT,
    detail TEXT,
    ip VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_logs_user_id (user_id),
    INDEX idx_logs_action (action),
    INDEX idx_logs_target (target_type, target_id),
    INDEX idx_logs_created_at (created_at),
    CONSTRAINT fk_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. AI面试会话表
CREATE TABLE IF NOT EXISTS ai_interview_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    application_id BIGINT,
    status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING, IN_PROGRESS, COMPLETED, EXPIRED
    violation_types JSON,  -- 随机选中的1-2个违规类型
    round_count INT DEFAULT 0,  -- 当前轮数
    max_rounds INT DEFAULT 15,  -- 最大轮数8-15
    ai_provider VARCHAR(50),
    ai_model VARCHAR(100),
    started_at DATETIME,
    ended_at DATETIME,
    expired_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ai_session_user_id (user_id),
    INDEX idx_ai_session_application_id (application_id),
    INDEX idx_ai_session_status (status),
    CONSTRAINT fk_ai_session_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ai_session_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. AI面试消息表
CREATE TABLE IF NOT EXISTS ai_interview_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,  -- USER, AI, SYSTEM
    content TEXT NOT NULL,
    sequence_number INT NOT NULL,
    token_count INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ai_message_session_id (session_id),
    INDEX idx_ai_message_sequence (session_id, sequence_number),
    CONSTRAINT fk_ai_message_session FOREIGN KEY (session_id) REFERENCES ai_interview_session(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. AI面试评分表
CREATE TABLE IF NOT EXISTS ai_interview_score (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL UNIQUE,
    attitude_score INT DEFAULT 0,  -- 处理态度 0-25
    rule_execution_score INT DEFAULT 0,  -- 执行群规能力 0-25
    emotional_control_score INT DEFAULT 0,  -- 情绪控制 0-25
    decision_rationality_score INT DEFAULT 0,  -- 决策合理性 0-25
    final_score INT DEFAULT 0,  -- 总分 0-100
    evaluation TEXT,  -- AI评价
    scored_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ai_score_session_id (session_id),
    CONSTRAINT fk_ai_score_session FOREIGN KEY (session_id) REFERENCES ai_interview_session(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. 成员流转日志表
CREATE TABLE IF NOT EXISTS member_flow_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    from_role VARCHAR(20),
    to_role VARCHAR(20),
    trigger_type VARCHAR(20) COMMENT 'AUTO-自动, MANUAL-手动',
    reason VARCHAR(500),
    approved_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_flow_user_id (user_id),
    INDEX idx_flow_trigger_type (trigger_type),
    INDEX idx_flow_created_at (created_at),
    CONSTRAINT fk_flow_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_flow_approver FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. 月度绩效跟踪表
CREATE TABLE IF NOT EXISTS monthly_performance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    period VARCHAR(7) NOT NULL COMMENT 'YYYY-MM格式',
    total_points INT DEFAULT 0,
    checkin_count INT DEFAULT 0,
    consecutive_low_months INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_perf_user_id (user_id),
    INDEX idx_perf_period (period),
    UNIQUE INDEX uk_user_period (user_id, period),
    CONSTRAINT fk_perf_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 薪酬表增强字段 (ALTER语句，用于已存在的表升级)
-- 如果是新建数据库，建议直接在salaries表CREATE语句中包含这些字段
ALTER TABLE salaries ADD COLUMN IF NOT EXISTS checkin_count INT DEFAULT 0 AFTER deduction;
ALTER TABLE salaries ADD COLUMN IF NOT EXISTS checkin_points INT DEFAULT 0 AFTER checkin_count;
ALTER TABLE salaries ADD COLUMN IF NOT EXISTS checkin_coins INT DEFAULT 0 AFTER checkin_points;
ALTER TABLE salaries ADD COLUMN IF NOT EXISTS pool_allocation DECIMAL(10,2) DEFAULT 0.00 AFTER checkin_coins;
