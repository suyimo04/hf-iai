package com.huafen.system.repository;

import com.huafen.system.entity.QuestionnaireField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 问卷字段Repository
 */
@Repository
public interface QuestionnaireFieldRepository extends JpaRepository<QuestionnaireField, Long> {

    /**
     * 根据问卷ID查询字段列表，按排序顺序排列
     */
    List<QuestionnaireField> findByQuestionnaireIdOrderBySortOrderAsc(Long questionnaireId);

    /**
     * 根据问卷ID删除所有字段
     */
    void deleteByQuestionnaireId(Long questionnaireId);

    /**
     * 根据问卷ID统计字段数量
     */
    long countByQuestionnaireId(Long questionnaireId);
}
