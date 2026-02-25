package com.huafen.system.service;

import com.huafen.system.dto.salary.CheckinReward;
import com.huafen.system.dto.salary.UserCheckinReward;

import java.util.List;

/**
 * 签到积分计算服务接口
 */
public interface CheckinCalculationService {

    /**
     * 根据签到次数计算奖励
     *
     * 签到规则：
     * | 月签到次数 | 积分 | 迷你币 |
     * | <20       | -20  | -40   |
     * | 20-29     | -10  | -20   |
     * | 30-39     | 0    | 0     |
     * | 40-49     | +30  | +60   |
     * | ≥50       | +50  | +100  |
     *
     * @param checkinCount 签到次数
     * @return 签到奖励（积分和迷你币）
     */
    CheckinReward calculateCheckinReward(int checkinCount);

    /**
     * 获取用户月度签到次数
     *
     * @param userId 用户ID
     * @param period 统计期间（格式：yyyy-MM）
     * @return 签到次数
     */
    int getMonthlyCheckinCount(Long userId, String period);

    /**
     * 批量计算所有成员的签到奖励
     *
     * @param period 统计期间（格式：yyyy-MM）
     * @return 所有成员的签到奖励列表
     */
    List<UserCheckinReward> calculateAllMembersCheckinReward(String period);
}
