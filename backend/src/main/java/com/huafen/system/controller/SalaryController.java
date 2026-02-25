package com.huafen.system.controller;

import com.huafen.system.common.Result;
import com.huafen.system.dto.salary.*;
import com.huafen.system.service.SalaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 薪酬控制器
 */
@RestController
@RequestMapping("/api/salaries")
@RequiredArgsConstructor
public class SalaryController {

    private final SalaryService salaryService;

    /**
     * 分页查询薪酬列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER', 'VICE_LEADER', 'MEMBER')")
    public Result<Page<SalaryDTO>> list(SalaryQueryRequest request) {
        return Result.success(salaryService.getSalaries(request));
    }

    /**
     * 获取当前用户的薪酬记录
     */
    @GetMapping("/my")
    public Result<List<SalaryDTO>> getMySalaries() {
        return Result.success(salaryService.getMySalaries());
    }

    /**
     * 编辑单条薪酬记录
     */
    @PostMapping("/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public Result<SalaryDTO> edit(@RequestBody SalaryEditRequest request) {
        return Result.success(salaryService.edit(request));
    }

    /**
     * 校验批量保存请求
     */
    @PostMapping("/validate")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public Result<SalaryValidationResult> validate(@RequestBody SalaryBatchSaveRequest request) {
        return Result.success(salaryService.validate(request));
    }

    /**
     * 批量保存薪酬记录
     */
    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public Result<List<SalaryDTO>> batchSave(@RequestBody SalaryBatchSaveRequest request) {
        return Result.success(salaryService.batchSave(request));
    }

    /**
     * 生成月度薪酬记录
     */
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public Result<Void> generate(@RequestParam String period) {
        salaryService.generateMonthlySalary(period);
        return Result.success();
    }
}
