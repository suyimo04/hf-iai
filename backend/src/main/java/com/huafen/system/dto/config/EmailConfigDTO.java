package com.huafen.system.dto.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 邮件配置测试请求DTO
 */
@Data
public class EmailConfigDTO {

    /**
     * SMTP服务器地址
     */
    @NotBlank(message = "SMTP服务器地址不能为空")
    private String host;

    /**
     * 端口
     */
    @NotNull(message = "端口不能为空")
    private Integer port;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 测试收件人地址
     */
    @NotBlank(message = "测试收件人地址不能为空")
    @Email(message = "收件人地址格式不正确")
    private String testTo;
}
