package com.huafen.system.service.impl;

import com.huafen.system.entity.ConfigChangeLog;
import com.huafen.system.entity.SystemConfig;
import com.huafen.system.repository.ConfigChangeLogRepository;
import com.huafen.system.repository.SystemConfigRepository;
import com.huafen.system.service.ConfigCacheService;
import com.huafen.system.service.ConfigEncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 配置缓存服务实现
 * 提供Redis缓存和热更新功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigCacheServiceImpl implements ConfigCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SystemConfigRepository configRepository;
    private final ConfigEncryptionService encryptionService;
    private final ConfigChangeLogRepository changeLogRepository;

    private static final String CACHE_PREFIX = "config:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    @Override
    public String getConfig(String group, String key) {
        String cacheKey = buildCacheKey(group, key);

        // 优先从缓存获取
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("从缓存获取配置: {}", cacheKey);
            return (String) cached;
        }

        // 缓存未命中，从数据库查询
        SystemConfig config = configRepository.findByConfigGroupAndConfigKey(group, key)
                .orElse(null);
        if (config == null) {
            return null;
        }

        // 解密敏感配置
        String value = Boolean.TRUE.equals(config.getEncrypted())
                ? encryptionService.decrypt(config.getConfigValue())
                : config.getConfigValue();

        // 写入缓存
        redisTemplate.opsForValue().set(cacheKey, value, CACHE_TTL);
        log.debug("配置已缓存: {}", cacheKey);

        return value;
    }

    @Override
    @Transactional
    public void updateConfig(String group, String key, String value, boolean encrypted) {
        SystemConfig config = configRepository.findByConfigGroupAndConfigKey(group, key)
                .orElse(null);

        String oldValue = null;
        if (config == null) {
            // 新建配置
            config = SystemConfig.builder()
                    .configGroup(group)
                    .configKey(key)
                    .encrypted(encrypted)
                    .build();
        } else {
            // 记录旧值
            oldValue = Boolean.TRUE.equals(config.getEncrypted())
                    ? encryptionService.decrypt(config.getConfigValue())
                    : config.getConfigValue();
        }

        // 加密存储
        String storedValue = encrypted ? encryptionService.encrypt(value) : value;
        config.setConfigValue(storedValue);
        config.setEncrypted(encrypted);
        config.setUpdatedBy(getCurrentUserId());

        configRepository.save(config);

        // 记录变更日志
        saveChangeLog(group, key, oldValue, value);

        // 更新缓存
        String cacheKey = buildCacheKey(group, key);
        redisTemplate.opsForValue().set(cacheKey, value, CACHE_TTL);
        log.info("配置已更新并缓存: {}", cacheKey);
    }

    @Override
    public void evictConfig(String group, String key) {
        String cacheKey = buildCacheKey(group, key);
        redisTemplate.delete(cacheKey);
        log.info("配置缓存已清除: {}", cacheKey);
    }

    @Override
    public void evictGroup(String group) {
        String pattern = CACHE_PREFIX + group + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("分组缓存已清除: {}, 共{}个", group, keys.size());
        }
    }

    @Override
    public Map<String, String> getGroupConfigs(String group) {
        Map<String, String> result = new HashMap<>();

        List<SystemConfig> configs = configRepository.findByConfigGroup(group);
        for (SystemConfig config : configs) {
            String value = Boolean.TRUE.equals(config.getEncrypted())
                    ? encryptionService.decrypt(config.getConfigValue())
                    : config.getConfigValue();
            result.put(config.getConfigKey(), value);

            // 同时更新缓存
            String cacheKey = buildCacheKey(group, config.getConfigKey());
            redisTemplate.opsForValue().set(cacheKey, value, CACHE_TTL);
        }

        return result;
    }

    private String buildCacheKey(String group, String key) {
        return CACHE_PREFIX + group + ":" + key;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof com.huafen.system.entity.User) {
            return ((com.huafen.system.entity.User) auth.getPrincipal()).getId();
        }
        return null;
    }

    private void saveChangeLog(String group, String key, String oldValue, String newValue) {
        ConfigChangeLog changeLog = ConfigChangeLog.builder()
                .configGroup(group)
                .configKey(key)
                .oldValue(oldValue)
                .newValue(newValue)
                .changedBy(getCurrentUserId())
                .build();
        changeLogRepository.save(changeLog);
    }
}
