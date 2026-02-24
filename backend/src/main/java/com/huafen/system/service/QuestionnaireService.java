package com.huafen.system.service;

import com.huafen.system.entity.Questionnaire;
import com.huafen.system.entity.QuestionnaireField;
import com.huafen.system.entity.QuestionnaireResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 问卷服务接口
 */
public interface QuestionnaireService {

    /**
     * 创建问卷
     */
    Questionnaire createQuestionnaire(String title, String description, List<QuestionnaireField> fields);

    /**
     * 更新问卷
     */
    Questionnaire updateQuestionnaire(Long id, String title, String description, List<QuestionnaireField> fields);

    /**
     * 删除问卷
     */
    void deleteQuestionnaire(Long id);

    /**
     * 根据ID获取问卷
     */
    Questionnaire getById(Long id);

    /**
     * 分页查询问卷列表
     */
    Page<Questionnaire> getQuestionnaires(Pageable pageable);

    /**
     * 发布问卷
     */
    Questionnaire publishQuestionnaire(Long id);

    /**
     * 归档问卷
     */
    Questionnaire archiveQuestionnaire(Long id);

    /**
     * 根据公开令牌获取问卷（公开链接访问）
     */
    Questionnaire getByPublicToken(String publicToken);

    /**
     * 提交问卷响应
     */
    QuestionnaireResponse submitResponse(Long questionnaireId, String answers, String respondentInfo, String ipAddress, String userAgent);

    /**
     * 通过公开令牌提交问卷响应
     */
    QuestionnaireResponse submitResponseByToken(String publicToken, String answers, String respondentInfo, String ipAddress, String userAgent);

    /**
     * 获取问卷响应列表
     */
    Page<QuestionnaireResponse> getResponses(Long questionnaireId, Pageable pageable);

    /**
     * 获取问卷响应详情
     */
    QuestionnaireResponse getResponseById(Long responseId);

    /**
     * 统计问卷响应数量
     */
    long countResponses(Long questionnaireId);
}
