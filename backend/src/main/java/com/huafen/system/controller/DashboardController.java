package com.huafen.system.controller;

import com.huafen.system.common.Result;
import com.huafen.system.dto.dashboard.DashboardDTO;
import com.huafen.system.dto.dashboard.TrendDataDTO;
import com.huafen.system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据看板控制器
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取统计数据
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER', 'VICE_LEADER')")
    public Result<DashboardDTO> getStats() {
        return Result.success(dashboardService.getStats());
    }

    /**
     * 获取用户注册趋势
     */
    @GetMapping("/trend/user")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER', 'VICE_LEADER')")
    public Result<List<TrendDataDTO>> getUserTrend(@RequestParam(defaultValue = "7") int days) {
        return Result.success(dashboardService.getUserTrend(days));
    }

    /**
     * 获取报名趋势
     */
    @GetMapping("/trend/application")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER', 'VICE_LEADER')")
    public Result<List<TrendDataDTO>> getApplicationTrend(@RequestParam(defaultValue = "7") int days) {
        return Result.success(dashboardService.getApplicationTrend(days));
    }

    /**
     * 获取积分发放趋势
     */
    @GetMapping("/trend/points")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER', 'VICE_LEADER')")
    public Result<List<TrendDataDTO>> getPointsTrend(@RequestParam(defaultValue = "7") int days) {
        return Result.success(dashboardService.getPointsTrend(days));
    }
}
