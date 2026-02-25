package com.huafen.system.dto.point;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户积分汇总
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPointSummary {

    private Long userId;
    private String username;
    private Integer totalPoints;
    private Integer checkinPoints;
    private Integer taskPoints;
    private Integer rewardPoints;
    private Integer deductionPoints;
    private Integer activityPoints;
}
