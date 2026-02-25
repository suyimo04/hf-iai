package com.huafen.system.controller;

import com.huafen.system.common.Result;
import com.huafen.system.dto.menu.PermissionChangeLogDTO;
import com.huafen.system.dto.menu.PermissionDTO;
import com.huafen.system.dto.menu.RoleChangeLogDTO;
import com.huafen.system.entity.Permission;
import com.huafen.system.entity.PermissionChangeLog;
import com.huafen.system.entity.RoleChangeLog;
import com.huafen.system.entity.User;
import com.huafen.system.repository.PermissionChangeLogRepository;
import com.huafen.system.repository.RoleChangeLogRepository;
import com.huafen.system.repository.UserRepository;
import com.huafen.system.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限管理控制器
 */
@RestController
@RequestMapping("/api/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;
    private final UserRepository userRepository;
    private final RoleChangeLogRepository roleChangeLogRepository;
    private final PermissionChangeLogRepository permissionChangeLogRepository;

    /**
     * 获取权限列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public Result<List<PermissionDTO>> listPermissions() {
        List<Permission> permissions = permissionService.getAllPermissions();
        List<PermissionDTO> dtos = permissions.stream()
                .map(PermissionDTO::fromEntity)
                .collect(Collectors.toList());
        return Result.success(dtos);
    }

    /**
     * 获取当前用户权限码
     */
    @GetMapping("/my-permissions")
    public Result<List<String>> getMyPermissions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return Result.success(List.of());
        }

        List<String> permissionCodes = permissionService.getPermissionCodesByRole(user.getRole().name());
        return Result.success(permissionCodes);
    }

    /**
     * 分配角色权限
     */
    @PostMapping("/role/{role}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> assignRolePermissions(@PathVariable String role, @RequestBody List<Long> permissionIds) {
        permissionService.assignPermissionsToRole(role, permissionIds);
        return Result.success();
    }

    /**
     * 获取角色变更日志
     */
    @GetMapping("/role-change-logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public Result<Page<RoleChangeLogDTO>> getRoleChangeLogs(Pageable pageable) {
        Page<RoleChangeLog> logs = roleChangeLogRepository.findAll(pageable);
        List<RoleChangeLogDTO> dtos = logs.getContent().stream()
                .map(RoleChangeLogDTO::fromEntity)
                .collect(Collectors.toList());
        Page<RoleChangeLogDTO> result = new PageImpl<>(dtos, pageable, logs.getTotalElements());
        return Result.success(result);
    }

    /**
     * 获取权限变更日志
     */
    @GetMapping("/permission-change-logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public Result<Page<PermissionChangeLogDTO>> getPermissionChangeLogs(Pageable pageable) {
        Page<PermissionChangeLog> logs = permissionChangeLogRepository.findAll(pageable);
        List<PermissionChangeLogDTO> dtos = logs.getContent().stream()
                .map(PermissionChangeLogDTO::fromEntity)
                .collect(Collectors.toList());
        Page<PermissionChangeLogDTO> result = new PageImpl<>(dtos, pageable, logs.getTotalElements());
        return Result.success(result);
    }
}
