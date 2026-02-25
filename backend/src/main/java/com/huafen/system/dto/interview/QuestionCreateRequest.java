package com.huafen.system.dto.interview;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建题目请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionCreateRequest {

    private String category;

    @NotBlank(message = "题目内容不能为空")
    private String question;

    private String options;

    private String answer;

    private Integer score;

    private Integer sortOrder;

    private Boolean enabled;
}
