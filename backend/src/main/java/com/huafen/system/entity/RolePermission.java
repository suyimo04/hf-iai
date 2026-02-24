package com.huafen.system.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 角色权限关联实体
 */
@Entity
@Table(name = "role_permission", uniqueConstraints = {
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

    /**
     * 角色标识（字符串形式，如 ADMIN, MEMBER 等）
     */
    @Column(nullable = false, length = 20)
    private String role;

    /**
     * 关联的权限
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;
}
