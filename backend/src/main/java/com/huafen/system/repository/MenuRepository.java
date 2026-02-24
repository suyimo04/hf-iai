package com.huafen.system.repository;

import com.huafen.system.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long>, JpaSpecificationExecutor<Menu> {

    List<Menu> findByParentIdIsNullOrParentIdOrderBySortOrderAsc(Long parentId);

    List<Menu> findByParentIdOrderBySortOrderAsc(Long parentId);

    List<Menu> findAllByOrderBySortOrderAsc();

    @Query("SELECT m FROM Menu m JOIN RoleMenu rm ON m.id = rm.menu.id WHERE rm.role = :role AND m.status = true ORDER BY m.sortOrder")
    List<Menu> findByRole(@Param("role") String role);

    List<Menu> findByStatusTrueOrderBySortOrderAsc();

    boolean existsByParentId(Long parentId);
}
