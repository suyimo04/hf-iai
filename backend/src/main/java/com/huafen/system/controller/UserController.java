package com.huafen.system.controller;

import com.huafen.system.common.Result;
import com.huafen.system.dto.UserDTO;
import com.huafen.system.dto.user.UpdateRoleRequest;
import com.huafen.system.dto.user.UpdateStatusRequest;
import com.huafen.system.dto.user.UserQueryRequest;
import com.huafen.system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER', 'VICE_LEADER')")
    public Result<Page<UserDTO>> getUsers(UserQueryRequest request) {
        Page<UserDTO> users = userService.getUsers(request);
        return Result.success(users);
    }

    /**
     * 根据ID获取用户详情
     */
    @GetMapping("/{id}")
    public Result<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return Result.success(user);
    }

    /**
     * 修改用户角色
     */
    @PutMapping("/{id}/role")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public Result<UserDTO> updateRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        UserDTO user = userService.updateRole(id, request.getRole());
        return Result.success(user);
    }

    /**
     * 修改用户状态
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public Result<UserDTO> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        UserDTO user = userService.updateStatus(id, request.getStatus());
        return Result.success(user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
}
