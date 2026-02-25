package com.huafen.system.dto.salary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 薪酬池汇总DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryPoolSummaryDTO {

    private String period;
    private Integer totalPool;
    private Integer allocatedCoins;
    private Integer remainingCoins;
    private Integer memberCount;
    private Integer minPerPerson;
    private Integer maxPerPerson;
    private List<SalaryAllocationDTO> allocations;
}
