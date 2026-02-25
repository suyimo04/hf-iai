package com.huafen.system.service.impl;

import com.huafen.system.common.ResultCode;
import com.huafen.system.dto.UserDTO;
import com.huafen.system.dto.auth.LoginRequest;
import com.huafen.system.dto.auth.LoginResponse;
import com.huafen.system.dto.auth.RegisterRequest;
import com.huafen.system.entity.User;
import com.huafen.system.entity.enums.Role;
import com.huafen.system.entity.enums.UserStatus;
import com.huafen.system.exception.BusinessException;
import com.huafen.system.repository.UserRepository;
import com.huafen.system.security.JwtTokenProvider;
import com.huafen.system.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 先获取用户信息
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误"));

        // 先检查用户状态（在验证密码之前）
        if (user.getStatus() == UserStatus.BANNED) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }

        // 再验证用户名密码
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 生成JWT令牌
        String token = jwtTokenProvider.generateToken(authentication);

        return LoginResponse.builder()
                .token(token)
                .user(UserDTO.fromEntity(user))
                .build();
    }

    @Override
    @Transactional
    public UserDTO register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名已存在");
        }

        // 创建用户
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(Role.APPLICANT)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);
        return UserDTO.fromEntity(savedUser);
    }

    @Override
    public UserDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户不存在"));

        return UserDTO.fromEntity(user);
    }
}
