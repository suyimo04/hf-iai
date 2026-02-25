package com.huafen.system.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 面试题库实体
 */
@Entity
@Table(name = "interview_questions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String category;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(name = "question_type", length = 20, nullable = false)
    private String questionType;

    @Column(columnDefinition = "JSON")
    private String options;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Column
    private Integer score;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column
    private Boolean enabled;
}
