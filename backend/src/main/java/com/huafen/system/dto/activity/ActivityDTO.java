package com.huafen.system.dto.activity;

import com.huafen.system.entity.Activity;
import com.huafen.system.entity.enums.ActivityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 活动DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDTO {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private Integer maxParticipants;
    private Long currentParticipants;
    private Integer pointsReward;
    private ActivityStatus status;
    private Long createdBy;
    private String creatorName;
    private LocalDateTime createdAt;

    public static ActivityDTO fromEntity(Activity activity, Long currentParticipants) {
        return ActivityDTO.builder()
                .id(activity.getId())
                .title(activity.getTitle())
                .description(activity.getDescription())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .location(activity.getLocation())
                .maxParticipants(activity.getMaxParticipants())
                .currentParticipants(currentParticipants)
                .pointsReward(activity.getPointsReward())
                .status(activity.getStatus())
                .createdBy(activity.getCreatedBy() != null ? activity.getCreatedBy().getId() : null)
                .creatorName(activity.getCreatedBy() != null ? activity.getCreatedBy().getNickname() : null)
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
