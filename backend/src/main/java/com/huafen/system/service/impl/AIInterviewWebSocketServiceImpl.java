package com.huafen.system.service.impl;

import com.huafen.system.dto.interview.AIInterviewSessionDTO;
import com.huafen.system.entity.AIInterviewMessage;
import com.huafen.system.entity.AIInterviewSession;
import com.huafen.system.entity.User;
import com.huafen.system.entity.enums.AIInterviewStatus;
import com.huafen.system.entity.enums.MessageRole;
import com.huafen.system.exception.BusinessException;
import com.huafen.system.repository.AIInterviewMessageRepository;
import com.huafen.system.repository.AIInterviewSessionRepository;
import com.huafen.system.repository.UserRepository;
import com.huafen.system.service.AIInterviewWebSocketService;
import com.huafen.system.service.ai.AIProvider;
import com.huafen.system.service.ai.AIProviderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI面试WebSocket服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIInterviewWebSocketServiceImpl implements AIInterviewWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final AIInterviewSessionRepository sessionRepository;
    private final AIInterviewMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final AIProviderFactory aiProviderFactory;

    @Override
    @Transactional
    public AIInterviewSessionDTO startSession(String username, Long applicationId, String wsSessionId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 创建新会话
        AIInterviewSession session = AIInterviewSession.builder()
                .user(user)
                .status(AIInterviewStatus.IN_PROGRESS)
                .roundCount(0)
                .maxRounds(15)
                .startedAt(LocalDateTime.now())
                .build();

        session = sessionRepository.save(session);

        // 生成AI开场白
        generateOpeningMessage(session);

        return AIInterviewSessionDTO.fromEntity(session);
    }

    @Override
    @Transactional
    public void processUserMessage(Long sessionId, String username, String content) {
        AIInterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("会话不存在"));

        if (session.getStatus() != AIInterviewStatus.IN_PROGRESS) {
            sendError(sessionId, username, "会话已结束");
            return;
        }

        // 保存用户消息
        int nextSeq = messageRepository.countBySessionId(sessionId) + 1;
        AIInterviewMessage userMessage = AIInterviewMessage.builder()
                .session(session)
                .role(MessageRole.USER)
                .content(content)
                .sequenceNumber(nextSeq)
                .build();
        messageRepository.save(userMessage);

        // 异步生成AI回复
        generateAIResponseAsync(session, username, content);
    }

    @Async
    protected void generateAIResponseAsync(AIInterviewSession session, String username, String userContent) {
        try {
            // 获取历史消息构建上下文
            List<AIInterviewMessage> history = messageRepository.findBySessionIdOrderBySequenceNumberAsc(session.getId());

            // 调用AI生成回复
            AIProvider provider = aiProviderFactory.getProvider();
            String systemPrompt = buildSystemPrompt(session);
            String conversationHistory = buildConversationHistory(history);

            StringBuilder aiResponse = new StringBuilder();

            // 流式输出
            provider.streamChat(systemPrompt, conversationHistory, userContent, chunk -> {
                aiResponse.append(chunk);
                sendTypingContent(session.getId(), username, chunk);
            });

            // 保存AI消息
            int nextSeq = messageRepository.countBySessionId(session.getId()) + 1;
            AIInterviewMessage aiMessage = AIInterviewMessage.builder()
                    .session(session)
                    .role(MessageRole.AI)
                    .content(aiResponse.toString())
                    .sequenceNumber(nextSeq)
                    .build();
            messageRepository.save(aiMessage);

            // 更新轮次
            session.setRoundCount(session.getRoundCount() + 1);
            sessionRepository.save(session);

            // 发送完整响应
            sendAIResponse(session.getId(), username, aiResponse.toString());

            // 检查是否达到最大轮次
            if (session.getRoundCount() >= session.getMaxRounds()) {
                endSession(session.getId(), username);
            }

        } catch (Exception e) {
            log.error("生成AI回复失败", e);
            sendError(session.getId(), username, "AI回复生成失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public AIInterviewSessionDTO endSession(Long sessionId, String username) {
        AIInterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("会话不存在"));

        session.setStatus(AIInterviewStatus.COMPLETED);
        session.setEndedAt(LocalDateTime.now());
        session = sessionRepository.save(session);

        AIInterviewSessionDTO dto = AIInterviewSessionDTO.fromEntity(session);
        sendSessionEnd(sessionId, username, dto);

        return dto;
    }

    @Override
    public AIInterviewSessionDTO getSessionStatus(Long sessionId, String username) {
        AIInterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("会话不存在"));
        return AIInterviewSessionDTO.fromEntity(session);
    }

    @Override
    public void sendAIResponse(Long sessionId, String username, String content) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "AI_RESPONSE");
        message.put("sessionId", sessionId);
        message.put("content", content);

        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/interview/message",
                message
        );
    }

    @Override
    public void sendTypingContent(Long sessionId, String username, String content) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "TYPING");
        message.put("sessionId", sessionId);
        message.put("content", content);

        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/interview/message",
                message
        );
    }

    @Override
    public void sendSessionEnd(Long sessionId, String username, AIInterviewSessionDTO session) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "SESSION_END");
        message.put("sessionId", sessionId);
        message.put("data", session);

        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/interview/message",
                message
        );
    }

    @Override
    public void sendError(Long sessionId, String username, String errorMessage) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "ERROR");
        message.put("sessionId", sessionId);
        message.put("content", errorMessage);

        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/interview/message",
                message
        );
    }

    private void generateOpeningMessage(AIInterviewSession session) {
        String openingMessage = "你好，欢迎参加本次AI面试。我将模拟一些群管理场景，请根据情况做出你的处理决策。准备好了吗？";

        AIInterviewMessage message = AIInterviewMessage.builder()
                .session(session)
                .role(MessageRole.AI)
                .content(openingMessage)
                .sequenceNumber(1)
                .build();
        messageRepository.save(message);
    }

    private String buildSystemPrompt(AIInterviewSession session) {
        return """
                你是一个群管理面试官AI，负责测试候选人的群管理能力。
                你需要模拟各种群内违规场景，测试候选人的：
                1. 处理态度 - 是否礼貌、专业
                2. 执行群规能力 - 是否熟悉并正确执行群规
                3. 情绪控制 - 面对挑衅是否保持冷静
                4. 决策合理性 - 处理方式是否恰当

                请根据候选人的回答，继续模拟场景或追问，最多进行15轮对话。
                """;
    }

    private String buildConversationHistory(List<AIInterviewMessage> history) {
        return history.stream()
                .map(msg -> msg.getRole().name() + ": " + msg.getContent())
                .collect(Collectors.joining("\n"));
    }
}
