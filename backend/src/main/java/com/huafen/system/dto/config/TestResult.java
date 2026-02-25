package com.huafen.system.dto.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 配置测试结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestResult {

    private boolean success;
    private String message;

    public static TestResult success(String msg) {
        return new TestResult(true, msg);
    }

    public static TestResult fail(String msg) {
        return new TestResult(false, msg);
    }
}
