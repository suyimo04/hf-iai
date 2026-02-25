package com.huafen.system.entity;

import com.huafen.system.entity.enums.ResponseStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 问卷响应实体
 */
@Entity
@Table(name = "questionnaire_responses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionnaire_id", nullable = false)
    private Questionnaire questionnaire;

    @Column(name = "questionnaire_version")
    private Integer questionnaireVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "respondent_info", columnDefinition = "JSON")
    private String respondentInfo;

    @Column(nullable = false, columnDefinition = "JSON")
    private String answers;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResponseStatus status;

    @Column(name = "auto_created_user_id")
    private Long autoCreatedUserId;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;
}
