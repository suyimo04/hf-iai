package com.huafen.system.repository;

import com.huafen.system.entity.PermissionChangeLog;
import com.huafen.system.entity.enums.ChangeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionChangeLogRepository extends JpaRepository<PermissionChangeLog, Long>, JpaSpecificationExecutor<PermissionChangeLog> {

    List<PermissionChangeLog> findByRoleOrderByCreatedAtDesc(String role);

    Page<PermissionChangeLog> findByRole(String role, Pageable pageable);

    List<PermissionChangeLog> findByChangeTypeOrderByCreatedAtDesc(ChangeType changeType);

    List<PermissionChangeLog> findByChangedByOrderByCreatedAtDesc(Long changedBy);
}
