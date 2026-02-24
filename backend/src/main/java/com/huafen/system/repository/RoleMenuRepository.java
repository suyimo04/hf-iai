package com.huafen.system.repository;

import com.huafen.system.entity.RoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleMenuRepository extends JpaRepository<RoleMenu, Long> {

    List<RoleMenu> findByRole(String role);

    void deleteByRole(String role);

    void deleteByRoleAndMenu_Id(String role, Long menuId);

    boolean existsByRoleAndMenu_Id(String role, Long menuId);
}
