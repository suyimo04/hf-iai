package com.huafen.system.dto.application;

import com.huafen.system.entity.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审核请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationReviewRequest {

    @NotNull(message = "审核状态不能为空")
    private ApplicationStatus status;

    private String comment;
}
