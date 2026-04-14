package com.nutriscanner.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class GeminiVisionService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String GROQ_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    public String extractIngredients(String base64Image) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);

            Map<String, Object> body = Map.of(
                    "model", "meta-llama/llama-4-scout-17b-16e-instruct",
                    "messages", List.of(
                            Map.of(
                                    "role", "user",
                                    "content", List.of(
                                            Map.of(
                                                    "type", "image_url",
                                                    "image_url", Map.of(
                                                            "url", "data:image/jpeg;base64," + base64Image
                                                    )
                                            ),
                                            Map.of(
                                                    "type", "text",
                                                    "text", "Read this food product label carefully. " +
                                                            "Extract ONLY the ingredients list. " +
                                                            "Return each ingredient on a new line. " +
                                                            "Include E-numbers exactly as written (e.g. E322, E471). " +
                                                            "Include ingredient names exactly as written. " +
                                                            "Return NOTHING else — no explanation, no preamble, just the ingredients list."
                                            )
                                    )
                            )
                    ),
                    "max_tokens", 500
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            String response = restTemplate.postForObject(GROQ_URL, request, String.class);

            System.out.println("Groq Vision response: " + response);

            JsonNode root = objectMapper.readTree(response);
            return root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

        } catch (Exception e) {
            System.out.println("Vision error: " + e.getMessage());
            return null;
        }
    }
}