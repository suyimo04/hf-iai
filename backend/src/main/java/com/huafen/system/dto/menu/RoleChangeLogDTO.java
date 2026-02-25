package com.huafen.system.dto.menu;

import com.huafen.system.entity.RoleChangeLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleChangeLogDTO {

    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String fromRole;
    private String toRole;
    private String reason;
    private Long changedBy;
    private String ipAddress;
    private LocalDateTime createdAt;

    public static RoleChangeLogDTO fromEntity(RoleChangeLog log) {
        return RoleChangeLogDTO.builder()
                .id(log.getId())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .username(log.getUser() != null ? log.getUser().getUsername() : null)
                .nickname(log.getUser() != null ? log.getUser().getNickname() : null)
                .fromRole(log.getFromRole())
                .toRole(log.getToRole())
                .reason(log.getReason())
                .changedBy(log.getChangedBy())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
