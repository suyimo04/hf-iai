package com.huafen.system.dto.interview;

import com.huafen.system.entity.AIInterviewScore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI面试评分DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIInterviewScoreDTO {

    private Long id;
    private Long sessionId;
    private Integer attitudeScore;
    private Integer ruleExecutionScore;
    private Integer emotionalControlScore;
    private Integer decisionRationalityScore;
    private Integer finalScore;
    private String evaluation;
    private LocalDateTime scoredAt;

    public static AIInterviewScoreDTO fromEntity(AIInterviewScore score) {
        if (score == null) {
            return null;
        }
        return AIInterviewScoreDTO.builder()
                .id(score.getId())
                .sessionId(score.getSession() != null ? score.getSession().getId() : null)
                .attitudeScore(score.getAttitudeScore())
                .ruleExecutionScore(score.getRuleExecutionScore())
                .emotionalControlScore(score.getEmotionalControlScore())
                .decisionRationalityScore(score.getDecisionRationalityScore())
                .finalScore(score.getFinalScore())
                .evaluation(score.getEvaluation())
                .scoredAt(score.getScoredAt())
                .build();
    }
}
