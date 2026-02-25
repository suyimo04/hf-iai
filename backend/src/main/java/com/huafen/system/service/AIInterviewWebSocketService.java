package com.huafen.system.service;

import com.huafen.system.dto.interview.AIInterviewSessionDTO;

/**
 * AI面试WebSocket服务接口
 */
public interface AIInterviewWebSocketService {

    /**
     * 开始面试会话
     */
    AIInterviewSessionDTO startSession(String username, Long applicationId, String wsSessionId);

    /**
     * 处理用户消息
     */
    void processUserMessage(Long sessionId, String username, String content);

    /**
     * 结束面试会话
     */
    AIInterviewSessionDTO endSession(Long sessionId, String username);

    /**
     * 获取会话状态
     */
    AIInterviewSessionDTO getSessionStatus(Long sessionId, String username);

    /**
     * 发送AI响应到客户端
     */
    void sendAIResponse(Long sessionId, String username, String content);

    /**
     * 发送流式输出到客户端
     */
    void sendTypingContent(Long sessionId, String username, String content);

    /**
     * 发送会话结束通知
     */
    void sendSessionEnd(Long sessionId, String username, AIInterviewSessionDTO session);

    /**
     * 发送错误消息
     */
    void sendError(Long sessionId, String username, String errorMessage);
}
