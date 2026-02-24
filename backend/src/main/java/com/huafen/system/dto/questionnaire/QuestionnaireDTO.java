package com.huafen.system.dto.questionnaire;

import com.huafen.system.entity.Questionnaire;
import com.huafen.system.entity.enums.AccessType;
import com.huafen.system.entity.enums.QuestionnaireStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 问卷详情DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireDTO {

    private Long id;
    private String title;
    private String description;
    private QuestionnaireStatus status;
    private AccessType accessType;
    private String publicToken;
    private Integer version;
    private Long createdBy;
    private String creatorName;
    private List<QuestionnaireFieldDTO> fields;
    private Long responseCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static QuestionnaireDTO fromEntity(Questionnaire questionnaire) {
        return fromEntity(questionnaire, null);
    }

    public static QuestionnaireDTO fromEntity(Questionnaire questionnaire, Long responseCount) {
        QuestionnaireDTO dto = QuestionnaireDTO.builder()
                .id(questionnaire.getId())
                .title(questionnaire.getTitle())
                .description(questionnaire.getDescription())
                .status(questionnaire.getStatus())
                .accessType(questionnaire.getAccessType())
                .publicToken(questionnaire.getPublicToken())
                .version(questionnaire.getVersion())
                .createdBy(questionnaire.getCreatedBy() != null ? questionnaire.getCreatedBy().getId() : null)
                .creatorName(questionnaire.getCreatedBy() != null ? questionnaire.getCreatedBy().getNickname() : null)
                .responseCount(responseCount)
                .createdAt(questionnaire.getCreatedAt())
                .updatedAt(questionnaire.getUpdatedAt())
                .build();

        if (questionnaire.getFields() != null) {
            dto.setFields(questionnaire.getFields().stream()
                    .map(QuestionnaireFieldDTO::fromEntity)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
