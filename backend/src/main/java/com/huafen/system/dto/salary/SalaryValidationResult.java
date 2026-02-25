package com.huafen.system.dto.salary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 薪酬校验结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryValidationResult {

    private Boolean valid;
    private List<String> errors;
    private BigDecimal totalSalary;
}
