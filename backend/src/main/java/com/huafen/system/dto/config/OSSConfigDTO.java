package com.huafen.system.dto.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * OSS配置测试请求DTO
 */
@Data
public class OSSConfigDTO {

    /**
     * OSS提供者类型 (aliyun, tencent, minio)
     */
    @NotBlank(message = "OSS提供者不能为空")
    private String provider;

    /**
     * 端点地址
     */
    @NotBlank(message = "端点地址不能为空")
    private String endpoint;

    /**
     * 访问密钥
     */
    @NotBlank(message = "访问密钥不能为空")
    private String accessKey;

    /**
     * 密钥
     */
    @NotBlank(message = "密钥不能为空")
    private String secretKey;

    /**
     * 存储桶名称
     */
    @NotBlank(message = "存储桶名称不能为空")
    private String bucket;
}
