package com.huafen.system.dto.salary;

import com.huafen.system.entity.Salary;
import com.huafen.system.entity.enums.SalaryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 薪酬记录DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryDTO {

    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String period;
    private Integer basePoints;
    private Integer bonusPoints;
    private Integer deduction;
    private Integer totalPoints;
    private Integer coins;
    private BigDecimal salary;
    private String remark;
    private SalaryStatus status;
    private LocalDateTime createdAt;

    public static SalaryDTO fromEntity(Salary salary) {
        return SalaryDTO.builder()
                .id(salary.getId())
                .userId(salary.getUser().getId())
                .username(salary.getUser().getUsername())
                .nickname(salary.getUser().getNickname())
                .period(salary.getPeriod())
                .basePoints(salary.getBasePoints())
                .bonusPoints(salary.getBonusPoints())
                .deduction(salary.getDeduction())
                .totalPoints(salary.getTotalPoints())
                .coins(salary.getCoins())
                .salary(salary.getSalary())
                .remark(salary.getRemark())
                .status(salary.getStatus())
                .createdAt(salary.getCreatedAt())
                .build();
    }
}
