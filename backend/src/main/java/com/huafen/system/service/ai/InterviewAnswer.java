package com.huafen.system.service.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 面试答案
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewAnswer {

    private Long questionId;
    private String answer;
}
