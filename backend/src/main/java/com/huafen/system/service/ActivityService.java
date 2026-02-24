package com.huafen.system.service;

import com.huafen.system.dto.activity.*;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 活动服务接口
 */
public interface ActivityService {

    /**
     * 分页查询活动列表
     */
    Page<ActivityDTO> getActivities(ActivityQueryRequest request);

    /**
     * 根据ID获取活动详情
     */
    ActivityDTO getById(Long id);

    /**
     * 创建活动
     */
    ActivityDTO create(ActivityCreateRequest request);

    /**
     * 更新活动
     */
    ActivityDTO update(Long id, ActivityUpdateRequest request);

    /**
     * 删除活动
     */
    void delete(Long id);

    /**
     * 报名活动
     */
    void signup(Long activityId);

    /**
     * 签到（管理员操作）
     */
    void signin(Long activityId, Long userId);

    /**
     * 获取活动报名列表
     */
    List<ActivitySignupDTO> getSignups(Long activityId);
}
