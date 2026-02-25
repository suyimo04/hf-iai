package com.huafen.system.entity.enums;

/**
 * 流转状态枚举
 */
public enum FlowStatus {
    PENDING,   // 待审批
    APPROVED,  // 已通过
    REJECTED,  // 已拒绝
    AUTO       // 自动执行（用于自动开除）
}
