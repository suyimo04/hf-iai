package com.huafen.system.repository;

import com.huafen.system.entity.Application;
import com.huafen.system.entity.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long>, JpaSpecificationExecutor<Application> {

    List<Application> findByUserId(Long userId);

    List<Application> findByStatus(ApplicationStatus status);

    List<Application> findByReviewerId(Long reviewerId);

    long countByStatus(ApplicationStatus status);

    @Query("SELECT FUNCTION('DATE', a.createdAt) as date, COUNT(a) as count FROM Application a WHERE a.createdAt >= :startDate GROUP BY FUNCTION('DATE', a.createdAt) ORDER BY date")
    List<Object[]> countByDateRange(@Param("startDate") LocalDateTime startDate);
}
