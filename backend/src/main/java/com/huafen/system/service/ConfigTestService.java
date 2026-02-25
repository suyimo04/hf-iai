package com.huafen.system.service;

import com.huafen.system.dto.config.TestResult;

/**
 * 配置测试服务接口
 * 用于测试AI、OSS、邮件等服务的连接
 */
public interface ConfigTestService {

    /**
     * 测试AI连接
     *
     * @param provider AI提供者类型 (openai, claude, deepseek, qwen)
     * @param apiKey   API密钥
     * @param baseUrl  API基础URL
     * @param model    模型名称
     * @return 测试结果
     */
    TestResult testAIConnection(String provider, String apiKey, String baseUrl, String model);

    /**
     * 测试OSS连接
     *
     * @param provider  OSS提供者类型 (aliyun, tencent, minio)
     * @param endpoint  端点地址
     * @param accessKey 访问密钥
     * @param secretKey 密钥
     * @param bucket    存储桶名称
     * @return 测试结果
     */
    TestResult testOSSConnection(String provider, String endpoint, String accessKey, String secretKey, String bucket);

    /**
     * 测试邮件连接
     *
     * @param host     SMTP服务器地址
     * @param port     端口
     * @param username 用户名
     * @param password 密码
     * @param testTo   测试收件人地址
     * @return 测试结果
     */
    TestResult testEmailConnection(String host, Integer port, String username, String password, String testTo);
}
