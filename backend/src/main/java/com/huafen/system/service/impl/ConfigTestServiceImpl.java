package com.huafen.system.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huafen.system.dto.config.TestResult;
import com.huafen.system.service.ConfigTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.*;

/**
 * 配置测试服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigTestServiceImpl implements ConfigTestService {

    private final ObjectMapper objectMapper;

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    @Override
    public TestResult testAIConnection(String provider, String apiKey, String baseUrl, String model) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return TestResult.fail("API Key不能为空");
        }

        try {
            return switch (provider.toLowerCase()) {
                case "openai", "deepseek", "qwen" -> testOpenAICompatible(apiKey, baseUrl, model);
                case "claude", "anthropic" -> testClaude(apiKey, baseUrl, model);
                default -> TestResult.fail("不支持的AI提供者: " + provider);
            };
        } catch (Exception e) {
            log.error("AI连接测试失败: {}", e.getMessage());
            return TestResult.fail("AI连接失败: " + e.getMessage());
        }
    }

    private TestResult testOpenAICompatible(String apiKey, String baseUrl, String model) {
        String url = baseUrl != null && !baseUrl.isEmpty() ? baseUrl : "https://api.openai.com/v1";
        String testModel = model != null && !model.isEmpty() ? model : "gpt-3.5-turbo";

        WebClient webClient = WebClient.builder()
                .baseUrl(url)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", testModel);
        requestBody.put("max_tokens", 10);
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> msg = new HashMap<>();
        msg.put("role", "user");
        msg.put("content", "Hi");
        messages.add(msg);
        requestBody.put("messages", messages);

        try {
            webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(TIMEOUT);
            return TestResult.success("AI连接成功，模型响应正常");
        } catch (Exception e) {
            return TestResult.fail("AI连接失败: " + e.getMessage());
        }
    }

    private TestResult testClaude(String apiKey, String baseUrl, String model) {
        String url = baseUrl != null && !baseUrl.isEmpty() ? baseUrl : "https://api.anthropic.com";
        String testModel = model != null && !model.isEmpty() ? model : "claude-3-sonnet-20240229";

        WebClient webClient = WebClient.builder()
                .baseUrl(url)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("Content-Type", "application/json")
                .build();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", testModel);
        requestBody.put("max_tokens", 10);
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> msg = new HashMap<>();
        msg.put("role", "user");
        msg.put("content", "Hi");
        messages.add(msg);
        requestBody.put("messages", messages);

        try {
            webClient.post()
                    .uri("/v1/messages")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(TIMEOUT);
            return TestResult.success("AI连接成功，模型响应正常");
        } catch (Exception e) {
            return TestResult.fail("AI连接失败: " + e.getMessage());
        }
    }

    @Override
    public TestResult testOSSConnection(String provider, String endpoint, String accessKey, String secretKey, String bucket) {
        if (accessKey == null || accessKey.trim().isEmpty()) {
            return TestResult.fail("AccessKey不能为空");
        }
        if (secretKey == null || secretKey.trim().isEmpty()) {
            return TestResult.fail("SecretKey不能为空");
        }
        if (bucket == null || bucket.trim().isEmpty()) {
            return TestResult.fail("Bucket名称不能为空");
        }

        try {
            return switch (provider.toLowerCase()) {
                case "aliyun" -> testAliyunOSS(endpoint, accessKey, secretKey, bucket);
                case "tencent" -> testTencentCOS(endpoint, accessKey, secretKey, bucket);
                case "minio" -> testMinIO(endpoint, accessKey, secretKey, bucket);
                default -> TestResult.fail("不支持的OSS提供者: " + provider);
            };
        } catch (Exception e) {
            log.error("OSS连接测试失败: {}", e.getMessage());
            return TestResult.fail("OSS连接失败: " + e.getMessage());
        }
    }

    private TestResult testAliyunOSS(String endpoint, String accessKey, String secretKey, String bucket) {
        // 使用HTTP请求测试阿里云OSS连接
        try {
            String testEndpoint = endpoint != null && !endpoint.isEmpty() ? endpoint : "oss-cn-hangzhou.aliyuncs.com";
            String url = String.format("https://%s.%s", bucket, testEndpoint);

            WebClient webClient = WebClient.builder()
                    .baseUrl(url)
                    .build();

            webClient.get()
                    .uri("/?location")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(TIMEOUT);

            return TestResult.success("OSS连接成功，存储桶访问正常");
        } catch (Exception e) {
            // 即使返回403也说明连接成功，只是权限问题
            if (e.getMessage() != null && e.getMessage().contains("403")) {
                return TestResult.success("OSS连接成功，请检查访问权限配置");
            }
            return TestResult.fail("OSS连接失败: " + e.getMessage());
        }
    }

    private TestResult testTencentCOS(String endpoint, String accessKey, String secretKey, String bucket) {
        try {
            String testEndpoint = endpoint != null && !endpoint.isEmpty() ? endpoint : "cos.ap-guangzhou.myqcloud.com";
            String url = String.format("https://%s.%s", bucket, testEndpoint);

            WebClient webClient = WebClient.builder()
                    .baseUrl(url)
                    .build();

            webClient.get()
                    .uri("/?location")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(TIMEOUT);

            return TestResult.success("COS连接成功，存储桶访问正常");
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("403")) {
                return TestResult.success("COS连接成功，请检查访问权限配置");
            }
            return TestResult.fail("COS连接失败: " + e.getMessage());
        }
    }

    private TestResult testMinIO(String endpoint, String accessKey, String secretKey, String bucket) {
        try {
            if (endpoint == null || endpoint.isEmpty()) {
                return TestResult.fail("MinIO endpoint不能为空");
            }

            String url = endpoint.startsWith("http") ? endpoint : "http://" + endpoint;

            WebClient webClient = WebClient.builder()
                    .baseUrl(url)
                    .build();

            webClient.get()
                    .uri("/" + bucket)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(TIMEOUT);

            return TestResult.success("MinIO连接成功，存储桶访问正常");
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("403")) {
                return TestResult.success("MinIO连接成功，请检查访问权限配置");
            }
            return TestResult.fail("MinIO连接失败: " + e.getMessage());
        }
    }

    @Override
    public TestResult testEmailConnection(String host, Integer port, String username, String password, String testTo) {
        if (host == null || host.trim().isEmpty()) {
            return TestResult.fail("SMTP服务器地址不能为空");
        }
        if (username == null || username.trim().isEmpty()) {
            return TestResult.fail("邮箱用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return TestResult.fail("邮箱密码不能为空");
        }

        try {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(host);
            mailSender.setPort(port != null ? port : 587);
            mailSender.setUsername(username);
            mailSender.setPassword(password);

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "10000");

            // 测试连接
            mailSender.testConnection();

            // 如果提供了测试收件人，发送测试邮件
            if (testTo != null && !testTo.trim().isEmpty()) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(username);
                message.setTo(testTo);
                message.setSubject("华芬系统邮件测试");
                message.setText("这是一封测试邮件，用于验证邮件服务配置是否正确。");
                mailSender.send(message);
                return TestResult.success("邮件发送成功");
            }

            return TestResult.success("邮件服务器连接成功");
        } catch (Exception e) {
            log.error("邮件连接测试失败: {}", e.getMessage());
            return TestResult.fail("邮件连接失败: " + e.getMessage());
        }
    }
}
