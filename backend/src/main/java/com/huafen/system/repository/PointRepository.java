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

    List<Point> findByUser_Id(Long userId);

    List<Point> findByUser_IdAndType(Long userId, PointType type);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Point p WHERE p.user.id = :userId")
    Integer sumByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Point p")
    Long sumAllPoints();

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Point p WHERE p.createdAt >= :startDate")
    Long sumPointsSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT FUNCTION('DATE', p.createdAt) as date, COALESCE(SUM(p.amount), 0) as total FROM Point p WHERE p.createdAt >= :startDate GROUP BY FUNCTION('DATE', p.createdAt) ORDER BY date")
    List<Object[]> sumByDateRange(@Param("startDate") LocalDateTime startDate);

    /**
     * 统计用户在指定时间范围内的签到次数
     */
    @Query("SELECT COUNT(p) FROM Point p WHERE p.user.id = :userId AND p.type = :type AND p.createdAt >= :startDate AND p.createdAt < :endDate")
    int countByUserIdAndTypeAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("type") PointType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 统计用户在指定时间范围内的总积分
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Point p WHERE p.user.id = :userId AND p.createdAt >= :startDate AND p.createdAt < :endDate")
    int sumByUserIdAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
