package com.huafen.system.dto.questionnaire;

import com.huafen.system.entity.QuestionnaireResponse;
import com.huafen.system.entity.enums.ResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 问卷响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireResponseDTO {

    private Long id;
    private Long questionnaireId;
    private String questionnaireTitle;
    private Integer questionnaireVersion;
    private Long userId;
    private String userName;
    private String respondentInfo;
    private String answers;
    private ResponseStatus status;
    private Long autoCreatedUserId;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime submittedAt;

    public static QuestionnaireResponseDTO fromEntity(QuestionnaireResponse response) {
        return QuestionnaireResponseDTO.builder()
                .id(response.getId())
                .questionnaireId(response.getQuestionnaire() != null ? response.getQuestionnaire().getId() : null)
                .questionnaireTitle(response.getQuestionnaire() != null ? response.getQuestionnaire().getTitle() : null)
                .questionnaireVersion(response.getQuestionnaireVersion())
                .userId(response.getUser() != null ? response.getUser().getId() : null)
                .userName(response.getUser() != null ? response.getUser().getNickname() : null)
                .respondentInfo(response.getRespondentInfo())
                .answers(response.getAnswers())
                .status(response.getStatus())
                .autoCreatedUserId(response.getAutoCreatedUserId())
                .ipAddress(response.getIpAddress())
                .userAgent(response.getUserAgent())
                .submittedAt(response.getSubmittedAt())
                .build();
    }
}
