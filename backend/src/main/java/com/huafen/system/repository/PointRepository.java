package com.huafen.system.repository;

import com.huafen.system.entity.Point;
import com.huafen.system.entity.enums.PointType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointRepository extends JpaRepository<Point, Long>, JpaSpecificationExecutor<Point> {

    List<Point> findByUserId(Long userId);

    List<Point> findByUserIdAndType(Long userId, PointType type);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Point p WHERE p.userId = :userId")
    Integer sumByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Point p")
    Long sumAllPoints();

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Point p WHERE p.createdAt >= :startDate")
    Long sumPointsSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT FUNCTION('DATE', p.createdAt) as date, COALESCE(SUM(p.amount), 0) as total FROM Point p WHERE p.createdAt >= :startDate GROUP BY FUNCTION('DATE', p.createdAt) ORDER BY date")
    List<Object[]> sumByDateRange(@Param("startDate") LocalDateTime startDate);
}
