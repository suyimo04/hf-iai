package com.huafen.system.dto.activity;

import com.huafen.system.entity.enums.ActivityStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 创建活动请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityCreateRequest {

    @NotBlank(message = "活动标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    private String location;

    private Integer maxParticipants;

    private Integer pointsReward;

    private ActivityStatus status;
}
