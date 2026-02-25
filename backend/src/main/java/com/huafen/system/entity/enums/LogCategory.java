package com.huafen.system.entity.enums;

/**
 * 操作日志分类枚举
 */
public enum LogCategory {
    LOGIN,      // 登录/登出
    DELETE,     // 删除操作
    PERMISSION, // 权限变更
    EMAIL,      // 邮件发送
    OSS,        // 文件上传/删除
    CONFIG,     // 配置修改
    INTERVIEW,  // 面试相关
    SALARY      // 薪酬相关
}
