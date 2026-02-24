package com.huafen.system.repository;

import com.huafen.system.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {

    Optional<Permission> findByCode(String code);

    boolean existsByCode(String code);

    List<Permission> findByMenu_Id(Long menuId);

    @Query("SELECT p FROM Permission p JOIN RolePermission rp ON p.id = rp.permission.id WHERE rp.role = :role")
    List<Permission> findByRole(@Param("role") String role);

    @Query("SELECT p.code FROM Permission p JOIN RolePermission rp ON p.id = rp.permission.id WHERE rp.role = :role")
    List<String> findCodesByRole(@Param("role") String role);
}
