package com.huafen.system.dto.activity;

import com.huafen.system.entity.enums.ActivityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 活动查询请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityQueryRequest {

    private ActivityStatus status;
    private String keyword;
    private Integer page = 0;
    private Integer size = 10;
}
