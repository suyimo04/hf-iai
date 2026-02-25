package com.huafen.system.service;

import com.huafen.system.dto.salary.SalaryAllocationDTO;
import com.huafen.system.dto.salary.SalaryDTO;
import com.huafen.system.dto.salary.SalaryPoolSummaryDTO;
import com.huafen.system.dto.salary.SalaryValidationResult;

import java.util.List;

/**
 * 薪酬池分配服务接口
 */
public interface SalaryPoolService {

    // 薪酬池常量
    int TOTAL_POOL = 2000;          // 总池
    int MIN_PER_PERSON = 200;       // 单人最小
    int MAX_PER_PERSON = 400;       // 单人最大
    int FORMAL_MEMBER_COUNT = 5;    // 正式成员数

    /**
     * 校验薪酬分配
     */
    SalaryValidationResult validateAllocation(List<SalaryAllocationDTO> allocations);

    /**
     * 批量保存薪酬
     */
    void batchSaveSalaries(String period, List<SalaryAllocationDTO> allocations);

    /**
     * 生成月度薪酬（基于积分自动计算）
     */
    List<SalaryDTO> generateMonthlySalaries(String period);

    /**
     * 获取薪酬池汇总
     */
    SalaryPoolSummaryDTO getPoolSummary(String period);
}
