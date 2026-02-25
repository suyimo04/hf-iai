package com.huafen.system.dto.interview;

import com.huafen.system.entity.InterviewQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 面试题目DTO（不含答案）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewQuestionDTO {

    private Long id;
    private String category;
    private String question;
    private String options;
    private Integer score;
    private Integer sortOrder;

    /**
     * 从实体转换（不包含答案）
     */
    public static InterviewQuestionDTO fromEntity(InterviewQuestion entity) {
        if (entity == null) {
            return null;
        }
        return InterviewQuestionDTO.builder()
                .id(entity.getId())
                .category(entity.getCategory())
                .question(entity.getQuestion())
                .options(entity.getOptions())
                .score(entity.getScore())
                .sortOrder(entity.getSortOrder())
                .build();
    }

    /**
     * 从实体转换（包含答案，用于管理端）
     */
    public static InterviewQuestionDTO fromEntityWithAnswer(InterviewQuestion entity) {
        if (entity == null) {
            return null;
        }
        InterviewQuestionDTO dto = fromEntity(entity);
        return dto;
    }
}
