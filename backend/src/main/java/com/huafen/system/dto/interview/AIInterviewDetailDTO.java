package com.huafen.system.dto.interview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI面试详情DTO（含消息历史和评分）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIInterviewDetailDTO {

    /**
     * 会话基本信息
     */
    private AIInterviewSessionDTO session;

    /**
     * 消息历史列表
     */
    private List<AIInterviewMessageDTO> messages;

    /**
     * 评分信息
     */
    private AIInterviewScoreDTO score;
}
