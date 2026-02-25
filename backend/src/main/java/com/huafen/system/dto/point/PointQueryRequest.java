package com.huafen.system.dto.point;

import com.huafen.system.entity.enums.PointType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 积分查询请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointQueryRequest {

    private Long userId;
    private PointType type;
    private LocalDate startDate;
    private LocalDate endDate;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 10;
}
