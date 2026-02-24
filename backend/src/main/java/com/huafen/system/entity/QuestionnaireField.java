package com.huafen.system.entity;

import com.huafen.system.entity.enums.FieldType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 问卷字段实体
 */
@Entity
@Table(name = "questionnaire_fields")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionnaire_id", nullable = false)
    private Questionnaire questionnaire;

    @Column(name = "field_key", nullable = false, length = 50)
    private String fieldKey;

    @Column(nullable = false, length = 200)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false, length = 20)
    private FieldType fieldType;

    @Column(columnDefinition = "JSON")
    private String options;

    @Column(name = "validation_rules", columnDefinition = "JSON")
    private String validationRules;

    @Column(nullable = false)
    @Builder.Default
    private Boolean required = false;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "condition_logic", columnDefinition = "JSON")
    private String conditionLogic;

    @Column(name = "group_id")
    private Long groupId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
