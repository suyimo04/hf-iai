package com.huafen.system.controller;

import com.huafen.system.common.Result;
import com.huafen.system.dto.config.*;
import com.huafen.system.service.ConfigCacheService;
import com.huafen.system.service.ConfigService;
import com.huafen.system.service.ConfigTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 */
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;
    private final ConfigCacheService configCacheService;
    private final ConfigTestService configTestService;

    /**
     * 获取所有配置
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public Result<List<ConfigDTO>> list() {
        List<ConfigDTO> configs = configService.getAllConfigs();
        return Result.success(configs);
    }

    /**
     * 获取配置（按分组）
     */
    @GetMapping("/group/{group}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public Result<Map<String, String>> getByGroup(@PathVariable String group) {
        Map<String, String> configs = configCacheService.getGroupConfigs(group);
        return Result.success(configs);
    }

    /**
     * 获取指定配置
     */
    @GetMapping("/{key}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public Result<ConfigDTO> get(@PathVariable String key) {
        ConfigDTO config = configService.getConfig(key);
        return Result.success(config);
    }

    /**
     * 更新配置
     */
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public Result<ConfigDTO> update(@Valid @RequestBody ConfigUpdateRequest request) {
        ConfigDTO config = configService.updateConfig(request);
        return Result.success(config);
    }

    /**
     * 测试AI连接
     */
    @PostMapping("/test/ai")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public Result<TestResult> testAIConnection(@Valid @RequestBody AIConfigDTO config) {
        TestResult result = configTestService.testAIConnection(
                config.getProvider(),
                config.getApiKey(),
                config.getBaseUrl(),
                config.getModel()
        );
        return Result.success(result);
    }

    /**
     * 测试OSS连接
     */
    @PostMapping("/test/oss")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public Result<TestResult> testOSSConnection(@Valid @RequestBody OSSConfigDTO config) {
        TestResult result = configTestService.testOSSConnection(
                config.getProvider(),
                config.getEndpoint(),
                config.getAccessKey(),
                config.getSecretKey(),
                config.getBucket()
        );
        return Result.success(result);
    }

    /**
     * 测试邮件连接
     */
    @PostMapping("/test/email")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public Result<TestResult> testEmailConnection(@Valid @RequestBody EmailConfigDTO config) {
        TestResult result = configTestService.testEmailConnection(
                config.getHost(),
                config.getPort(),
                config.getUsername(),
                config.getPassword(),
                config.getTestTo()
        );
        return Result.success(result);
    }
}
