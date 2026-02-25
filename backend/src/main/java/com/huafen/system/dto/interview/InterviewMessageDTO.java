package com.huafen.system.dto.interview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 面试消息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewMessageDTO {

    /**
     * 消息角色: USER(用户), AI(AI面试官), SYSTEM(系统)
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 是否为流式消息的最后一条
     */
    private Boolean isEnd;

    public static InterviewMessageDTO userMessage(Long sessionId, String content) {
        return InterviewMessageDTO.builder()
                .role("USER")
                .content(content)
                .sessionId(sessionId)
                .timestamp(LocalDateTime.now())
                .isEnd(true)
                .build();
    }

    public static InterviewMessageDTO aiMessage(Long sessionId, String content, boolean isEnd) {
        return InterviewMessageDTO.builder()
                .role("AI")
                .content(content)
                .sessionId(sessionId)
                .timestamp(LocalDateTime.now())
                .isEnd(isEnd)
                .build();
    }

    public static InterviewMessageDTO systemMessage(Long sessionId, String content) {
        return InterviewMessageDTO.builder()
                .role("SYSTEM")
                .content(content)
                .sessionId(sessionId)
                .timestamp(LocalDateTime.now())
                .isEnd(true)
                .build();
    }
}
