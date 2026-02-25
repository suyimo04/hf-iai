package com.huafen.system.service;

import com.huafen.system.dto.application.ApplicationDTO;
import com.huafen.system.dto.application.ApplicationQueryRequest;
import com.huafen.system.dto.application.ApplicationReviewRequest;
import com.huafen.system.dto.application.ApplicationSubmitRequest;
import org.springframework.data.domain.Page;

/**
 * 报名服务接口
 */
public interface ApplicationService {

    /**
     * 提交报名
     */
    ApplicationDTO submit(ApplicationSubmitRequest request);

    /**
     * 分页查询报名列表
     */
    Page<ApplicationDTO> getApplications(ApplicationQueryRequest request);

    /**
     * 根据ID获取报名详情
     */
    ApplicationDTO getById(Long id);

    /**
     * 审核报名
     */
    ApplicationDTO review(Long id, ApplicationReviewRequest request);

    /**
     * 获取当前用户的报名信息
     */
    ApplicationDTO getMyApplication();
}
