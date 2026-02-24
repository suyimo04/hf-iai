package com.huafen.system.service.ai;

import com.huafen.system.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI提供者工厂
 * 根据配置选择对应的AI服务提供者
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIProviderFactory {

    private final ConfigService configService;
    private final LocalJSONProvider localJSONProvider;
    private final OpenAICompatibleProvider openAICompatibleProvider;
    private final ClaudeProvider claudeProvider;

    /**
     * 根据系统配置获取AI提供者
     */
    public AIProvider getProvider() {
        String provider = configService.getValue("ai.provider", "local");
        return getProvider(provider);
    }

    /**
     * 根据类型获取AI提供者
     */
    public AIProvider getProvider(String type) {
        if (type == null) {
            return localJSONProvider;
        }

        return switch (type.toLowerCase()) {
            case "openai", "deepseek", "qwen" -> openAICompatibleProvider;
            case "claude", "anthropic" -> claudeProvider;
            case "local" -> localJSONProvider;
            default -> {
                log.warn("Unknown AI provider type: {}, falling back to local", type);
                yield localJSONProvider;
            }
        };
    }

    /**
     * 获取默认提供者
     */
    public AIProvider getDefaultProvider() {
        return getProvider();
    }

    /**
     * 测试指定提供者的连接
     */
    public boolean testProvider(String type) {
        try {
            AIProvider provider = getProvider(type);
            return provider.testConnection();
        } catch (Exception e) {
            log.error("Failed to test provider {}: {}", type, e.getMessage());
            return false;
        }
    }

    /**
     * 获取所有可用的提供者名称
     */
    public String[] getAvailableProviders() {
        return new String[]{"local", "openai", "claude", "deepseek", "qwen"};
    }
}
