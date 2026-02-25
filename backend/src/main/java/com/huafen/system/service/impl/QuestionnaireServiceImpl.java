package com.huafen.system.service.impl;

import com.huafen.system.common.ResultCode;
import com.huafen.system.entity.Questionnaire;
import com.huafen.system.entity.QuestionnaireField;
import com.huafen.system.entity.QuestionnaireResponse;
import com.huafen.system.entity.User;
import com.huafen.system.entity.enums.AccessType;
import com.huafen.system.entity.enums.QuestionnaireStatus;
import com.huafen.system.entity.enums.ResponseStatus;
import com.huafen.system.exception.BusinessException;
import com.huafen.system.repository.QuestionnaireFieldRepository;
import com.huafen.system.repository.QuestionnaireRepository;
import com.huafen.system.repository.QuestionnaireResponseRepository;
import com.huafen.system.repository.UserRepository;
import com.huafen.system.service.QuestionnaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

/**
 * 问卷服务实现
 */
@Service
@RequiredArgsConstructor
public class QuestionnaireServiceImpl implements QuestionnaireService {

    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionnaireFieldRepository questionnaireFieldRepository;
    private final QuestionnaireResponseRepository questionnaireResponseRepository;
    private final UserRepository userRepository;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    @Transactional
    public Questionnaire createQuestionnaire(String title, String description, List<QuestionnaireField> fields) {
        User currentUser = getCurrentUser();

        Questionnaire questionnaire = Questionnaire.builder()
                .title(title)
                .description(description)
                .status(QuestionnaireStatus.DRAFT)
                .accessType(AccessType.INTERNAL)
                .version(1)
                .createdBy(currentUser)
                .build();

        Questionnaire saved = questionnaireRepository.save(questionnaire);

        if (fields != null && !fields.isEmpty()) {
            for (int i = 0; i < fields.size(); i++) {
                QuestionnaireField field = fields.get(i);
                field.setQuestionnaire(saved);
                field.setSortOrder(i);
            }
            questionnaireFieldRepository.saveAll(fields);
        }

        return saved;
    }

    @Override
    @Transactional
    public Questionnaire updateQuestionnaire(Long id, String title, String description, List<QuestionnaireField> fields) {
        Questionnaire questionnaire = questionnaireRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "问卷不存在"));

        // 已发布的问卷更新时需要增加版本号
        if (questionnaire.getStatus() == QuestionnaireStatus.PUBLISHED) {
            questionnaire.setVersion(questionnaire.getVersion() + 1);
        }

        if (title != null) {
            questionnaire.setTitle(title);
        }
        if (description != null) {
            questionnaire.setDescription(description);
        }

        Questionnaire saved = questionnaireRepository.save(questionnaire);

        // 更新字段
        if (fields != null) {
            questionnaireFieldRepository.deleteByQuestionnaireId(id);
            for (int i = 0; i < fields.size(); i++) {
                QuestionnaireField field = fields.get(i);
                field.setId(null);
                field.setQuestionnaire(saved);
                field.setSortOrder(i);
            }
            questionnaireFieldRepository.saveAll(fields);
        }

        return saved;
    }

    @Override
    @Transactional
    public void deleteQuestionnaire(Long id) {
        Questionnaire questionnaire = questionnaireRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "问卷不存在"));
        questionnaireRepository.delete(questionnaire);
    }

    @Override
    public Questionnaire getById(Long id) {
        return questionnaireRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "问卷不存在"));
    }

    @Override
    public Page<Questionnaire> getQuestionnaires(Pageable pageable) {
        return questionnaireRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Questionnaire publishQuestionnaire(Long id) {
        Questionnaire questionnaire = questionnaireRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "问卷不存在"));

        if (questionnaire.getStatus() == QuestionnaireStatus.ARCHIVED) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "已归档的问卷不能发布");
        }

        questionnaire.setStatus(QuestionnaireStatus.PUBLISHED);

        // 生成公开访问令牌
        if (questionnaire.getPublicToken() == null) {
            questionnaire.setPublicToken(generatePublicToken());
            questionnaire.setAccessType(AccessType.PUBLIC_LINK);
        }

        return questionnaireRepository.save(questionnaire);
    }

    @Override
    @Transactional
    public Questionnaire archiveQuestionnaire(Long id) {
        Questionnaire questionnaire = questionnaireRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "问卷不存在"));

        questionnaire.setStatus(QuestionnaireStatus.ARCHIVED);
        return questionnaireRepository.save(questionnaire);
    }

    @Override
    public Questionnaire getByPublicToken(String publicToken) {
        Questionnaire questionnaire = questionnaireRepository.findByPublicToken(publicToken)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "问卷不存在或链接无效"));

        if (questionnaire.getStatus() != QuestionnaireStatus.PUBLISHED) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "问卷当前不可访问");
        }

        return questionnaire;
    }

    @Override
    @Transactional
    public QuestionnaireResponse submitResponse(Long questionnaireId, String answers, String respondentInfo, String ipAddress, String userAgent) {
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "问卷不存在"));

        if (questionnaire.getStatus() != QuestionnaireStatus.PUBLISHED) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "问卷当前不接受提交");
        }

        User currentUser = getCurrentUserOrNull();

        QuestionnaireResponse response = QuestionnaireResponse.builder()
                .questionnaire(questionnaire)
                .questionnaireVersion(questionnaire.getVersion())
                .user(currentUser)
                .answers(answers)
                .respondentInfo(respondentInfo)
                .status(ResponseStatus.SUBMITTED)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        return questionnaireResponseRepository.save(response);
    }

    @Override
    @Transactional
    public QuestionnaireResponse submitResponseByToken(String publicToken, String answers, String respondentInfo, String ipAddress, String userAgent) {
        Questionnaire questionnaire = getByPublicToken(publicToken);
        return submitResponse(questionnaire.getId(), answers, respondentInfo, ipAddress, userAgent);
    }

    @Override
    public Page<QuestionnaireResponse> getResponses(Long questionnaireId, Pageable pageable) {
        if (!questionnaireRepository.existsById(questionnaireId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "问卷不存在");
        }
        return questionnaireResponseRepository.findByQuestionnaireId(questionnaireId, pageable);
    }

    @Override
    public QuestionnaireResponse getResponseById(Long responseId) {
        return questionnaireResponseRepository.findById(responseId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "响应不存在"));
    }

    @Override
    public long countResponses(Long questionnaireId) {
        return questionnaireResponseRepository.countByQuestionnaireId(questionnaireId);
    }

    /**
     * 生成安全的公开访问令牌
     */
    private String generatePublicToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        // 确保令牌唯一
        while (questionnaireRepository.existsByPublicToken(token)) {
            SECURE_RANDOM.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        }
        return token;
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

    private User getCurrentUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }
}
