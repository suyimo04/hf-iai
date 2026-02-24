package com.huafen.system.service;

import com.huafen.system.dto.dashboard.DashboardDTO;
import com.huafen.system.dto.dashboard.TrendDataDTO;

import java.util.List;

/**
 * 数据看板服务接口
 */
public interface DashboardService {

    /**
     * 获取统计数据
     */
    DashboardDTO getStats();

    /**
     * 获取用户注册趋势
     * @param days 天数
     */
    List<TrendDataDTO> getUserTrend(int days);

    /**
     * 获取报名趋势
     * @param days 天数
     */
    List<TrendDataDTO> getApplicationTrend(int days);

    /**
     * 获取积分发放趋势
     * @param days 天数
     */
    List<TrendDataDTO> getPointsTrend(int days);
}
