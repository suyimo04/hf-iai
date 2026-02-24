package com.huafen.system.dto.activity;

import com.huafen.system.entity.ActivitySignup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 活动报名记录DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivitySignupDTO {

    private Long id;
    private Long activityId;
    private Long userId;
    private String username;
    private String nickname;
    private Boolean signedIn;
    private LocalDateTime signInTime;
    private LocalDateTime createdAt;

    public static ActivitySignupDTO fromEntity(ActivitySignup signup) {
        return ActivitySignupDTO.builder()
                .id(signup.getId())
                .activityId(signup.getActivity() != null ? signup.getActivity().getId() : null)
                .userId(signup.getUser() != null ? signup.getUser().getId() : null)
                .username(signup.getUser() != null ? signup.getUser().getUsername() : null)
                .nickname(signup.getUser() != null ? signup.getUser().getNickname() : null)
                .signedIn(signup.getSignedIn())
                .signInTime(signup.getSignInTime())
                .createdAt(signup.getCreatedAt())
                .build();
    }
}
