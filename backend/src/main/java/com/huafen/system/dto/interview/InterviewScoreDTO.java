package com.huafen.system.dto.interview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 面试评分DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewScoreDTO {

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 沟通能力分数 (0-25)
     */
    private Integer communicationScore;

    /**
     * 问题解决能力分数 (0-25)
     */
    private Integer problemSolvingScore;

    /**
     * 规则理解能力分数 (0-25)
     */
    private Integer ruleUnderstandingScore;

    /**
     * 应变能力分数 (0-25)
     */
    private Integer adaptabilityScore;

    /**
     * 总分 (0-100)
     */
    private Integer totalScore;

    /**
     * AI评价
     */
    private String evaluation;

    /**
     * 是否通过
     */
    private Boolean passed;

    /**
     * 计算总分
     */
    public void calculateTotalScore() {
        this.totalScore = (communicationScore != null ? communicationScore : 0)
                + (problemSolvingScore != null ? problemSolvingScore : 0)
                + (ruleUnderstandingScore != null ? ruleUnderstandingScore : 0)
                + (adaptabilityScore != null ? adaptabilityScore : 0);
    }
}
