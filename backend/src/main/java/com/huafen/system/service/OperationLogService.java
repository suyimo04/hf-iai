package com.huafen.system.service;

import com.huafen.system.dto.log.LogQueryRequest;
import com.huafen.system.dto.log.OperationLogDTO;
import com.huafen.system.entity.enums.LogCategory;
import org.springframework.data.domain.Page;

/**
 * 操作日志服务接口
 */
public interface OperationLogService {

    /**
     * 记录操作日志（带分类）
     */
    void log(LogCategory category, String action, String targetType, Long targetId, String detail);

    /**
     * 记录操作日志（兼容旧方法）
     */
    void log(String action, String targetType, Long targetId, String detail);

    /**
     * 分页查询日志
     */
    Page<OperationLogDTO> getLogs(LogQueryRequest request);
}
