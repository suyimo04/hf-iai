package com.huafen.system.aspect;

import com.huafen.system.annotation.OperationLog;
import com.huafen.system.entity.enums.LogCategory;
import com.huafen.system.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 操作日志AOP切面
 * 拦截@OperationLog注解的方法，自动记录日志
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;

    @AfterReturning(pointcut = "@annotation(com.huafen.system.annotation.OperationLog)", returning = "result")
    public void logOperation(JoinPoint joinPoint, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            OperationLog annotation = method.getAnnotation(OperationLog.class);

            LogCategory category = annotation.category();
            String action = annotation.action();
            String targetType = annotation.targetType();
            String description = annotation.description();
            Long targetId = extractTargetId(joinPoint, result);
            String detail = buildDetail(joinPoint, result, description);

            operationLogService.log(category, action, targetType, targetId, detail);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }

    /**
     * 提取目标ID
     * 优先从返回值中获取，其次从参数中获取
     */
    private Long extractTargetId(JoinPoint joinPoint, Object result) {
        // 尝试从返回值获取ID
        if (result != null) {
            try {
                Method getId = result.getClass().getMethod("getId");
                Object id = getId.invoke(result);
                if (id instanceof Long) {
                    return (Long) id;
                }
            } catch (Exception ignored) {
            }
        }

        // 尝试从第一个Long类型参数获取
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }

        return null;
    }

    /**
     * 构建操作详情
     */
    private String buildDetail(JoinPoint joinPoint, Object result, String description) {
        if (description != null && !description.isEmpty()) {
            return description;
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        return className + "." + methodName;
    }
}
