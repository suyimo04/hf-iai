package com.huafen.system.dto.application;

import com.huafen.system.dto.UserDTO;
import com.huafen.system.entity.Application;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 报名信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDTO {

    private Long id;
    private UserDTO user;
    private String status;
    private Map<String, Object> formData;
    private UserDTO reviewer;
    private String reviewComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 从实体转换为DTO
     */
    public static ApplicationDTO fromEntity(Application application) {
        if (application == null) {
            return null;
        }
        return ApplicationDTO.builder()
                .id(application.getId())
                .user(UserDTO.fromEntity(application.getUser()))
                .status(application.getStatus() != null ? application.getStatus().name() : null)
                .reviewComment(application.getReviewComment())
                .reviewer(UserDTO.fromEntity(application.getReviewer()))
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }
}
