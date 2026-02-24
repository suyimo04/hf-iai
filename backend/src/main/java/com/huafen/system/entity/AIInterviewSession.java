package com.huafen.system.entity;

import com.huafen.system.entity.enums.AIInterviewStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * AI面试会话实体
 */
@Entity
@Table(name = "ai_interview_session")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIInterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private AIInterviewStatus status = AIInterviewStatus.PENDING;

    @Column(name = "violation_types", columnDefinition = "JSON")
    private String violationTypes;

    @Column(name = "round_count")
    @Builder.Default
    private Integer roundCount = 0;

    @Column(name = "max_rounds")
    @Builder.Default
    private Integer maxRounds = 15;

    @Column(name = "ai_provider", length = 50)
    private String aiProvider;

    @Column(name = "ai_model", length = 100)
    private String aiModel;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
