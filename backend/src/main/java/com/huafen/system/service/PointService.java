package com.huafen.system.service;

import com.huafen.system.dto.point.*;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 积分服务接口
 */
public interface PointService {

    /**
     * 添加积分记录
     */
    PointDTO addPoint(PointAddRequest request);

    /**
     * 分页查询积分记录
     */
    Page<PointDTO> getPoints(PointQueryRequest request);

    /**
     * 获取当前用户的积分记录
     */
    List<PointDTO> getMyPoints();

    /**
     * 获取用户积分汇总
     */
    UserPointSummary getUserSummary(Long userId);

    /**
     * 获取用户总积分
     */
    Integer getTotalPoints(Long userId);

    /**
     * 每日签到
     */
    PointDTO checkin();
}
