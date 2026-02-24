package com.huafen.system.repository;

import com.huafen.system.entity.Questionnaire;
import com.huafen.system.entity.enums.QuestionnaireStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 问卷Repository
 */
@Repository
public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long>, JpaSpecificationExecutor<Questionnaire> {

    /**
     * 根据状态查询问卷列表
     */
    List<Questionnaire> findByStatus(QuestionnaireStatus status);

    /**
     * 根据公开令牌查询问卷
     */
    Optional<Questionnaire> findByPublicToken(String publicToken);

    /**
     * 根据创建者ID查询问卷列表
     */
    List<Questionnaire> findByCreatedById(Long userId);

    /**
     * 根据状态统计问卷数量
     */
    long countByStatus(QuestionnaireStatus status);

    /**
     * 检查公开令牌是否存在
     */
    boolean existsByPublicToken(String publicToken);
}
