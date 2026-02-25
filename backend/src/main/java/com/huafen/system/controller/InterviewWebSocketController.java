package com.huafen.system.controller;

import com.huafen.system.dto.interview.InterviewMessageDTO;
import com.huafen.system.dto.interview.InterviewScoreDTO;
import com.huafen.system.dto.interview.StartInterviewRequest;
import com.huafen.system.entity.Interview;
import com.huafen.system.entity.enums.InterviewStatus;
import com.huafen.system.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI面试WebSocket控制器
 * 处理AI面试实时消息交互
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class InterviewWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final InterviewRepository interviewRepository;

    // 存储会话的违规类型
    private final Map<Long, String[]> sessionViolationTypes = new ConcurrentHashMap<>();

    // 违规类型列表
    private static final String[] VIOLATION_TYPES = {
            "辱骂他人",
            "恶意刷屏",
            "发布不当内容",
            "冒充管理员",
            "散布谣言",
            "恶意举报"
    };

    /**
     * 开始面试
     * 创建会话，随机选择1-2个违规类型，发送AI第一条消息
     */
    @MessageMapping("/interview/start")
    public void startInterview(@Payload StartInterviewRequest request, Principal principal) {
        log.info("用户 {} 开始AI面试, applicationId: {}", principal.getName(), request.getApplicationId());

        try {
            // 创建面试记录
            Interview interview = new Interview();
            interview.setStatus(InterviewStatus.IN_PROGRESS);
            interview.setCreatedAt(LocalDateTime.now());
            interview = interviewRepository.save(interview);
            Long sessionId = interview.getId();

            // 随机选择1-2个违规类型
            Random random = new Random();
            int violationCount = random.nextInt(2) + 1;
            String[] selectedViolations = new String[violationCount];
            for (int i = 0; i < violationCount; i++) {
                selectedViolations[i] = VIOLATION_TYPES[random.nextInt(VIOLATION_TYPES.length)];
            }
            sessionViolationTypes.put(sessionId, selectedViolations);

            // 发送会话创建成功消息
            InterviewMessageDTO sessionInfo = InterviewMessageDTO.systemMessage(
                    sessionId,
                    "面试会话已创建，会话ID: " + sessionId
            );
            sendToUser(principal.getName(), sessionId, sessionInfo);

            // 发送AI第一条消息（扮演违规成员）
            String firstMessage = generateFirstMessage(selectedViolations);
            InterviewMessageDTO aiMessage = InterviewMessageDTO.aiMessage(sessionId, firstMessage, true);
            sendToUser(principal.getName(), sessionId, aiMessage);

            log.info("AI面试会话 {} 创建成功, 违规类型: {}", sessionId, String.join(", ", selectedViolations));
        } catch (Exception e) {
            log.error("开始面试失败", e);
            InterviewMessageDTO errorMsg = InterviewMessageDTO.systemMessage(null, "开始面试失败: " + e.getMessage());
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/interview/error", errorMsg);
        }
    }

    /**
     * 处理用户发送的消息
     */
    @MessageMapping("/interview/{sessionId}/send")
    public void handleMessage(@DestinationVariable Long sessionId,
                              @Payload String content,
                              Principal principal) {
        log.info("用户 {} 在会话 {} 发送消息: {}", principal.getName(), sessionId, content);

        try {
            // 1. 保存用户消息（可扩展为持久化）
            InterviewMessageDTO userMessage = InterviewMessageDTO.userMessage(sessionId, content);
            sendToUser(principal.getName(), sessionId, userMessage);

            // 2. 获取该会话的违规类型
            String[] violations = sessionViolationTypes.get(sessionId);
            if (violations == null) {
                InterviewMessageDTO errorMsg = InterviewMessageDTO.systemMessage(sessionId, "会话不存在或已过期");
                sendToUser(principal.getName(), sessionId, errorMsg);
                return;
            }

            // 3. 生成AI响应（模拟流式响应）
            String aiResponse = generateAIResponse(content, violations);

            // 模拟流式发送（分段发送）
            sendStreamResponse(principal.getName(), sessionId, aiResponse);

        } catch (Exception e) {
            log.error("处理消息失败", e);
            InterviewMessageDTO errorMsg = InterviewMessageDTO.systemMessage(sessionId, "处理消息失败: " + e.getMessage());
            sendToUser(principal.getName(), sessionId, errorMsg);
        }
    }

    /**
     * 结束面试并评分
     */
    @MessageMapping("/interview/{sessionId}/end")
    public void endInterview(@DestinationVariable Long sessionId, Principal principal) {
        log.info("用户 {} 结束会话 {} 的面试", principal.getName(), sessionId);

        try {
            // 获取违规类型
            String[] violations = sessionViolationTypes.get(sessionId);
            if (violations == null) {
                InterviewMessageDTO errorMsg = InterviewMessageDTO.systemMessage(sessionId, "会话不存在或已过期");
                sendToUser(principal.getName(), sessionId, errorMsg);
                return;
            }

            // 生成评分结果
            InterviewScoreDTO score = generateScore(sessionId);

            // 更新面试记录
            interviewRepository.findById(sessionId).ifPresent(interview -> {
                interview.setScore(score.getTotalScore());
                interview.setReport(score.getEvaluation());
                interview.setStatus(score.getPassed() ? InterviewStatus.PASSED : InterviewStatus.FAILED);
                interviewRepository.save(interview);
            });

            // 发送评分结果
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/interview/" + sessionId + "/score",
                    score
            );

            // 清理会话数据
            sessionViolationTypes.remove(sessionId);

            log.info("会话 {} 面试结束, 总分: {}, 是否通过: {}", sessionId, score.getTotalScore(), score.getPassed());
        } catch (Exception e) {
            log.error("结束面试失败", e);
            InterviewMessageDTO errorMsg = InterviewMessageDTO.systemMessage(sessionId, "结束面试失败: " + e.getMessage());
            sendToUser(principal.getName(), sessionId, errorMsg);
        }
    }

    /**
     * 发送消息给用户
     */
    private void sendToUser(String username, Long sessionId, InterviewMessageDTO message) {
        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/interview/" + sessionId,
                message
        );
    }

    /**
     * 模拟流式发送响应
     */
    private void sendStreamResponse(String username, Long sessionId, String fullResponse) {
        // 将响应分成多个片段发送，模拟流式效果
        int chunkSize = 20;
        for (int i = 0; i < fullResponse.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, fullResponse.length());
            String chunk = fullResponse.substring(i, end);
            boolean isEnd = (end >= fullResponse.length());

            InterviewMessageDTO aiMessage = InterviewMessageDTO.aiMessage(sessionId, chunk, isEnd);
            sendToUser(username, sessionId, aiMessage);

            // 模拟延迟
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 生成AI第一条消息
     */
    private String generateFirstMessage(String[] violations) {
        StringBuilder sb = new StringBuilder();
        sb.append("【场景模拟开始】\n\n");
        sb.append("你好，我是一名社区成员。");

        if (violations.length == 1) {
            sb.append("我对最近的一些事情有些不满，想和你聊聊。");
        } else {
            sb.append("我最近遇到了一些问题，心情不太好，想找人说说。");
        }

        sb.append("\n\n请以管理员身份与我对话，处理可能出现的违规行为。");
        return sb.toString();
    }

    /**
     * 生成AI响应（基于违规类型）
     */
    private String generateAIResponse(String userMessage, String[] violations) {
        // 这里是简化的响应逻辑，实际应调用AI服务
        Random random = new Random();
        String currentViolation = violations[random.nextInt(violations.length)];

        return switch (currentViolation) {
            case "辱骂他人" -> "你说的这些我不同意！你们这些管理员就知道偏袒，真是太过分了！";
            case "恶意刷屏" -> "我要说的话很重要！！！大家都来看看！！！这件事必须解决！！！";
            case "发布不当内容" -> "我觉得这个规则太死板了，我就是要发我想发的内容，你管得着吗？";
            case "冒充管理员" -> "我也是管理员，我说的话你应该听。这个用户必须被处理！";
            case "散布谣言" -> "我听说管理层要大换血了，很多人都要被踢出去，你知道吗？";
            case "恶意举报" -> "我要举报那个用户！他肯定有问题，你们必须处理他！";
            default -> "我对这个处理结果不满意，你们应该重新考虑。";
        };
    }

    /**
     * 生成评分结果
     */
    private InterviewScoreDTO generateScore(Long sessionId) {
        // 这里是简化的评分逻辑，实际应调用AI服务进行评分
        Random random = new Random();

        InterviewScoreDTO score = InterviewScoreDTO.builder()
                .sessionId(sessionId)
                .communicationScore(15 + random.nextInt(11))      // 15-25
                .problemSolvingScore(15 + random.nextInt(11))     // 15-25
                .ruleUnderstandingScore(15 + random.nextInt(11))  // 15-25
                .adaptabilityScore(15 + random.nextInt(11))       // 15-25
                .evaluation("面试者展现了良好的沟通能力和问题处理技巧。在面对违规行为时能够保持冷静，并尝试通过合理的方式解决问题。建议在规则理解方面继续加强学习。")
                .build();

        score.calculateTotalScore();
        score.setPassed(score.getTotalScore() >= 60);

        return score;
    }
}
