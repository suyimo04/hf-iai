package com.huafen.system.dto.salary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 薪酬分配DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryAllocationDTO {

    private Long userId;
    private String userName;
    private String nickname;
    private Integer coins;
    private Integer totalPoints;
    private String remark;
}
