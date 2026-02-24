package com.huafen.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 配置变更日志实体
 * 用于记录系统配置的修改历史
 */
@Entity
@Table(name = "config_change_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 配置分组: AI, OSS, EMAIL, SYSTEM
     */
    @Column(name = "config_group", length = 20)
    private String configGroup;

    /**
     * 配置键
     */
    @Column(name = "config_key", nullable = false, length = 50)
    private String configKey;

    /**
     * 旧值
     */
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    /**
     * 新值
     */
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    /**
     * 修改人ID
     */
    @Column(name = "changed_by")
    private Long changedBy;

    /**
     * 操作IP地址
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
