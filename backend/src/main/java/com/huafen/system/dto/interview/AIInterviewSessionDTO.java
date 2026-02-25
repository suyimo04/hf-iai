package com.huafen.system.dto.interview;

import com.huafen.system.entity.AIInterviewSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI面试会话DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIInterviewSessionDTO {

    private Long id;
    private Long userId;
    private String username;
    private Long applicationId;
    private String status;
    private String violationTypes;
    private Integer roundCount;
    private Integer maxRounds;
    private String aiProvider;
    private String aiModel;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;

    /**
     * 评分信息（可选）
     */
    private AIInterviewScoreDTO score;

    public static AIInterviewSessionDTO fromEntity(AIInterviewSession session) {
        if (session == null) {
            return null;
        }
        return AIInterviewSessionDTO.builder()
                .id(session.getId())
                .userId(session.getUser() != null ? session.getUser().getId() : null)
                .username(session.getUser() != null ? session.getUser().getUsername() : null)
                .applicationId(session.getApplication() != null ? session.getApplication().getId() : null)
                .status(session.getStatus() != null ? session.getStatus().name() : null)
                .violationTypes(session.getViolationTypes())
                .roundCount(session.getRoundCount())
                .maxRounds(session.getMaxRounds())
                .aiProvider(session.getAiProvider())
                .aiModel(session.getAiModel())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .expiredAt(session.getExpiredAt())
                .createdAt(session.getCreatedAt())
                .build();
    }
}
