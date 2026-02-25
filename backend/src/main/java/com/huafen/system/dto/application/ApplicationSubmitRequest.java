package com.huafen.system.dto.application;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 提交报名请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationSubmitRequest {

    @NotNull(message = "表单数据不能为空")
    private Map<String, Object> formData;
}
