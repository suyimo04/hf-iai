package com.huafen.system.dto.questionnaire;

import com.huafen.system.entity.enums.AccessType;
import com.huafen.system.entity.enums.QuestionnaireStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 问卷查询参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireQuery {

    private QuestionnaireStatus status;
    private AccessType accessType;
    private String keyword;
    private Integer page = 0;
    private Integer size = 10;
}
