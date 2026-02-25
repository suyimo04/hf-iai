package com.huafen.system.dto.config;

import com.huafen.system.entity.SystemConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统配置DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigDTO {

    private String key;
    private String value;
    private String description;
    private LocalDateTime updatedAt;

    /**
     * 从实体转换为DTO
     */
    public static ConfigDTO fromEntity(SystemConfig config) {
        if (config == null) {
            return null;
        }
        return ConfigDTO.builder()
                .key(config.getConfigKey())
                .value(config.getConfigValue())
                .description(config.getDescription())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
