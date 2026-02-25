package com.huafen.system.repository;

import com.huafen.system.entity.AIInterviewSession;
import com.huafen.system.entity.enums.AIInterviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AI面试会话Repository
 */
@Repository
public interface AIInterviewSessionRepository extends JpaRepository<AIInterviewSession, Long> {

    /**
     * 根据用户ID查询面试会话
     */
    List<AIInterviewSession> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 根据状态查询面试会话
     */
    Page<AIInterviewSession> findByStatus(AIInterviewStatus status, Pageable pageable);

    /**
     * 统计指定状态的会话数量
     */
    long countByStatus(AIInterviewStatus status);

    /**
     * 统计已完成且通过的会话数量（finalScore >= 60）
     */
    @Query("SELECT COUNT(s) FROM AIInterviewSession s JOIN AIInterviewScore sc ON sc.session = s WHERE s.status = 'COMPLETED' AND sc.finalScore >= :passScore")
    long countPassedSessions(@Param("passScore") int passScore);

    /**
     * 获取平均分
     */
    @Query("SELECT AVG(sc.finalScore) FROM AIInterviewScore sc JOIN sc.session s WHERE s.status = 'COMPLETED'")
    Double getAverageScore();

    /**
     * 获取最高分
     */
    @Query("SELECT MAX(sc.finalScore) FROM AIInterviewScore sc JOIN sc.session s WHERE s.status = 'COMPLETED'")
    Integer getMaxScore();

    /**
     * 获取最低分
     */
    @Query("SELECT MIN(sc.finalScore) FROM AIInterviewScore sc JOIN sc.session s WHERE s.status = 'COMPLETED'")
    Integer getMinScore();
}
