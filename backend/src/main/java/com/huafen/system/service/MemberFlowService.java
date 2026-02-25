package com.huafen.system.service;

import com.huafen.system.entity.MemberFlowLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 成员流转服务接口
 */
public interface MemberFlowService {

    /**
     * 检查用户是否可以转正
     * 条件：积分>=100 且 正式成员数<5
     */
    boolean canPromote(Long userId);

    /**
     * 申请转正（需审批）
     */
    MemberFlowLog applyPromotion(Long userId, String reason);

    /**
     * 审批转正
     */
    void approvePromotion(Long flowLogId, Long approverId, boolean approved, String comment);

    /**
     * 检查用户是否需要降级
     * 条件：连续2月积分<150
     */
    boolean shouldDemote(Long userId);

    /**
     * 申请降级（需审批）
     */
    MemberFlowLog applyDemotion(Long userId, String reason);

    /**
     * 审批降级
     */
    void approveDemotion(Long flowLogId, Long approverId, boolean approved, String comment);

    /**
     * 自动检查并开除表现不佳的实习生
     * 条件：连续2月积分<100
     * 定时任务：每月1号凌晨1点执行
     */
    void autoRemoveUnderperformingInterns();

    /**
     * 获取当前正式成员数
     */
    int getFormalMemberCount();

    /**
     * 获取流转日志（分页）
     */
    Page<MemberFlowLog> getFlowLogs(Pageable pageable);

    /**
     * 获取用户指定月份的积分
     */
    int getMonthlyPoints(Long userId, int year, int month);
}
