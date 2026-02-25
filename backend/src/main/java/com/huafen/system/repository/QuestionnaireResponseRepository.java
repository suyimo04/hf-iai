package com.huafen.system.repository;

import com.huafen.system.entity.QuestionnaireResponse;
import com.huafen.system.entity.enums.ResponseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 问卷响应Repository
 */
@Repository
public interface QuestionnaireResponseRepository extends JpaRepository<QuestionnaireResponse, Long>, JpaSpecificationExecutor<QuestionnaireResponse> {

    /**
     * 根据问卷ID查询响应列表
     */
    List<QuestionnaireResponse> findByQuestionnaireId(Long questionnaireId);

    /**
     * 根据问卷ID分页查询响应列表
     */
    Page<QuestionnaireResponse> findByQuestionnaireId(Long questionnaireId, Pageable pageable);

    /**
     * 根据用户ID查询响应列表
     */
    List<QuestionnaireResponse> findByUserId(Long userId);

    /**
     * 根据问卷ID和用户ID查询响应
     */
    List<QuestionnaireResponse> findByQuestionnaireIdAndUserId(Long questionnaireId, Long userId);

    /**
     * 根据问卷ID统计响应数量
     */
    long countByQuestionnaireId(Long questionnaireId);

    /**
     * 根据问卷ID和状态统计响应数量
     */
    long countByQuestionnaireIdAndStatus(Long questionnaireId, ResponseStatus status);

    /**
     * 根据自动创建的用户ID查询响应
     */
    List<QuestionnaireResponse> findByAutoCreatedUserId(Long autoCreatedUserId);
}
