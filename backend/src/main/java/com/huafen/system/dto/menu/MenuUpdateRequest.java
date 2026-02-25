package com.huafen.system.dto.menu;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuUpdateRequest {

    private Long parentId;

    @Size(max = 50, message = "菜单名称不能超过50个字符")
    private String name;

    @Size(max = 200, message = "路径不能超过200个字符")
    private String path;

    @Size(max = 200, message = "组件路径不能超过200个字符")
    private String component;

    @Size(max = 50, message = "图标不能超过50个字符")
    private String icon;

    private Integer sortOrder;

    private Boolean visible;

    private Boolean status;
}
