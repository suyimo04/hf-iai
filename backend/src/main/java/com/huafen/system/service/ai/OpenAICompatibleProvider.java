package com.huafen.system.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huafen.system.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI兼容API提供者
 * 支持OpenAI、DeepSeek、通义千问等兼容接口
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAICompatibleProvider implements AIProvider {

    private final ConfigService configService;
    private final ObjectMapper objectMapper;

    private static final String DEFAULT_BASE_URL = "https://api.openai.com/v1";
    private static final String DEFAULT_MODEL = "gpt-3.5-turbo";

    @Override
    public String getName() {
        return "openai";
    }

    @Override
    public Flux<String> streamChat(String systemPrompt, List<ChatMessage> history) {
        String apiKey = configService.getValue("ai.openai.api_key", "");
        String baseUrl = configService.getValue("ai.openai.base_url", DEFAULT_BASE_URL);
        String model = configService.getValue("ai.openai.model", DEFAULT_MODEL);

        if (apiKey.isEmpty()) {
            return Flux.error(new RuntimeException("OpenAI API Key未配置"));
        }

        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        List<Map<String, String>> messages = buildMessages(systemPrompt, history);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("stream", true);

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(data -> !data.equals("[DONE]"))
                .mapNotNull(this::extractContent)
                .onErrorResume(e -> {
                    log.error("OpenAI streaming error: {}", e.getMessage());
                    return Flux.error(new RuntimeException("AI服务调用失败: " + e.getMessage()));
                });
    }

    @Override
    public AIInterviewScore evaluateInterview(String systemPrompt, List<ChatMessage> messages) {
        String apiKey = configService.getValue("ai.openai.api_key", "");
        String baseUrl = configService.getValue("ai.openai.base_url", DEFAULT_BASE_URL);
        String model = configService.getValue("ai.openai.model", DEFAULT_MODEL);

        if (apiKey.isEmpty()) {
            throw new RuntimeException("OpenAI API Key未配置");
        }

        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        List<Map<String, String>> requestMessages = buildMessages(systemPrompt, messages);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", requestMessages);
        requestBody.put("stream", false);

        try {
            String response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseInterviewScore(response);
        } catch (Exception e) {
            log.error("OpenAI evaluate error: {}", e.getMessage());
            throw new RuntimeException("AI评估失败: " + e.getMessage());
        }
    }

    @Override
    public boolean testConnection() {
        String apiKey = configService.getValue("ai.openai.api_key", "");
        String baseUrl = configService.getValue("ai.openai.base_url", DEFAULT_BASE_URL);

        if (apiKey.isEmpty()) {
            return false;
        }

        try {
            WebClient webClient = WebClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader("Authorization", "Bearer " + apiKey)
                    .build();

            webClient.get()
                    .uri("/models")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return true;
        } catch (Exception e) {
            log.error("OpenAI connection test failed: {}", e.getMessage());
            return false;
        }
    }

    private List<Map<String, String>> buildMessages(String systemPrompt, List<ChatMessage> history) {
        List<Map<String, String>> messages = new ArrayList<>();

        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);
        }

        for (ChatMessage msg : history) {
            Map<String, String> message = new HashMap<>();
            message.put("role", msg.getRole());
            message.put("content", msg.getContent());
            messages.add(message);
        }

        return messages;
    }

    private String extractContent(String data) {
        try {
            JsonNode node = objectMapper.readTree(data);
            JsonNode choices = node.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode delta = choices.get(0).get("delta");
                if (delta != null && delta.has("content")) {
                    return delta.get("content").asText();
                }
            }
        } catch (Exception e) {
            log.debug("Failed to parse SSE data: {}", data);
        }
        return null;
    }

    private AIInterviewScore parseInterviewScore(String response) {
        try {
            JsonNode node = objectMapper.readTree(response);
            JsonNode choices = node.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).get("message").get("content").asText();

                // 尝试解析JSON格式的评分结果
                try {
                    return objectMapper.readValue(content, AIInterviewScore.class);
                } catch (Exception e) {
                    // 如果不是JSON格式，构建默认结果
                    return AIInterviewScore.builder()
                            .score(0)
                            .report(content)
                            .suggestion("请检查AI返回格式")
                            .build();
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse interview score: {}", e.getMessage());
        }

        return AIInterviewScore.builder()
                .score(0)
                .report("评分解析失败")
                .build();
    }
}
