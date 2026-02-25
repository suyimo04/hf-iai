package com.huafen.system.entity.enums;

/**
 * 报名状态枚举
 */
public enum ApplicationStatus {
    PENDING,    // 待审核
    REVIEWING,  // 审核中
    INTERVIEW,  // 面试中
    PASSED,     // 已通过
    REJECTED,   // 已拒绝
    INTERN,     // 实习期
    CONVERTED   // 已转正
}
