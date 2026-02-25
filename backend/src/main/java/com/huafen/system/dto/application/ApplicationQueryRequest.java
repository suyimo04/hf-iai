package com.huafen.system.dto.application;

import com.huafen.system.entity.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationQueryRequest {

    private ApplicationStatus status;
    private String keyword;
    private Integer page = 0;
    private Integer size = 10;
}
