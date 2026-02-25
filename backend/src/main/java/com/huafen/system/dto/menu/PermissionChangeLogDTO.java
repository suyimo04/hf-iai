package com.huafen.system.dto.menu;

import com.huafen.system.entity.PermissionChangeLog;
import com.huafen.system.entity.enums.ChangeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionChangeLogDTO {

    private Long id;
    private String role;
    private ChangeType changeType;
    private Long targetId;
    private String targetName;
    private Long changedBy;
    private String ipAddress;
    private LocalDateTime createdAt;

    public static PermissionChangeLogDTO fromEntity(PermissionChangeLog log) {
        return PermissionChangeLogDTO.builder()
                .id(log.getId())
                .role(log.getRole())
                .changeType(log.getChangeType())
                .targetId(log.getTargetId())
                .targetName(log.getTargetName())
                .changedBy(log.getChangedBy())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
