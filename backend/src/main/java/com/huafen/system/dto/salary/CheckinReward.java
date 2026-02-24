package com.huafen.system.dto.salary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 签到奖励DTO
 * 包含积分和迷你币奖励
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckinReward {

    /**
     * 积分奖励（可为负数表示扣除）
     */
    private int points;

    /**
     * 迷你币奖励（可为负数表示扣除）
     */
    private int coins;
}
