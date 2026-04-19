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
                                                    "text", "You are reading a food product label. The label may be in Arabic, French, English or a mix.\n\n" +
                                                            "Your task: Extract the COMPLETE ingredients list exactly as written on the label.\n" +
                                                            "- Copy text exactly as you see it — do NOT translate or interpret\n" +
                                                            "- Include E-numbers exactly as written (E471, E322, etc.)\n" +
                                                            "- Include Arabic ingredient names exactly as written\n" +
                                                            "- Include French/English ingredient names exactly as written\n" +
                                                            "- Separate each ingredient with a comma\n" +
                                                            "- Do NOT add anything not visible on the label\n" +
                                                            "- Do NOT guess or infer ingredients\n\n" +
                                                            "If you cannot read the label clearly, return: NONE\n\n" +
                                                            "Return ONLY the ingredients list, nothing else."
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