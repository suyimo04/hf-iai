package com.huafen.system.dto.questionnaire;

import com.huafen.system.entity.QuestionnaireField;
import com.huafen.system.entity.enums.FieldType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 问卷字段DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireFieldDTO {

    private Long id;
    private String fieldKey;
    private String label;
    private FieldType fieldType;
    private String options;
    private String validationRules;
    private Boolean required;
    private Integer sortOrder;
    private String conditionLogic;
    private Long groupId;
    private LocalDateTime createdAt;

    public static QuestionnaireFieldDTO fromEntity(QuestionnaireField field) {
        return QuestionnaireFieldDTO.builder()
                .id(field.getId())
                .fieldKey(field.getFieldKey())
                .label(field.getLabel())
                .fieldType(field.getFieldType())
                .options(field.getOptions())
                .validationRules(field.getValidationRules())
                .required(field.getRequired())
                .sortOrder(field.getSortOrder())
                .conditionLogic(field.getConditionLogic())
                .groupId(field.getGroupId())
                .createdAt(field.getCreatedAt())
                .build();
    }
}
