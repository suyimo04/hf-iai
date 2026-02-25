package com.huafen.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 角色变更日志实体
 * 用于记录用户角色的变更历史
 */
@Entity
@Table(name = "role_change_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 被变更角色的用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 原角色
     */
    @Column(name = "from_role", length = 20)
    private String fromRole;

    /**
     * 新角色
     */
    @Column(name = "to_role", length = 20)
    private String toRole;

    /**
     * 变更原因
     */
    @Column(length = 500)
    private String reason;

    /**
     * 操作人ID
     */
    @Column(name = "changed_by", nullable = false)
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
