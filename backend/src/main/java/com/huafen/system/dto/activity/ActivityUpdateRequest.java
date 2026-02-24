package com.huafen.system.dto.activity;

import com.huafen.system.entity.enums.ActivityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 更新活动请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityUpdateRequest {

    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private Integer maxParticipants;
    private Integer pointsReward;
    private ActivityStatus status;
}
