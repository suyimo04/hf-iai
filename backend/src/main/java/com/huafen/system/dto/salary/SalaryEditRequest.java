package com.huafen.system.dto.salary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单条薪酬编辑请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryEditRequest {

    private Long id;
    private Integer basePoints;
    private Integer bonusPoints;
    private Integer deduction;
    private Integer coins;
    private String remark;
}
