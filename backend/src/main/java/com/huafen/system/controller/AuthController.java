package com.huafen.system.controller;

import com.huafen.system.common.Result;
import com.huafen.system.dto.UserDTO;
import com.huafen.system.dto.auth.LoginRequest;
import com.huafen.system.dto.auth.LoginResponse;
import com.huafen.system.dto.auth.RegisterRequest;
import com.huafen.system.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        UserDTO user = authService.register(request);
        return Result.success(user);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    public Result<UserDTO> getCurrentUser() {
        UserDTO user = authService.getCurrentUser();
        return Result.success(user);
    }
}
