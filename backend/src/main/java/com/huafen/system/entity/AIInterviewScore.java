package com.huafen.system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AI面试评分实体
 */
@Entity
@Table(name = "ai_interview_score")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIInterviewScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private AIInterviewSession session;

    @Column(name = "attitude_score")
    @Builder.Default
    private Integer attitudeScore = 0;

    @Column(name = "rule_execution_score")
    @Builder.Default
    private Integer ruleExecutionScore = 0;

    @Column(name = "emotional_control_score")
    @Builder.Default
    private Integer emotionalControlScore = 0;

    @Column(name = "decision_rationality_score")
    @Builder.Default
    private Integer decisionRationalityScore = 0;

    @Column(name = "final_score")
    @Builder.Default
    private Integer finalScore = 0;

    @Column(columnDefinition = "TEXT")
    private String evaluation;

    @Column(name = "scored_at")
    @Builder.Default
    private LocalDateTime scoredAt = LocalDateTime.now();
}
