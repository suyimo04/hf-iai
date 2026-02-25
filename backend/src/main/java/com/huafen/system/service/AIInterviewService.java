package com.huafen.system.service;

import com.huafen.system.dto.interview.AIInterviewDetailDTO;
import com.huafen.system.dto.interview.AIInterviewSessionDTO;
import com.huafen.system.dto.interview.InterviewStatisticsDTO;
import com.huafen.system.dto.interview.ViolationTypeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * AI面试服务接口
 */
public interface AIInterviewService {

    /**
     * 分页查询面试会话列表
     */
    Page<AIInterviewSessionDTO> listSessions(Pageable pageable);

    /**
     * 获取当前用户的面试记录
     */
    List<AIInterviewSessionDTO> getMySessions();

    /**
     * 获取面试详情（含消息历史和评分）
     */
    AIInterviewDetailDTO getSessionDetail(Long sessionId);

    /**
     * 获取面试统计数据
     */
    InterviewStatisticsDTO getStatistics();

    /**
     * 获取违规类型列表
     */
    List<ViolationTypeDTO> getViolationTypes();
}
