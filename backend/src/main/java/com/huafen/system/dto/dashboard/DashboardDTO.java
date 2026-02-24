package com.huafen.system.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 看板统计数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {

    /**
     * 用户总数
     */
    private Long userCount;

    /**
     * 正式成员数
     */
    private Long memberCount;

    /**
     * 报名总数
     */
    private Long applicationCount;

    /**
     * 待审核报名数
     */
    private Long pendingApplicationCount;

    /**
     * 活动总数
     */
    private Long activityCount;

    /**
     * 进行中活动数
     */
    private Long activeActivityCount;

    /**
     * 总积分
     */
    private Long totalPoints;

    /**
     * 本月积分
     */
    private Long monthlyPoints;
}
