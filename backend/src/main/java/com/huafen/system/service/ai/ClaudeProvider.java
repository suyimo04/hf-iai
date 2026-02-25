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
 * Claude (Anthropic) API提供者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClaudeProvider implements AIProvider {

    private final ConfigService configService;
    private final ObjectMapper objectMapper;

    private static final String DEFAULT_BASE_URL = "https://api.anthropic.com";
    private static final String DEFAULT_MODEL = "claude-3-sonnet-20240229";
    private static final String API_VERSION = "2023-06-01";

    @Override
    public String getName() {
        return "claude";
    }

    @Override
    public Flux<String> streamChat(String systemPrompt, List<ChatMessage> history) {
        String apiKey = configService.getValue("ai.claude.api_key", "");
        String baseUrl = configService.getValue("ai.claude.base_url", DEFAULT_BASE_URL);
        String model = configService.getValue("ai.claude.model", DEFAULT_MODEL);

        if (apiKey.isEmpty()) {
            return Flux.error(new RuntimeException("Claude API Key未配置"));
        }

        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", API_VERSION)
                .defaultHeader("Content-Type", "application/json")
                .build();

        Map<String, Object> requestBody = buildClaudeRequest(systemPrompt, history, model, true);

        return webClient.post()
                .uri("/v1/messages")
                .bodyValue(requestBody)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(data -> data.contains("content_block_delta"))
                .mapNotNull(this::extractClaudeContent)
                .onErrorResume(e -> {
                    log.error("Claude streaming error: {}", e.getMessage());
                    return Flux.error(new RuntimeException("AI服务调用失败: " + e.getMessage()));
                });
    }

    @Override
    public AIInterviewScore evaluateInterview(String systemPrompt, List<ChatMessage> messages) {
        String apiKey = configService.getValue("ai.claude.api_key", "");
        String baseUrl = configService.getValue("ai.claude.base_url", DEFAULT_BASE_URL);
        String model = configService.getValue("ai.claude.model", DEFAULT_MODEL);

        if (apiKey.isEmpty()) {
            throw new RuntimeException("Claude API Key未配置");
        }

        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", API_VERSION)
                .defaultHeader("Content-Type", "application/json")
                .build();

        Map<String, Object> requestBody = buildClaudeRequest(systemPrompt, messages, model, false);

        try {
            String response = webClient.post()
                    .uri("/v1/messages")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseClaudeInterviewScore(response);
        } catch (Exception e) {
            log.error("Claude evaluate error: {}", e.getMessage());
            throw new RuntimeException("AI评估失败: " + e.getMessage());
        }
    }

    @Override
    public boolean testConnection() {
        String apiKey = configService.getValue("ai.claude.api_key", "");
        String baseUrl = configService.getValue("ai.claude.base_url", DEFAULT_BASE_URL);
        String model = configService.getValue("ai.claude.model", DEFAULT_MODEL);

        if (apiKey.isEmpty()) {
            return false;
        }

        try {
            WebClient webClient = WebClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader("x-api-key", apiKey)
                    .defaultHeader("anthropic-version", API_VERSION)
                    .defaultHeader("Content-Type", "application/json")
                    .build();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 10);
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> msg = new HashMap<>();
            msg.put("role", "user");
            msg.put("content", "Hi");
            messages.add(msg);
            requestBody.put("messages", messages);

            webClient.post()
                    .uri("/v1/messages")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return true;
        } catch (Exception e) {
            log.error("Claude connection test failed: ", e.getMessage());
            return false;
        }
    }

    private Map<String, Object> buildClaudeRequest(String systemPrompt, List<ChatMessage> history,
                                                    String model, boolean stream) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", 4096);
        requestBody.put("stream", stream);

        // Claude使用单独的system字段
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            requestBody.put("system", systemPrompt);
        }

        // 构建消息列表（Claude不在messages中包含system消息）
        List<Map<String, String>> messages = new ArrayList<>();
        for (ChatMessage msg : history) {
            if (!"system".equals(msg.getRole())) {
                Map<String, String> message = new HashMap<>();
                message.put("role", msg.getRole());
                message.put("content", msg.getContent());
                messages.add(message);
            }
        }
        requestBody.put("messages", messages);

        return requestBody;
    }

    private String extractClaudeContent(String data) {
        try {
            // Claude SSE格式: event: content_block_delta\ndata: {"type":"content_block_delta",...}
            if (data.startsWith("data: ")) {
                data = data.substring(6);
            }
            JsonNode node = objectMapper.readTree(data);
            if ("content_block_delta".equals(node.get("type").asText())) {
                JsonNode delta = node.get("delta");
                if (delta != null && delta.has("text")) {
                    return delta.get("text").asText();
                }
            }
        } catch (Exception e) {
            log.debug("Failed to parse Claude SSE data: {}", data);
        }
        return null;
    }

    private AIInterviewScore parseClaudeInterviewScore(String response) {
        try {
            JsonNode node = objectMapper.readTree(response);
            JsonNode content = node.get("content");
            if (content != null && content.isArray() && content.size() > 0) {
                String text = content.get(0).get("text").asText();

                // 尝试解析JSON格式的评分结果
                try {
                    return objectMapper.readValue(text, AIInterviewScore.class);
                } catch (Exception e) {
                    // 如果不是JSON格式，构建默认结果
                    return AIInterviewScore.builder()
                            .score(0)
                            .report(text)
                            .suggestion("请检查AI返回格式")
                            .build();
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Claude interview score: {}", e.getMessage());
        }

        return AIInterviewScore.builder()
                .score(0)
                .report("评分解析失败")
                .build();
    }
}
