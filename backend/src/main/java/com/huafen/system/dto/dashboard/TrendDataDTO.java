package com.huafen.system.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 趋势数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendDataDTO {

    /**
     * 日期
     */
    private String date;

    /**
     * 数值
     */
    private Long value;
}
