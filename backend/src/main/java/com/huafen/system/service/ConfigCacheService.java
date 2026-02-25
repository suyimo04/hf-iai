package com.huafen.system.service;

import java.util.Map;

/**
 * 配置缓存服务接口
 * 提供Redis缓存和热更新功能
 */
public interface ConfigCacheService {

    /**
     * 获取配置（优先从缓存）
     * @param group 配置分组
     * @param key 配置键
     * @return 配置值，不存在返回null
     */
    String getConfig(String group, String key);

    /**
     * 更新配置（同时更新缓存）
     * @param group 配置分组
     * @param key 配置键
     * @param value 配置值
     * @param encrypted 是否加密存储
     */
    void updateConfig(String group, String key, String value, boolean encrypted);

    /**
     * 清除缓存（热更新）
     * @param group 配置分组
     * @param key 配置键
     */
    void evictConfig(String group, String key);

    /**
     * 清除分组所有缓存
     * @param group 配置分组
     */
    void evictGroup(String group);

    /**
     * 获取分组所有配置
     * @param group 配置分组
     * @return 配置键值对Map
     */
    Map<String, String> getGroupConfigs(String group);
}
