package com.huafen.system.service.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 面试评分结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewResult {

    private Integer score;
    private String report;
    private List<QuestionDetail> details;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionDetail {
        private Long questionId;
        private Integer score;
        private Integer maxScore;
        private String feedback;
    }
}
