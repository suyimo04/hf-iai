package com.huafen.system.controller;

import com.huafen.system.common.Result;
import com.huafen.system.dto.interview.QuestionCreateRequest;
import com.huafen.system.entity.InterviewQuestion;
import com.huafen.system.service.InterviewQuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 面试题库管理控制器
 */
@RestController
@RequestMapping("/api/interview-questions")
@RequiredArgsConstructor
public class InterviewQuestionController {

    private final InterviewQuestionService questionService;

    /**
     * 创建题目
     */
    @PostMapping
    public Result<InterviewQuestion> create(@Valid @RequestBody QuestionCreateRequest request) {
        InterviewQuestion question = questionService.create(request);
        return Result.success(question);
    }

    /**
     * 更新题目
     */
    @PutMapping("/{id}")
    public Result<InterviewQuestion> update(@PathVariable Long id, @Valid @RequestBody QuestionCreateRequest request) {
        InterviewQuestion question = questionService.update(id, request);
        return Result.success(question);
    }

    /**
     * 删除题目
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return Result.success();
    }

    /**
     * 获取题目详情
     */
    @GetMapping("/{id}")
    public Result<InterviewQuestion> getById(@PathVariable Long id) {
        InterviewQuestion question = questionService.getById(id);
        return Result.success(question);
    }

    /**
     * 分页查询题目
     */
    @GetMapping
    public Result<Page<InterviewQuestion>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean enabled,
            Pageable pageable) {
        Page<InterviewQuestion> page = questionService.list(category, enabled, pageable);
        return Result.success(page);
    }

    /**
     * 获取所有启用的题目
     */
    @GetMapping("/enabled")
    public Result<List<InterviewQuestion>> getEnabled() {
        List<InterviewQuestion> questions = questionService.getEnabledQuestions();
        return Result.success(questions);
    }
}
