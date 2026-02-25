package com.huafen.system.controller;

import com.huafen.system.common.Result;
import com.huafen.system.dto.log.LogQueryRequest;
import com.huafen.system.dto.log.OperationLogDTO;
import com.huafen.system.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
public class LogController {

    private final OperationLogService operationLogService;

    /**
     * 分页查询操作日志
     */
    @GetMapping
    public Result<Page<OperationLogDTO>> list(LogQueryRequest request) {
        Page<OperationLogDTO> logs = operationLogService.getLogs(request);
        return Result.success(logs);
    }
}
