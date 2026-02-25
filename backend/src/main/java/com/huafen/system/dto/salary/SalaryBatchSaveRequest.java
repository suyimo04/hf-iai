package com.huafen.system.dto.salary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量保存薪酬请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryBatchSaveRequest {

    private List<SalaryEditRequest> items;
    private String period;
}
