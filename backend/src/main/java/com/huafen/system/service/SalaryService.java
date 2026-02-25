package com.huafen.system.service;

import com.huafen.system.dto.salary.*;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 薪酬服务接口
 */
public interface SalaryService {

    /**
     * 分页查询薪酬记录
     */
    Page<SalaryDTO> getSalaries(SalaryQueryRequest request);

    /**
     * 根据ID获取薪酬记录
     */
    SalaryDTO getById(Long id);

    /**
     * 编辑单条薪酬记录
     */
    SalaryDTO edit(SalaryEditRequest request);

    /**
     * 校验批量保存请求
     */
    SalaryValidationResult validate(SalaryBatchSaveRequest request);

    /**
     * 批量保存薪酬记录
     */
    List<SalaryDTO> batchSave(SalaryBatchSaveRequest request);

    /**
     * 生成月度薪酬记录
     */
    void generateMonthlySalary(String period);

    /**
     * 获取当前用户的薪酬记录
     */
    List<SalaryDTO> getMySalaries();
}
