package com.huafen.system.dto.point;

import com.huafen.system.entity.Point;
import com.huafen.system.entity.enums.PointType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 积分记录DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointDTO {

    private Long id;
    private Long userId;
    private String username;
    private PointType type;
    private Integer amount;
    private String description;
    private LocalDateTime createdAt;

    public static PointDTO fromEntity(Point point) {
        return PointDTO.builder()
                .id(point.getId())
                .userId(point.getUser().getId())
                .username(point.getUser().getUsername())
                .type(point.getType())
                .amount(point.getAmount())
                .description(point.getDescription())
                .createdAt(point.getCreatedAt())
                .build();
    }
}
