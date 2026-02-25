package com.huafen.system.dto.interview;

import com.huafen.system.entity.AIInterviewMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI面试消息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIInterviewMessageDTO {

    private Long id;
    private Long sessionId;
    private String role;
    private String content;
    private Integer sequenceNumber;
    private Integer tokenCount;
    private LocalDateTime createdAt;

    public static AIInterviewMessageDTO fromEntity(AIInterviewMessage message) {
        if (message == null) {
            return null;
        }
        return AIInterviewMessageDTO.builder()
                .id(message.getId())
                .sessionId(message.getSession() != null ? message.getSession().getId() : null)
                .role(message.getRole() != null ? message.getRole().name() : null)
                .content(message.getContent())
                .sequenceNumber(message.getSequenceNumber())
                .tokenCount(message.getTokenCount())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
