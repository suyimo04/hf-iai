package com.huafen.system.service.impl;

import com.huafen.system.common.ResultCode;
import com.huafen.system.dto.menu.PermissionCreateRequest;
import com.huafen.system.entity.*;
import com.huafen.system.entity.enums.ChangeType;
import com.huafen.system.exception.BusinessException;
import com.huafen.system.repository.*;
import com.huafen.system.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限服务实现
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;
    private final RoleChangeLogRepository roleChangeLogRepository;
    private final PermissionChangeLogRepository permissionChangeLogRepository;

    @Override
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @Override
    public List<Permission> getPermissionsByRole(String role) {
        return permissionRepository.findByRole(role);
    }

    @Override
    public List<String> getPermissionCodesByRole(String role) {
        return permissionRepository.findCodesByRole(role);
    }

    @Override
    @Transactional
    public Permission createPermission(PermissionCreateRequest request) {
        // 检查权限编码是否已存在
        if (permissionRepository.existsByCode(request.getCode())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "权限编码已存在");
        }

        Menu menu = null;
        if (request.getMenuId() != null) {
            menu = menuRepository.findById(request.getMenuId())
                    .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "关联菜单不存在"));
        }

        Permission permission = Permission.builder()
                .menu(menu)
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .build();

        return permissionRepository.save(permission);
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "权限不存在"));

        // 删除角色权限关联
        List<RolePermission> rolePermissions = rolePermissionRepository.findAll().stream()
                .filter(rp -> rp.getPermission().getId().equals(id))
                .toList();
        rolePermissionRepository.deleteAll(rolePermissions);

        permissionRepository.delete(permission);
    }

    @Override
    @Transactional
    public void assignPermissionsToRole(String role, List<Long> permissionIds) {
        // 删除该角色的所有权限关联
        rolePermissionRepository.deleteByRole(role);

        // 创建新的关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<Permission> permissions = permissionRepository.findAllById(permissionIds);
            List<RolePermission> rolePermissions = permissions.stream()
                    .map(permission -> RolePermission.builder()
                            .role(role)
                            .permission(permission)
                            .build())
                    .collect(Collectors.toList());
            rolePermissionRepository.saveAll(rolePermissions);
        }
    }

    @Override
    @Transactional
    public void logRoleChange(Long userId, String fromRole, String toRole, String reason, Long changedBy, String ip) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户不存在"));

        RoleChangeLog log = RoleChangeLog.builder()
                .user(user)
                .fromRole(fromRole)
                .toRole(toRole)
                .reason(reason)
                .changedBy(changedBy)
                .ipAddress(ip)
                .build();

        roleChangeLogRepository.save(log);
    }

    @Override
    @Transactional
    public void logPermissionChange(String role, ChangeType type, Long targetId, String targetName, Long changedBy, String ip) {
        PermissionChangeLog log = PermissionChangeLog.builder()
                .role(role)
                .changeType(type)
                .targetId(targetId)
                .targetName(targetName)
                .changedBy(changedBy)
                .ipAddress(ip)
                .build();

        permissionChangeLogRepository.save(log);
    }
}
