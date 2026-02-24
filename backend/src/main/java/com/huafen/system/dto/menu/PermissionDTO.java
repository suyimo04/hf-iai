package com.huafen.system.dto.menu;

import com.huafen.system.entity.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO {

    private Long id;
    private Long menuId;
    private String menuName;
    private String name;
    private String code;
    private String description;
    private LocalDateTime createdAt;

    public static PermissionDTO fromEntity(Permission permission) {
        return PermissionDTO.builder()
                .id(permission.getId())
                .menuId(permission.getMenu() != null ? permission.getMenu().getId() : null)
                .menuName(permission.getMenu() != null ? permission.getMenu().getName() : null)
                .name(permission.getName())
                .code(permission.getCode())
                .description(permission.getDescription())
                .createdAt(permission.getCreatedAt())
                .build();
    }
}
