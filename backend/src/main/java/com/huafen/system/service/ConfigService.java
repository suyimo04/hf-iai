package com.huafen.system.service;

import com.huafen.system.dto.config.ConfigDTO;
import com.huafen.system.dto.config.ConfigUpdateRequest;

import java.util.List;

/**
 * 系统配置服务接口
 */
public interface ConfigService {

    /**
     * 获取所有配置
     */
    List<ConfigDTO> getAllConfigs();

    /**
     * 获取指定配置
     */
    ConfigDTO getConfig(String key);

    /**
     * 更新配置
     */
    ConfigDTO updateConfig(ConfigUpdateRequest request);

    /**
     * 获取配置值，如果不存在则返回默认值
     */
    String getValue(String key, String defaultValue);

    /**
     * 获取整数配置值，如果不存在则返回默认值
     */
    Integer getIntValue(String key, Integer defaultValue);
}
