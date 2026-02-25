package com.huafen.system.service;

import com.huafen.system.dto.menu.PermissionCreateRequest;
import com.huafen.system.dto.menu.PermissionDTO;
import com.huafen.system.entity.Permission;
import com.huafen.system.entity.enums.ChangeType;

import java.util.List;

/**
 * 权限服务接口
 */
public interface PermissionService {

    /**
     * 获取所有权限
     */
    List<Permission> getAllPermissions();

    /**
     * 根据角色获取权限列表
     */
    List<Permission> getPermissionsByRole(String role);

    /**
     * 根据角色获取权限编码列表
     */
    List<String> getPermissionCodesByRole(String role);

    /**
     * 创建权限
     */
    Permission createPermission(PermissionCreateRequest request);

    /**
     * 删除权限
     */
    void deletePermission(Long id);

    /**
     * 为角色分配权限
     */
    void assignPermissionsToRole(String role, List<Long> permissionIds);

    /**
     * 记录角色变更日志
     */
    void logRoleChange(Long userId, String fromRole, String toRole, String reason, Long changedBy, String ip);

    /**
     * 记录权限变更日志
     */
    void logPermissionChange(String role, ChangeType type, Long targetId, String targetName, Long changedBy, String ip);
}
