package com.huafen.system.repository;

import com.huafen.system.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByRole(String role);

    void deleteByRole(String role);

    void deleteByRoleAndPermission_Id(String role, Long permissionId);

    boolean existsByRoleAndPermission_Id(String role, Long permissionId);
}
