package com.huafen.system.controller;

import com.huafen.system.common.Result;
import com.huafen.system.dto.application.ApplicationDTO;
import com.huafen.system.dto.application.ApplicationQueryRequest;
import com.huafen.system.dto.application.ApplicationReviewRequest;
import com.huafen.system.dto.application.ApplicationSubmitRequest;
import com.huafen.system.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 报名控制器
 */
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * 提交报名
     */
    @PostMapping
    public Result<ApplicationDTO> submit(@Valid @RequestBody ApplicationSubmitRequest request) {
        ApplicationDTO result = applicationService.submit(request);
        return Result.success(result);
    }

    /**
     * 分页查询报名列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER', 'VICE_LEADER')")
    public Result<Page<ApplicationDTO>> list(ApplicationQueryRequest request) {
        Page<ApplicationDTO> page = applicationService.getApplications(request);
        return Result.success(page);
    }

    /**
     * 根据ID获取报名详情
     */
    @GetMapping("/{id}")
    public Result<ApplicationDTO> getById(@PathVariable Long id) {
        ApplicationDTO result = applicationService.getById(id);
        return Result.success(result);
    }

    /**
     * 获取当前用户的报名信息
     */
    @GetMapping("/my")
    public Result<ApplicationDTO> getMyApplication() {
        ApplicationDTO result = applicationService.getMyApplication();
        return Result.success(result);
    }

    /**
     * 审核报名
     */
    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER', 'VICE_LEADER')")
    public Result<ApplicationDTO> review(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationReviewRequest request) {
        ApplicationDTO result = applicationService.review(id, request);
        return Result.success(result);
    }
}
