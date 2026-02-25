package com.huafen.system.dto.interview;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket开始AI面试请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartInterviewRequest {

    @NotNull(message = "申请ID不能为空")
    private Long applicationId;
}
