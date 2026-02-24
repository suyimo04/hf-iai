package com.huafen.system.dto.menu;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionCreateRequest {

    private Long menuId;

    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称不能超过50个字符")
    private String name;

    @NotBlank(message = "权限编码不能为空")
    @Size(max = 100, message = "权限编码不能超过100个字符")
    private String code;

    @Size(max = 200, message = "描述不能超过200个字符")
    private String description;
}
