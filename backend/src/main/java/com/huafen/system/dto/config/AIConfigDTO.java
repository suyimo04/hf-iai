package com.huafen.system.dto.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI配置测试请求DTO
 */
@Data
public class AIConfigDTO {

    /**
     * AI提供者类型 (openai, claude, deepseek, qwen)
     */
    @NotBlank(message = "AI提供者不能为空")
    private String provider;

    /**
     * API密钥
     */
    @NotBlank(message = "API密钥不能为空")
    private String apiKey;

    /**
     * API基础URL
     */
    private String baseUrl;

    /**
     * 模型名称
     */
    private String model;
}
