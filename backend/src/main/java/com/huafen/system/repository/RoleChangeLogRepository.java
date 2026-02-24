package com.huafen.system.repository;

import com.huafen.system.entity.RoleChangeLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleChangeLogRepository extends JpaRepository<RoleChangeLog, Long>, JpaSpecificationExecutor<RoleChangeLog> {

    List<RoleChangeLog> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Page<RoleChangeLog> findByUser_Id(Long userId, Pageable pageable);

    List<RoleChangeLog> findByChangedByOrderByCreatedAtDesc(Long changedBy);
}
