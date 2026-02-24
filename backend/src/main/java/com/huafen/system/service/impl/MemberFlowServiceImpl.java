package com.huafen.system.service.impl;

import com.huafen.system.common.ResultCode;
import com.huafen.system.entity.MemberFlowLog;
import com.huafen.system.entity.User;
import com.huafen.system.entity.enums.FlowStatus;
import com.huafen.system.entity.enums.Role;
import com.huafen.system.entity.enums.TriggerType;
import com.huafen.system.exception.BusinessException;
import com.huafen.system.repository.MemberFlowLogRepository;
import com.huafen.system.repository.PointRepository;
import com.huafen.system.repository.UserRepository;
import com.huafen.system.service.MemberFlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/**
 * 成员流转服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberFlowServiceImpl implements MemberFlowService {

    private static final int MAX_FORMAL_MEMBERS = 5;
    private static final int PROMOTION_POINTS_THRESHOLD = 100;
    private static final int DEMOTION_POINTS_THRESHOLD = 150;
    private static final int REMOVAL_POINTS_THRESHOLD = 100;

    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final MemberFlowLogRepository memberFlowLogRepository;

    @Override
    public boolean canPromote(Long userId) {
        User user = getUserById(userId);

        // 必须是实习成员
        if (user.getRole() != Role.INTERN) {
            return false;
        }

        // 检查积分>=100
        Integer totalPoints = pointRepository.sumByUserId(userId);
        if (totalPoints == null || totalPoints < PROMOTION_POINTS_THRESHOLD) {
            return false;
        }

        // 检查正式成员数<5
        return getFormalMemberCount() < MAX_FORMAL_MEMBERS;
    }

    @Override
    @Transactional
    public MemberFlowLog applyPromotion(Long userId, String reason) {
        User user = getUserById(userId);

        // 验证是否为实习成员
        if (user.getRole() != Role.INTERN) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "只有实习成员可以申请转正");
        }

        // 检查是否已有待审批的申请
        if (memberFlowLogRepository.existsByUser_IdAndStatus(userId, FlowStatus.PENDING)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "已有待审批的流转申请");
        }

        // 检查是否满足转正条件
        if (!canPromote(userId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不满足转正条件：积分需>=100且正式成员未满");
        }

        MemberFlowLog flowLog = MemberFlowLog.builder()
                .user(user)
                .fromRole(Role.INTERN)
                .toRole(Role.MEMBER)
                .triggerType(TriggerType.MANUAL)
                .status(FlowStatus.PENDING)
                .reason(reason)
                .build();

        return memberFlowLogRepository.save(flowLog);
    }

    @Override
    @Transactional
    public void approvePromotion(Long flowLogId, Long approverId, boolean approved, String comment) {
        MemberFlowLog flowLog = getFlowLogById(flowLogId);
        User approver = getUserById(approverId);

        // 验证状态
        if (flowLog.getStatus() != FlowStatus.PENDING) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该申请已处理");
        }

        // 验证是转正申请
        if (flowLog.getToRole() != Role.MEMBER) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该申请不是转正申请");
        }

        flowLog.setApprovedBy(approver);
        flowLog.setApproveComment(comment);

        if (approved) {
            // 再次检查正式成员数
            if (getFormalMemberCount() >= MAX_FORMAL_MEMBERS) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "正式成员已满，无法转正");
            }

            flowLog.setStatus(FlowStatus.APPROVED);
            // 更新用户角色
            User user = flowLog.getUser();
            user.setRole(Role.MEMBER);
            userRepository.save(user);
        } else {
            flowLog.setStatus(FlowStatus.REJECTED);
        }

        memberFlowLogRepository.save(flowLog);
    }

    @Override
    public boolean shouldDemote(Long userId) {
        User user = getUserById(userId);

        // 必须是正式成员
        if (user.getRole() != Role.MEMBER) {
            return false;
        }

        // 检查连续2月积分<150
        LocalDate now = LocalDate.now();
        YearMonth lastMonth = YearMonth.from(now).minusMonths(1);
        YearMonth twoMonthsAgo = YearMonth.from(now).minusMonths(2);

        int lastMonthPoints = getMonthlyPoints(userId, lastMonth.getYear(), lastMonth.getMonthValue());
        int twoMonthsAgoPoints = getMonthlyPoints(userId, twoMonthsAgo.getYear(), twoMonthsAgo.getMonthValue());

        return lastMonthPoints < DEMOTION_POINTS_THRESHOLD && twoMonthsAgoPoints < DEMOTION_POINTS_THRESHOLD;
    }

    @Override
    @Transactional
    public MemberFlowLog applyDemotion(Long userId, String reason) {
        User user = getUserById(userId);

        // 验证是否为正式成员
        if (user.getRole() != Role.MEMBER) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "只有正式成员可以被降级");
        }

        // 检查是否已有待审批的申请
        if (memberFlowLogRepository.existsByUser_IdAndStatus(userId, FlowStatus.PENDING)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "已有待审批的流转申请");
        }

        MemberFlowLog flowLog = MemberFlowLog.builder()
                .user(user)
                .fromRole(Role.MEMBER)
                .toRole(Role.INTERN)
                .triggerType(TriggerType.MANUAL)
                .status(FlowStatus.PENDING)
                .reason(reason)
                .build();

        return memberFlowLogRepository.save(flowLog);
    }

    @Override
    @Transactional
    public void approveDemotion(Long flowLogId, Long approverId, boolean approved, String comment) {
        MemberFlowLog flowLog = getFlowLogById(flowLogId);
        User approver = getUserById(approverId);

        // 验证状态
        if (flowLog.getStatus() != FlowStatus.PENDING) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该申请已处理");
        }

        // 验证是降级申请
        if (flowLog.getToRole() != Role.INTERN || flowLog.getFromRole() != Role.MEMBER) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该申请不是降级申请");
        }

        flowLog.setApprovedBy(approver);
        flowLog.setApproveComment(comment);

        if (approved) {
            flowLog.setStatus(FlowStatus.APPROVED);
            // 更新用户角色
            User user = flowLog.getUser();
            user.setRole(Role.INTERN);
            userRepository.save(user);
        } else {
            flowLog.setStatus(FlowStatus.REJECTED);
        }

        memberFlowLogRepository.save(flowLog);
    }

    @Override
    @Scheduled(cron = "0 0 1 1 * ?") // 每月1号凌晨1点执行
    @Transactional
    public void autoRemoveUnderperformingInterns() {
        log.info("开始执行自动开除检查任务");

        List<User> interns = userRepository.findByRole(Role.INTERN);
        LocalDate now = LocalDate.now();
        YearMonth lastMonth = YearMonth.from(now).minusMonths(1);
        YearMonth twoMonthsAgo = YearMonth.from(now).minusMonths(2);

        for (User intern : interns) {
            int lastMonthPoints = getMonthlyPoints(intern.getId(), lastMonth.getYear(), lastMonth.getMonthValue());
            int twoMonthsAgoPoints = getMonthlyPoints(intern.getId(), twoMonthsAgo.getYear(), twoMonthsAgo.getMonthValue());

            // 连续2月积分<100，自动开除
            if (lastMonthPoints < REMOVAL_POINTS_THRESHOLD && twoMonthsAgoPoints < REMOVAL_POINTS_THRESHOLD) {
                log.info("用户 {} 连续两月积分不达标（{}, {}），执行自动开除",
                        intern.getUsername(), twoMonthsAgoPoints, lastMonthPoints);

                // 创建流转日志
                MemberFlowLog flowLog = MemberFlowLog.builder()
                        .user(intern)
                        .fromRole(Role.INTERN)
                        .toRole(null) // 开除无目标角色
                        .triggerType(TriggerType.AUTO)
                        .status(FlowStatus.AUTO)
                        .reason(String.format("连续两月积分不达标：%d月%d分，%d月%d分",
                                twoMonthsAgo.getMonthValue(), twoMonthsAgoPoints,
                                lastMonth.getMonthValue(), lastMonthPoints))
                        .build();
                memberFlowLogRepository.save(flowLog);

                // 更新用户状态为封禁
                intern.setStatus(com.huafen.system.entity.enums.UserStatus.BANNED);
                userRepository.save(intern);
            }
        }

        log.info("自动开除检查任务执行完成");
    }

    @Override
    public int getFormalMemberCount() {
        return (int) userRepository.countByRoleIn(List.of(Role.MEMBER));
    }

    @Override
    public Page<MemberFlowLog> getFlowLogs(Pageable pageable) {
        return memberFlowLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    public int getMonthlyPoints(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        return pointRepository.sumByUserIdAndCreatedAtBetween(userId, startDate, endDate);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户不存在"));
    }

    private MemberFlowLog getFlowLogById(Long flowLogId) {
        return memberFlowLogRepository.findById(flowLogId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "流转记录不存在"));
    }
}
