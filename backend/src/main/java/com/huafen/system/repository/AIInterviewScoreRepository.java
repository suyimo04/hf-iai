package com.huafen.system.repository;

import com.huafen.system.entity.AIInterviewScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * AI面试评分Repository
 */
@Repository
public interface AIInterviewScoreRepository extends JpaRepository<AIInterviewScore, Long> {

    /**
     * 根据会话ID查询评分
     */
    Optional<AIInterviewScore> findBySessionId(Long sessionId);

    /**
     * 根据会话ID删除评分
     */
    void deleteBySessionId(Long sessionId);
}
