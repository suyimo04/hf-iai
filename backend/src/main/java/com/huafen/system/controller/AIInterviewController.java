package com.huafen.system.controller;

import com.huafen.system.common.Result;
import com.huafen.system.dto.interview.AIInterviewDetailDTO;
import com.huafen.system.dto.interview.AIInterviewSessionDTO;
import com.huafen.system.dto.interview.InterviewStatisticsDTO;
import com.huafen.system.dto.interview.ViolationTypeDTO;
import com.huafen.system.service.AIInterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI面试控制器
 */
@RestController
@RequestMapping("/api/ai-interview")
@RequiredArgsConstructor
public class AIInterviewController {

    private final AIInterviewService aiInterviewService;

    /**
     * 获取面试会话列表（管理员/领导可用）
     */
    @GetMapping("/sessions")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER', 'VICE_LEADER')")
    public Result<Page<AIInterviewSessionDTO>> listSessions(Pageable pageable) {
        Page<AIInterviewSessionDTO> sessions = aiInterviewService.listSessions(pageable);
        return Result.success(sessions);
    }

    /**
     * 获取我的面试记录
     */
    @GetMapping("/my-sessions")
    public Result<List<AIInterviewSessionDTO>> getMySessions() {
        List<AIInterviewSessionDTO> sessions = aiInterviewService.getMySessions();
        return Result.success(sessions);
    }

    /**
     * 获取面试详情（含消息历史）
     */
    @GetMapping("/sessions/{id}")
    public Result<AIInterviewDetailDTO> getSessionDetail(@PathVariable Long id) {
        AIInterviewDetailDTO detail = aiInterviewService.getSessionDetail(id);
        return Result.success(detail);
    }

    /**
     * 获取面试统计（通过率等）
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER', 'VICE_LEADER')")
    public Result<InterviewStatisticsDTO> getStatistics() {
        InterviewStatisticsDTO statistics = aiInterviewService.getStatistics();
        return Result.success(statistics);
    }

    /**
     * 获取违规类型列表
     */
    @GetMapping("/violation-types")
    public Result<List<ViolationTypeDTO>> getViolationTypes() {
        List<ViolationTypeDTO> types = aiInterviewService.getViolationTypes();
        return Result.success(types);
    }
}
