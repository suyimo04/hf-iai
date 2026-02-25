package com.huafen.system.repository;

import com.huafen.system.entity.OperationLog;
import com.huafen.system.entity.enums.LogCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long>, JpaSpecificationExecutor<OperationLog> {

    List<OperationLog> findByUser_Id(Long userId);

    List<OperationLog> findByAction(String action);

    List<OperationLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<OperationLog> findByCategory(LogCategory category);

    List<OperationLog> findByCategoryAndCreatedAtBetween(LogCategory category, LocalDateTime start, LocalDateTime end);

    List<OperationLog> findByUser_IdAndCategory(Long userId, LogCategory category);

    long countByCategory(LogCategory category);
}
