package com.huafen.system.dto.log;

import com.huafen.system.entity.enums.LogCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 日志查询请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogQueryRequest {

    private Long userId;
    private LogCategory category;
    private String action;
    private String targetType;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;
}
