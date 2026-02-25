package com.huafen.system.service;

import com.huafen.system.dto.interview.InterviewDTO;
import com.huafen.system.dto.interview.InterviewQuestionDTO;
import com.huafen.system.dto.interview.InterviewSubmitRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 面试服务接口
 */
public interface InterviewService {

    /**
     * 开始面试
     */
    InterviewDTO start(Long applicationId);

    /**
     * 获取面试题目
     */
    List<InterviewQuestionDTO> getQuestions(Long interviewId);

    /**
     * 提交面试答案
     */
    InterviewDTO submit(Long interviewId, InterviewSubmitRequest request);

    /**
     * 根据ID获取面试记录
     */
    InterviewDTO getById(Long id);

    /**
     * 分页查询面试记录
     */
    Page<InterviewDTO> list(Long userId, String status, Pageable pageable);
}
