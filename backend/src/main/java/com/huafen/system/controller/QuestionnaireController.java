package com.huafen.system.controller;

import com.huafen.system.common.Result;
import com.huafen.system.dto.questionnaire.*;
import com.huafen.system.service.QuestionnaireService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 问卷管理控制器
 */
@RestController
@RequestMapping("/api/questionnaires")
@RequiredArgsConstructor
@Tag(name = "问卷管理")
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;

    /**
     * 分页查询问卷列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER', 'VICE_LEADER')")
    @Operation(summary = "分页查询问卷列表")
    public Result<Page<QuestionnaireDTO>> list(QuestionnaireQuery query, Pageable pageable) {
        return Result.success(questionnaireService.getQuestionnaires(query, pageable));
    }

    /**
     * 获取问卷详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取问卷详情")
    public Result<QuestionnaireDTO> getById(@PathVariable Long id) {
        return Result.success(questionnaireService.getById(id));
    }

    /**
     * 创建问卷
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER', 'VICE_LEADER')")
    @Operation(summary = "创建问卷")
    public Result<QuestionnaireDTO> create(@Valid @RequestBody QuestionnaireCreateDTO dto) {
        return Result.success(questionnaireService.create(dto));
    }

    /**
     * 更新问卷
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER', 'VICE_LEADER')")
    @Operation(summary = "更新问卷")
    public Result<QuestionnaireDTO> update(@PathVariable Long id, @Valid @RequestBody QuestionnaireUpdateDTO dto) {
        return Result.success(questionnaireService.update(id, dto));
    }

    /**
     * 删除问卷
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    @Operation(summary = "删除问卷")
    public Result<Void> delete(@PathVariable Long id) {
        questionnaireService.delete(id);
        return Result.success();
    }

    /**
     * 发布问卷
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER', 'VICE_LEADER')")
    @Operation(summary = "发布问卷")
    public Result<QuestionnaireDTO> publish(@PathVariable Long id) {
        return Result.success(questionnaireService.publish(id));
    }

    /**
     * 通过公开token获取问卷（无需认证）
     */
    @GetMapping("/public/{token}")
    @Operation(summary = "通过公开token获取问卷")
    public Result<QuestionnaireDTO> getByPublicToken(@PathVariable String token) {
        return Result.success(questionnaireService.getByPublicToken(token));
    }

    /**
     * 提交问卷响应
     */
    @PostMapping("/{id}/responses")
    @Operation(summary = "提交问卷响应")
    public Result<QuestionnaireResponseDTO> submitResponse(
            @PathVariable Long id,
            @RequestBody Map<String, Object> answers,
            HttpServletRequest request) {
        return Result.success(questionnaireService.submitResponse(id, answers, request));
    }

    /**
     * 获取问卷响应列表
     */
    @GetMapping("/{id}/responses")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER', 'VICE_LEADER')")
    @Operation(summary = "获取问卷响应列表")
    public Result<Page<QuestionnaireResponseDTO>> getResponses(@PathVariable Long id, Pageable pageable) {
        return Result.success(questionnaireService.getResponses(id, pageable));
    }
}
