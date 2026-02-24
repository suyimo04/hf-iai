package com.huafen.system.dto.menu;

import com.huafen.system.entity.Menu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuDTO {

    private Long id;
    private Long parentId;
    private String name;
    private String path;
    private String component;
    private String icon;
    private Integer sortOrder;
    private Boolean visible;
    private Boolean status;
    private LocalDateTime createdAt;

    @Builder.Default
    private List<MenuDTO> children = new ArrayList<>();

    public static MenuDTO fromEntity(Menu menu) {
        return MenuDTO.builder()
                .id(menu.getId())
                .parentId(menu.getParentId())
                .name(menu.getName())
                .path(menu.getPath())
                .component(menu.getComponent())
                .icon(menu.getIcon())
                .sortOrder(menu.getSortOrder())
                .visible(menu.getVisible())
                .status(menu.getStatus())
                .createdAt(menu.getCreatedAt())
                .build();
    }
}
