package com.huafen.system.dto.interview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 面试统计DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewStatisticsDTO {

    /**
     * 面试总数
     */
    private Long totalCount;

    /**
     * 通过数
     */
    private Long passedCount;

    /**
     * 未通过数
     */
    private Long failedCount;

    /**
     * 进行中数量
     */
    private Long inProgressCount;

    /**
     * 待开始数量
     */
    private Long pendingCount;

    /**
     * 通过率 (0-100)
     */
    private Double passRate;

    /**
     * 平均分
     */
    private Double averageScore;

    /**
     * 最高分
     */
    private Integer maxScore;

    /**
     * 最低分
     */
    private Integer minScore;
}
