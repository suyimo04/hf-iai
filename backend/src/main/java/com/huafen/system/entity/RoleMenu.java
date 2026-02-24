package com.huafen.system.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 角色菜单关联实体
 */
@Entity
@Table(name = "role_menu", uniqueConstraints = {
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

    /**
     * 角色标识（字符串形式，如 ADMIN, MEMBER 等）
     */
    @Column(nullable = false, length = 20)
    private String role;

    /**
     * 关联的菜单
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;
}
