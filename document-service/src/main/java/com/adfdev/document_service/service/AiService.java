package com.adfdev.document_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AiService {

    private final WebClient webClient;

    @Value("${groq.api-key:}")
    private String apiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String model;

    public AiService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.groq.com")
                .build();
    }

    public String generateSummary(String text) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Groq API key no configurada, generando resumen básico");
            return generateBasicSummary(text);
        }

        try {
            String truncatedText = text.length() > 4000 ? text.substring(0, 4000) : text;

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "user", "content",
                                    "Resume el siguiente texto académico en español, " +
                                            "destacando los puntos clave en máximo 3 párrafos:\n\n" + truncatedText)
                    ),
                    "max_tokens", 500,
                    "temperature", 0.3
            );

            Map response = webClient.post()
                    .uri("/openai/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null) {
                        String summary = (String) message.get("content");
                        log.info("Resumen generado con Groq AI");
                        return summary;
                    }
                }
            }

            return generateBasicSummary(text);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("429")) {
                log.warn("Rate limit de Groq, esperando 10s...");
                try {
                    Thread.sleep(10000);
                    return generateSummary(text);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
            log.error("Error al llamar a Groq API: {}", e.getMessage());
            return generateBasicSummary(text);
        }
    }

    private String generateBasicSummary(String text) {
        int maxLength = Math.min(text.length(), 500);
        return text.substring(0, maxLength) + "...";
    }
}