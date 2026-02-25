package com.huafen.system.dto.salary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户签到奖励DTO
 * 包含用户信息和签到奖励详情
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCheckinReward {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 月度签到次数
     */
    private int checkinCount;

    /**
     * 积分奖励
     */
    private int points;

    /**
     * 迷你币奖励
     */
    private int coins;

    /**
     * 统计期间（格式：yyyy-MM）
     */
    private String period;
}
