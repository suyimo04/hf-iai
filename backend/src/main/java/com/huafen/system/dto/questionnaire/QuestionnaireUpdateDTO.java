package com.huafen.system.dto.questionnaire;

import com.huafen.system.entity.enums.AccessType;
import com.huafen.system.entity.enums.FieldType;
import com.huafen.system.entity.enums.QuestionnaireStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 更新问卷请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireUpdateDTO {

    private String title;
    private String description;
    private AccessType accessType;
    private QuestionnaireStatus status;
    private List<FieldUpdateDTO> fields;

    /**
     * 字段更新DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldUpdateDTO {

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
    }
}
