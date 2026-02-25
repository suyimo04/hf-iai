package com.huafen.system.service.impl;

import com.huafen.system.common.ResultCode;
import com.huafen.system.dto.interview.InterviewQuestionDTO;
import com.huafen.system.dto.interview.QuestionCreateRequest;
import com.huafen.system.entity.InterviewQuestion;
import com.huafen.system.exception.BusinessException;
import com.huafen.system.repository.InterviewQuestionRepository;
import com.huafen.system.service.InterviewQuestionService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 面试题库服务实现
 */
@Service
@RequiredArgsConstructor
public class InterviewQuestionServiceImpl implements InterviewQuestionService {

    private final InterviewQuestionRepository questionRepository;

    @Override
    @Transactional
    public InterviewQuestion create(QuestionCreateRequest request) {
        InterviewQuestion question = InterviewQuestion.builder()
                .category(request.getCategory())
                .question(request.getQuestion())
                .options(request.getOptions())
                .answer(request.getAnswer())
                .score(request.getScore() != null ? request.getScore() : 10)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .build();

        return questionRepository.save(question);
    }

    @Override
    @Transactional
    public InterviewQuestion update(Long id, QuestionCreateRequest request) {
        InterviewQuestion question = questionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "题目不存在"));

        if (request.getCategory() != null) {
            question.setCategory(request.getCategory());
        }
        if (request.getQuestion() != null) {
            question.setQuestion(request.getQuestion());
        }
        if (request.getOptions() != null) {
            question.setOptions(request.getOptions());
        }
        if (request.getAnswer() != null) {
            question.setAnswer(request.getAnswer());
        }
        if (request.getScore() != null) {
            question.setScore(request.getScore());
        }
        if (request.getSortOrder() != null) {
            question.setSortOrder(request.getSortOrder());
        }
        if (request.getEnabled() != null) {
            question.setEnabled(request.getEnabled());
        }

        return questionRepository.save(question);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "题目不存在");
        }
        questionRepository.deleteById(id);
    }

    @Override
    public InterviewQuestion getById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "题目不存在"));
    }

    @Override
    public Page<InterviewQuestion> list(String category, Boolean enabled, Pageable pageable) {
        Specification<InterviewQuestion> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (category != null && !category.isEmpty()) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (enabled != null) {
                predicates.add(cb.equal(root.get("enabled"), enabled));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return questionRepository.findAll(spec, pageable);
    }

    @Override
    public List<InterviewQuestion> getEnabledQuestions() {
        return questionRepository.findByEnabledTrueOrderBySortOrder();
    }

    @Override
    public List<InterviewQuestion> getEnabledQuestionsByCategory(String category) {
        return questionRepository.findByCategoryAndEnabledTrue(category);
    }
}
