package com.huafen.system.dto.log;

import com.huafen.system.entity.OperationLog;
import com.huafen.system.entity.enums.LogCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 操作日志DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogDTO {

    private Long id;
    private Long userId;
    private String username;
    private LogCategory category;
    private String action;
    private String targetType;
    private Long targetId;
    private String detail;
    private String ip;
    private LocalDateTime createdAt;

    /**
     * 从实体转换为DTO
     */
    public static OperationLogDTO fromEntity(OperationLog log) {
        if (log == null) {
            return null;
        }
        return OperationLogDTO.builder()
                .id(log.getId())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .username(log.getUser() != null ? log.getUser().getUsername() : null)
                .category(log.getCategory())
                .action(log.getAction())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .detail(log.getDetail())
                .ip(log.getIp())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
