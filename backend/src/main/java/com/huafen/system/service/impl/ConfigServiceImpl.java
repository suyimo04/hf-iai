package com.huafen.system.service.impl;

import com.huafen.system.common.ResultCode;
import com.huafen.system.dto.config.ConfigDTO;
import com.huafen.system.dto.config.ConfigUpdateRequest;
import com.huafen.system.entity.SystemConfig;
import com.huafen.system.exception.BusinessException;
import com.huafen.system.repository.SystemConfigRepository;
import com.huafen.system.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统配置服务实现
 */
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final SystemConfigRepository systemConfigRepository;

    @Override
    public List<ConfigDTO> getAllConfigs() {
        return systemConfigRepository.findAll().stream()
                .map(ConfigDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ConfigDTO getConfig(String key) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "配置不存在: " + key));
        return ConfigDTO.fromEntity(config);
    }

    @Override
    @Transactional
    public ConfigDTO updateConfig(ConfigUpdateRequest request) {
        SystemConfig config = systemConfigRepository.findByConfigKey(request.getKey())
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "配置不存在: " + request.getKey()));

        config.setConfigValue(request.getValue());
        SystemConfig savedConfig = systemConfigRepository.save(config);
        return ConfigDTO.fromEntity(savedConfig);
    }

    @Override
    public String getValue(String key, String defaultValue) {
        return systemConfigRepository.findByConfigKey(key)
                .map(SystemConfig::getConfigValue)
                .orElse(defaultValue);
    }

    @Override
    public Integer getIntValue(String key, Integer defaultValue) {
        return systemConfigRepository.findByConfigKey(key)
                .map(config -> {
                    try {
                        return Integer.parseInt(config.getConfigValue());
                    } catch (NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }
}
