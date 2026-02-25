package com.huafen.system.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huafen.system.common.ResultCode;
import com.huafen.system.dto.interview.InterviewDTO;
import com.huafen.system.dto.interview.InterviewQuestionDTO;
import com.huafen.system.dto.interview.InterviewSubmitRequest;
import com.huafen.system.entity.Application;
import com.huafen.system.entity.Interview;
import com.huafen.system.entity.InterviewQuestion;
import com.huafen.system.entity.User;
import com.huafen.system.entity.enums.InterviewStatus;
import com.huafen.system.exception.BusinessException;
import com.huafen.system.repository.ApplicationRepository;
import com.huafen.system.repository.InterviewRepository;
import com.huafen.system.repository.UserRepository;
import com.huafen.system.service.InterviewQuestionService;
import com.huafen.system.service.InterviewService;
import com.huafen.system.service.ai.AIProvider;
import com.huafen.system.service.ai.AIProviderFactory;
import com.huafen.system.service.ai.InterviewAnswer;
import com.huafen.system.service.ai.InterviewResult;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 面试服务实现
 */
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final InterviewQuestionService questionService;
    private final AIProviderFactory aiProviderFactory;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public InterviewDTO start(Long applicationId) {
        // 获取当前用户
        User currentUser = getCurrentUser();

        // 检查申请是否存在
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "申请不存在"));

        // 检查是否是本人的申请
        if (!application.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此申请");
        }

        // 检查是否已有面试记录
        if (interviewRepository.findByApplication_Id(applicationId).isPresent()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该申请已有面试记录");
        }

        // 创建面试记录
        Interview interview = Interview.builder()
                .user(currentUser)
                .application(application)
                .status(InterviewStatus.IN_PROGRESS)
                .build();

        Interview saved = interviewRepository.save(interview);
        return InterviewDTO.fromEntity(saved);
    }

    @Override
    public List<InterviewQuestionDTO> getQuestions(Long interviewId) {
        // 验证面试记录存在
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "面试记录不存在"));

        // 检查面试状态
        if (interview.getStatus() == InterviewStatus.COMPLETED) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "面试已完成");
        }

        // 获取所有启用的题目（不含答案）
        List<InterviewQuestion> questions = questionService.getEnabledQuestions();
        return questions.stream()
                .map(InterviewQuestionDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InterviewDTO submit(Long interviewId, InterviewSubmitRequest request) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "面试记录不存在"));

        // 检查面试状态
        if (interview.getStatus() == InterviewStatus.COMPLETED) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "面试已完成，不能重复提交");
        }

        // 获取所有题目用于评分
        List<InterviewQuestion> questions = questionService.getEnabledQuestions();
        List<InterviewAnswer> answers = request.getAnswers();

        // 使用AI评分
        AIProvider provider = aiProviderFactory.getDefaultProvider();
        InterviewResult result = provider.evaluate(answers, questions);

        // 保存答案JSON
        try {
            interview.setAnswers(objectMapper.writeValueAsString(answers));
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.ERROR, "答案序列化失败");
        }

        // 更新面试记录
        interview.setScore(result.getScore());
        interview.setReport(result.getReport());
        interview.setStatus(InterviewStatus.COMPLETED);

        Interview saved = interviewRepository.save(interview);
        return InterviewDTO.fromEntity(saved);
    }

    @Override
    public InterviewDTO getById(Long id) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "面试记录不存在"));
        return InterviewDTO.fromEntity(interview);
    }

    @Override
    public Page<InterviewDTO> list(Long userId, String status, Pageable pageable) {
        Specification<Interview> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), InterviewStatus.valueOf(status)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return interviewRepository.findAll(spec, pageable).map(InterviewDTO::fromEntity);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户不存在"));
    }
}
