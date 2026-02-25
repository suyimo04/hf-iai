package com.huafen.system.service.impl;

import com.huafen.system.dto.interview.*;
import com.huafen.system.entity.AIInterviewMessage;
import com.huafen.system.entity.AIInterviewScore;
import com.huafen.system.entity.AIInterviewSession;
import com.huafen.system.entity.User;
import com.huafen.system.entity.enums.AIInterviewStatus;
import com.huafen.system.exception.BusinessException;
import com.huafen.system.repository.AIInterviewMessageRepository;
import com.huafen.system.repository.AIInterviewScoreRepository;
import com.huafen.system.repository.AIInterviewSessionRepository;
import com.huafen.system.service.AIInterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI面试服务实现
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AIInterviewServiceImpl implements AIInterviewService {

    private final AIInterviewSessionRepository sessionRepository;
    private final AIInterviewMessageRepository messageRepository;
    private final AIInterviewScoreRepository scoreRepository;

    private static final int PASS_SCORE = 60;

    @Override
    public Page<AIInterviewSessionDTO> listSessions(Pageable pageable) {
        return sessionRepository.findAll(pageable)
                .map(this::toSessionDTOWithScore);
    }

    @Override
    public List<AIInterviewSessionDTO> getMySessions() {
        Long currentUserId = getCurrentUserId();
        List<AIInterviewSession> sessions = sessionRepository.findByUserIdOrderByCreatedAtDesc(currentUserId);
        return sessions.stream()
                .map(this::toSessionDTOWithScore)
                .collect(Collectors.toList());
    }

    @Override
    public AIInterviewDetailDTO getSessionDetail(Long sessionId) {
        AIInterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("面试会话不存在"));

        // 获取消息历史
        List<AIInterviewMessage> messages = messageRepository.findBySessionIdOrderBySequenceNumberAsc(sessionId);
        List<AIInterviewMessageDTO> messageDTOs = messages.stream()
                .map(AIInterviewMessageDTO::fromEntity)
                .collect(Collectors.toList());

        // 获取评分
        AIInterviewScoreDTO scoreDTO = scoreRepository.findBySessionId(sessionId)
                .map(AIInterviewScoreDTO::fromEntity)
                .orElse(null);

        return AIInterviewDetailDTO.builder()
                .session(AIInterviewSessionDTO.fromEntity(session))
                .messages(messageDTOs)
                .score(scoreDTO)
                .build();
    }

    @Override
    public InterviewStatisticsDTO getStatistics() {
        long totalCount = sessionRepository.count();
        long completedCount = sessionRepository.countByStatus(AIInterviewStatus.COMPLETED);
        long passedCount = sessionRepository.countPassedSessions(PASS_SCORE);
        long failedCount = completedCount - passedCount;
        long inProgressCount = sessionRepository.countByStatus(AIInterviewStatus.IN_PROGRESS);
        long pendingCount = sessionRepository.countByStatus(AIInterviewStatus.PENDING);

        Double averageScore = sessionRepository.getAverageScore();
        Integer maxScore = sessionRepository.getMaxScore();
        Integer minScore = sessionRepository.getMinScore();

        double passRate = completedCount > 0 ? (passedCount * 100.0 / completedCount) : 0.0;

        return InterviewStatisticsDTO.builder()
                .totalCount(totalCount)
                .passedCount(passedCount)
                .failedCount(failedCount)
                .inProgressCount(inProgressCount)
                .pendingCount(pendingCount)
                .passRate(Math.round(passRate * 100.0) / 100.0)
                .averageScore(averageScore != null ? Math.round(averageScore * 100.0) / 100.0 : null)
                .maxScore(maxScore)
                .minScore(minScore)
                .build();
    }

    @Override
    public List<ViolationTypeDTO> getViolationTypes() {
        return Arrays.asList(
                ViolationTypeDTO.builder()
                        .code("ABUSE")
                        .name("辱骂行为")
                        .description("对他人进行言语攻击或侮辱")
                        .severity(5)
                        .build(),
                ViolationTypeDTO.builder()
                        .code("HARASSMENT")
                        .name("骚扰行为")
                        .description("持续性的不当言行或跟踪")
                        .severity(4)
                        .build(),
                ViolationTypeDTO.builder()
                        .code("RULE_VIOLATION")
                        .name("违反规则")
                        .description("违反社区或组织规定")
                        .severity(3)
                        .build(),
                ViolationTypeDTO.builder()
                        .code("INAPPROPRIATE_CONTENT")
                        .name("不当内容")
                        .description("发布不适当或违规内容")
                        .severity(3)
                        .build(),
                ViolationTypeDTO.builder()
                        .code("FRAUD")
                        .name("欺诈行为")
                        .description("欺骗或误导他人")
                        .severity(5)
                        .build(),
                ViolationTypeDTO.builder()
                        .code("OTHER")
                        .name("其他违规")
                        .description("其他类型的违规行为")
                        .severity(2)
                        .build()
        );
    }

    private AIInterviewSessionDTO toSessionDTOWithScore(AIInterviewSession session) {
        AIInterviewSessionDTO dto = AIInterviewSessionDTO.fromEntity(session);
        scoreRepository.findBySessionId(session.getId())
                .ifPresent(score -> dto.setScore(AIInterviewScoreDTO.fromEntity(score)));
        return dto;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return ((User) authentication.getPrincipal()).getId();
        }
        throw new BusinessException("用户未登录");
    }
}
