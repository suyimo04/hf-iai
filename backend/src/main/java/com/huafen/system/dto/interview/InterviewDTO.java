package com.huafen.system.dto.interview;

import com.huafen.system.entity.Interview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 面试记录DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewDTO {

    private Long id;
    private Long userId;
    private String username;
    private Long applicationId;
    private Integer score;
    private String report;
    private String status;
    private LocalDateTime createdAt;

    public static InterviewDTO fromEntity(Interview interview) {
        if (interview == null) {
            return null;
        }
        return InterviewDTO.builder()
                .id(interview.getId())
                .userId(interview.getUser() != null ? interview.getUser().getId() : null)
                .username(interview.getUser() != null ? interview.getUser().getUsername() : null)
                .applicationId(interview.getApplication() != null ? interview.getApplication().getId() : null)
                .score(interview.getScore())
                .report(interview.getReport())
                .status(interview.getStatus() != null ? interview.getStatus().name() : null)
                .createdAt(interview.getCreatedAt())
                .build();
    }
}
