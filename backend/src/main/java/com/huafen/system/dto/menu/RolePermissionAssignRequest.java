package com.huafen.system.dto.menu;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionAssignRequest {

    @NotBlank(message = "角色不能为空")
    private String role;

    @NotNull(message = "权限ID列表不能为空")
    private List<Long> permissionIds;
}
