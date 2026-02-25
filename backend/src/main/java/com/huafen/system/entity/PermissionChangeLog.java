package com.huafen.system.entity;

import com.huafen.system.entity.enums.ChangeType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 权限变更日志实体
 * 用于记录角色权限的变更历史
 */
@Entity
@Table(name = "permission_change_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 角色标识
     */
    @Column(nullable = false, length = 20)
    private String role;

    /**
     * 变更类型: MENU_GRANT, MENU_REVOKE, PERM_GRANT, PERM_REVOKE
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 20)
    private ChangeType changeType;

    /**
     * 目标ID (菜单ID或权限ID)
     */
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    /**
     * 目标名称
     */
    @Column(name = "target_name", length = 100)
    private String targetName;

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
