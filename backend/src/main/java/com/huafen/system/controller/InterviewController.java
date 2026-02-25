package com.huafen.system.controller;

import com.huafen.system.common.Result;
import com.huafen.system.dto.interview.InterviewDTO;
import com.huafen.system.dto.interview.InterviewQuestionDTO;
import com.huafen.system.dto.interview.InterviewStartRequest;
import com.huafen.system.dto.interview.InterviewSubmitRequest;
import com.huafen.system.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 面试控制器
 */
@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    /**
     * 开始面试
     */
    @PostMapping("/start")
    public Result<InterviewDTO> start(@Valid @RequestBody InterviewStartRequest request) {
        InterviewDTO interview = interviewService.start(request.getApplicationId());
        return Result.success(interview);
    }

    /**
     * 获取面试题目
     */
    @GetMapping("/{id}/questions")
    public Result<List<InterviewQuestionDTO>> getQuestions(@PathVariable Long id) {
        List<InterviewQuestionDTO> questions = interviewService.getQuestions(id);
        return Result.success(questions);
    }

    /**
     * 提交面试答案
     */
    @PostMapping("/{id}/submit")
    public Result<InterviewDTO> submit(@PathVariable Long id, @Valid @RequestBody InterviewSubmitRequest request) {
        InterviewDTO interview = interviewService.submit(id, request);
        return Result.success(interview);
    }

    /**
     * 获取面试详情
     */
    @GetMapping("/{id}")
    public Result<InterviewDTO> getById(@PathVariable Long id) {
        InterviewDTO interview = interviewService.getById(id);
        return Result.success(interview);
    }

    /**
     * 分页查询面试记录
     */
    @GetMapping
    public Result<Page<InterviewDTO>> list(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Page<InterviewDTO> page = interviewService.list(userId, status, pageable);
        return Result.success(page);
    }
}
