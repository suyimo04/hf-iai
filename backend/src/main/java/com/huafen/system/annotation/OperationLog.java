package com.huafen.system.annotation;

import com.huafen.system.entity.enums.LogCategory;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 日志分类
     */
    LogCategory category();

    /**
     * 操作类型
     */
    String action();

    /**
     * 目标类型
     */
    String targetType() default "";

    /**
     * 操作描述
     */
    String description() default "";
}
