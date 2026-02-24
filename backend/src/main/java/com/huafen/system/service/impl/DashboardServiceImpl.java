package com.huafen.system.service.impl;

import com.huafen.system.dto.dashboard.DashboardDTO;
import com.huafen.system.dto.dashboard.TrendDataDTO;
import com.huafen.system.entity.enums.ActivityStatus;
import com.huafen.system.entity.enums.ApplicationStatus;
import com.huafen.system.entity.enums.Role;
import com.huafen.system.repository.ActivityRepository;
import com.huafen.system.repository.ApplicationRepository;
import com.huafen.system.repository.PointRepository;
import com.huafen.system.repository.UserRepository;
import com.huafen.system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 数据看板服务实现
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final ActivityRepository activityRepository;
    private final PointRepository pointRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public DashboardDTO getStats() {
        // 用户总数
        long userCount = userRepository.count();

        // 正式成员数（ADMIN, LEADER, VICE_LEADER, MEMBER）
        List<Role> memberRoles = Arrays.asList(Role.ADMIN, Role.LEADER, Role.VICE_LEADER, Role.MEMBER);
        long memberCount = userRepository.countByRoleIn(memberRoles);

        // 报名总数
        long applicationCount = applicationRepository.count();

        // 待审核报名数
        long pendingApplicationCount = applicationRepository.countByStatus(ApplicationStatus.PENDING);

        // 活动总数
        long activityCount = activityRepository.count();

        // 进行中活动数（PUBLISHED 和 ONGOING）
        List<ActivityStatus> activeStatuses = Arrays.asList(ActivityStatus.PUBLISHED, ActivityStatus.ONGOING);
        long activeActivityCount = activityRepository.countByStatusIn(activeStatuses);

        // 总积分
        Long totalPoints = pointRepository.sumAllPoints();

        // 本月积分
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        Long monthlyPoints = pointRepository.sumPointsSince(monthStart);

        return DashboardDTO.builder()
                .userCount(userCount)
                .memberCount(memberCount)
                .applicationCount(applicationCount)
                .pendingApplicationCount(pendingApplicationCount)
                .activityCount(activityCount)
                .activeActivityCount(activeActivityCount)
                .totalPoints(totalPoints != null ? totalPoints : 0L)
                .monthlyPoints(monthlyPoints != null ? monthlyPoints : 0L)
                .build();
    }

    @Override
    public List<TrendDataDTO> getUserTrend(int days) {
        LocalDateTime startDate = LocalDate.now().minusDays(days - 1).atStartOfDay();
        List<Object[]> results = userRepository.countByDateRange(startDate);
        return buildTrendData(results, days);
    }

    @Override
    public List<TrendDataDTO> getApplicationTrend(int days) {
        LocalDateTime startDate = LocalDate.now().minusDays(days - 1).atStartOfDay();
        List<Object[]> results = applicationRepository.countByDateRange(startDate);
        return buildTrendData(results, days);
    }

    @Override
    public List<TrendDataDTO> getPointsTrend(int days) {
        LocalDateTime startDate = LocalDate.now().minusDays(days - 1).atStartOfDay();
        List<Object[]> results = pointRepository.sumByDateRange(startDate);
        return buildTrendData(results, days);
    }

    /**
     * 构建趋势数据，填充缺失的日期
     */
    private List<TrendDataDTO> buildTrendData(List<Object[]> results, int days) {
        // 将查询结果转为Map
        Map<String, Long> dataMap = new HashMap<>();
        for (Object[] row : results) {
            String date = row[0].toString();
            Long value = ((Number) row[1]).longValue();
            dataMap.put(date, value);
        }

        // 生成完整的日期序列
        List<TrendDataDTO> trendList = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(DATE_FORMATTER);
            Long value = dataMap.getOrDefault(dateStr, 0L);
            trendList.add(TrendDataDTO.builder()
                    .date(dateStr)
                    .value(value)
                    .build());
        }

        return trendList;
    }
}
