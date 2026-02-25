package com.huafen.system.service.ai;

import com.huafen.system.entity.InterviewQuestion;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 本地JSON评分实现
 */
@Component
public class LocalJSONProvider implements AIProvider {

    @Override
    public String getName() {
        return "local";
    }

    @Override
    public Flux<String> streamChat(String systemPrompt, List<ChatMessage> history) {
        // 本地模式不支持流式聊天，返回固定响应
        return Flux.just("本地模式不支持AI聊天功能，请配置OpenAI或Claude服务。");
    }

    @Override
    public AIInterviewScore evaluateInterview(String systemPrompt, List<ChatMessage> messages) {
        // 本地模式返回默认评分
        return AIInterviewScore.builder()
                .score(0)
                .report("本地模式不支持AI评估，请配置OpenAI或Claude服务。")
                .suggestion("请在系统配置中设置AI服务提供商")
                .build();
    }

    @Override
    public boolean testConnection() {
        // 本地模式始终可用
        return true;
    }

    @Override
    public InterviewResult evaluate(List<InterviewAnswer> answers, List<InterviewQuestion> questions) {
        Map<Long, InterviewQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(InterviewQuestion::getId, q -> q));

        List<InterviewResult.QuestionDetail> details = new ArrayList<>();
        int totalScore = 0;
        int maxTotalScore = 0;

        for (InterviewAnswer answer : answers) {
            InterviewQuestion question = questionMap.get(answer.getQuestionId());
            if (question == null) {
                continue;
            }

            int maxScore = question.getScore() != null ? question.getScore() : 10;
            maxTotalScore += maxScore;

            InterviewResult.QuestionDetail detail = evaluateAnswer(answer, question, maxScore);
            details.add(detail);
            totalScore += detail.getScore();
        }

        // 计算百分制分数
        int finalScore = maxTotalScore > 0 ? (int) Math.round((double) totalScore / maxTotalScore * 100) : 0;

        // 生成报告
        String report = generateReport(finalScore, details, questions.size());

        return InterviewResult.builder()
                .score(finalScore)
                .report(report)
                .details(details)
                .build();
    }

    private InterviewResult.QuestionDetail evaluateAnswer(InterviewAnswer answer, InterviewQuestion question, int maxScore) {
        String userAnswer = answer.getAnswer();
        String correctAnswer = question.getAnswer();
        int score = 0;
        String feedback;

        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            feedback = "未作答";
        } else if (question.getOptions() != null && !question.getOptions().isEmpty()) {
            // 选择题：答案完全匹配得满分
            if (correctAnswer != null && correctAnswer.trim().equalsIgnoreCase(userAnswer.trim())) {
                score = maxScore;
                feedback = "回答正确";
            } else {
                feedback = "回答错误，正确答案：" + correctAnswer;
            }
        } else {
            // 问答题：关键词匹配评分
            score = evaluateTextAnswer(userAnswer, correctAnswer, maxScore);
            if (score == maxScore) {
                feedback = "回答完整，包含所有关键点";
            } else if (score > maxScore / 2) {
                feedback = "回答较好，但可以更完整";
            } else if (score > 0) {
                feedback = "回答部分正确，缺少一些关键点";
            } else {
                feedback = "回答未涉及关键内容";
            }
        }

        return InterviewResult.QuestionDetail.builder()
                .questionId(question.getId())
                .score(score)
                .maxScore(maxScore)
                .feedback(feedback)
                .build();
    }

    /**
     * 问答题关键词匹配评分
     */
    private int evaluateTextAnswer(String userAnswer, String correctAnswer, int maxScore) {
        if (correctAnswer == null || correctAnswer.trim().isEmpty()) {
            return maxScore;
        }

        String[] keywords = correctAnswer.split("[,;，；\\n]+");
        if (keywords.length == 0) {
            return maxScore;
        }

        String lowerUserAnswer = userAnswer.toLowerCase();
        int matchedCount = 0;

        for (String keyword : keywords) {
            String trimmedKeyword = keyword.trim().toLowerCase();
            if (!trimmedKeyword.isEmpty() && lowerUserAnswer.contains(trimmedKeyword)) {
                matchedCount++;
            }
        }

        return (int) Math.round((double) matchedCount / keywords.length * maxScore);
    }

    private String generateReport(int score, List<InterviewResult.QuestionDetail> details, int totalQuestions) {
        StringBuilder report = new StringBuilder();
        report.append("面试评估报告\n");
        report.append("================\n\n");
        report.append("总分：").append(score).append("/100\n");
        report.append("答题数：").append(details.size()).append("/").append(totalQuestions).append("\n\n");

        if (score >= 80) {
            report.append("评价：优秀！您的表现非常出色，展现了扎实的专业知识。\n");
        } else if (score >= 60) {
            report.append("评价：良好。您具备基本的专业素养，建议继续加强学习。\n");
        } else if (score >= 40) {
            report.append("评价：一般。部分知识点掌握不够牢固，需要进一步学习。\n");
        } else {
            report.append("评价：需要提升。建议系统学习相关知识后再次尝试。\n");
        }

        report.append("\n各题详情：\n");
        for (int i = 0; i < details.size(); i++) {
            InterviewResult.QuestionDetail detail = details.get(i);
            report.append(String.format("%d. 得分：%d/%d - %s\n",
                    i + 1, detail.getScore(), detail.getMaxScore(), detail.getFeedback()));
        }

        return report.toString();
    }
}
