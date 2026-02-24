package com.huafen.system.service.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI面试评分结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIInterviewScore {

    /**
     * 总分 (0-100)
     */
    private Integer score;

    /**
     * 评价报告
     */
    private String report;

    /**
     * 各维度评分
     */
    private List<DimensionScore> dimensions;

    /**
     * 建议
     */
    private String suggestion;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DimensionScore {
        private String dimension;
        private Integer score;
        private String feedback;
    }
}
