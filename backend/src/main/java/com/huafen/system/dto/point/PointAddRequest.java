package com.huafen.system.dto.point;

import com.huafen.system.entity.enums.PointType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加积分请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointAddRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "积分类型不能为空")
    private PointType type;

    @NotNull(message = "积分数量不能为空")
    private Integer amount;

    private String description;
}
