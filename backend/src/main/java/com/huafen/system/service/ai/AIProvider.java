package com.huafen.system.service.ai;

import com.huafen.system.entity.InterviewQuestion;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI服务提供者接口
 */
public interface AIProvider {

    /**
     * 获取提供者名称
     */
    String getName();

    /**
     * 流式聊天
     *
     * @param systemPrompt 系统提示词
     * @param history      聊天历史
     * @return 流式响应
     */
    Flux<String> streamChat(String systemPrompt, List<ChatMessage> history);

    /**
     * AI评估面试
     *
     * @param systemPrompt 系统提示词
     * @param messages     面试对话消息
     * @return 面试评分结果
     */
    AIInterviewScore evaluateInterview(String systemPrompt, List<ChatMessage> messages);

    /**
     * 测试连接
     *
     * @return 连接是否成功
     */
    boolean testConnection();

    /**
     * 评估面试答案（兼容旧接口）
     *
     * @param answers   答案列表
     * @param questions 题目列表
     * @return 评分结果
     */
    default InterviewResult evaluate(List<InterviewAnswer> answers, List<InterviewQuestion> questions) {
        throw new UnsupportedOperationException("This provider does not support legacy evaluate method");
    }
}
