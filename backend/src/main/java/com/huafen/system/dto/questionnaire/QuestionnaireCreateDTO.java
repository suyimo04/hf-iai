package com.huafen.system.dto.questionnaire;

import com.huafen.system.entity.enums.AccessType;
import com.huafen.system.entity.enums.FieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建问卷请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireCreateDTO {

    @NotBlank(message = "问卷标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "访问类型不能为空")
    private AccessType accessType;

    private List<FieldCreateDTO> fields;

    /**
     * 字段创建DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldCreateDTO {

        @NotBlank(message = "字段key不能为空")
        private String fieldKey;

        @NotBlank(message = "字段标签不能为空")
        private String label;

        @NotNull(message = "字段类型不能为空")
        private FieldType fieldType;

        private String options;
        private String validationRules;
        private Boolean required;
        private Integer sortOrder;
        private String conditionLogic;
        private Long groupId;
    }
}
