package com.huafen.system.service;

import com.huafen.system.dto.interview.InterviewQuestionDTO;
import com.huafen.system.dto.interview.QuestionCreateRequest;
import com.huafen.system.entity.InterviewQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 面试题库服务接口
 */
public interface InterviewQuestionService {

    /**
     * 创建题目
     */
    InterviewQuestion create(QuestionCreateRequest request);

    /**
     * 更新题目
     */
    InterviewQuestion update(Long id, QuestionCreateRequest request);

    /**
     * 删除题目
     */
    void delete(Long id);

    /**
     * 根据ID获取题目
     */
    InterviewQuestion getById(Long id);

    /**
     * 分页查询题目
     */
    Page<InterviewQuestion> list(String category, Boolean enabled, Pageable pageable);

    /**
     * 获取所有启用的题目
     */
    List<InterviewQuestion> getEnabledQuestions();

    /**
     * 根据分类获取启用的题目
     */
    List<InterviewQuestion> getEnabledQuestionsByCategory(String category);
}
