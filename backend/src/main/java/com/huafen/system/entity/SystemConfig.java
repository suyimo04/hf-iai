package com.huafen.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 系统配置实体
 */
@Entity
@Table(name = "system_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 配置分组: AI, OSS, EMAIL, SYSTEM
     */
    @Column(name = "config_group", length = 20)
    private String configGroup;

    @Column(name = "config_key", nullable = false, unique = true, length = 50)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    /**
     * 是否加密存储
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean encrypted = false;

    @Column(length = 255)
    private String description;

    /**
     * 更新人ID
     */
    @Column(name = "updated_by")
    private Long updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
