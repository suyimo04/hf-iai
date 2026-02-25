package com.huafen.system.dto.interview;

import com.huafen.system.service.ai.InterviewAnswer;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 提交面试答案请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewSubmitRequest {

    @NotEmpty(message = "答案列表不能为空")
    private List<InterviewAnswer> answers;
}
