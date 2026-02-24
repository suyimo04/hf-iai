package com.huafen.system.service.impl;

import com.huafen.system.dto.salary.CheckinReward;
import com.huafen.system.dto.salary.UserCheckinReward;
import com.huafen.system.entity.User;
import com.huafen.system.entity.enums.PointType;
import com.huafen.system.entity.enums.UserStatus;
import com.huafen.system.repository.PointRepository;
import com.huafen.system.repository.UserRepository;
import com.huafen.system.service.CheckinCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 签到积分计算服务实现
 */
@Service
@RequiredArgsConstructor
public class CheckinCalculationServiceImpl implements CheckinCalculationService {

    private final PointRepository pointRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

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
     */
    @Override
    public CheckinReward calculateCheckinReward(int checkinCount) {
        // 处理负数情况
        if (checkinCount < 0) {
            checkinCount = 0;
        }

        if (checkinCount < 20) {
            return new CheckinReward(-20, -40);
        } else if (checkinCount < 30) {
            return new CheckinReward(-10, -20);
        } else if (checkinCount < 40) {
            return new CheckinReward(0, 0);
        } else if (checkinCount < 50) {
            return new CheckinReward(30, 60);
        } else {
            return new CheckinReward(50, 100);
        }
    }

    @Override
    public int getMonthlyCheckinCount(Long userId, String period) {
        // 解析期间字符串为起止时间
        YearMonth yearMonth = YearMonth.parse(period, PERIOD_FORMATTER);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        return pointRepository.countByUserIdAndTypeAndCreatedAtBetween(
                userId, PointType.CHECKIN, startDate, endDate);
    }

    @Override
    public List<UserCheckinReward> calculateAllMembersCheckinReward(String period) {
        // 获取所有正式成员
        List<User> activeUsers = userRepository.findByStatus(UserStatus.ACTIVE);

        List<UserCheckinReward> rewards = new ArrayList<>();

        for (User user : activeUsers) {
            // 获取用户月度签到次数
            int checkinCount = getMonthlyCheckinCount(user.getId(), period);

            // 计算奖励
            CheckinReward reward = calculateCheckinReward(checkinCount);

            // 构建用户签到奖励对象
            UserCheckinReward userReward = UserCheckinReward.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .checkinCount(checkinCount)
                    .points(reward.getPoints())
                    .coins(reward.getCoins())
                    .period(period)
                    .build();

            rewards.add(userReward);
        }

        return rewards;
    }
}
