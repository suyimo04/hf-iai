package com.huafen.system.service;

import com.huafen.system.dto.UserDTO;
import com.huafen.system.dto.auth.LoginRequest;
import com.huafen.system.dto.auth.LoginResponse;
import com.huafen.system.dto.auth.RegisterRequest;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户注册
     */
    UserDTO register(RegisterRequest request);

    /**
     * 获取当前登录用户信息
     */
    UserDTO getCurrentUser();
}
