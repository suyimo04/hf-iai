package com.huafen.system.repository;

import com.huafen.system.entity.AIInterviewMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AI面试消息Repository
 */
@Repository
public interface AIInterviewMessageRepository extends JpaRepository<AIInterviewMessage, Long> {

    /**
     * 根据会话ID查询消息列表，按序号排序
     */
    List<AIInterviewMessage> findBySessionIdOrderBySequenceNumberAsc(Long sessionId);

    /**
     * 根据会话ID删除所有消息
     */
    void deleteBySessionId(Long sessionId);
}
