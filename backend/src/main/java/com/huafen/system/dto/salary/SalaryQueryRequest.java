package com.huafen.system.dto.salary;

import com.huafen.system.entity.enums.SalaryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 薪酬查询请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryQueryRequest {

    private String period;
    private SalaryStatus status;
    private Integer page = 0;
    private Integer size = 10;
}
