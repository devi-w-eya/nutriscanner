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
                                                    "text", "You are analyzing a food product label. The label may be in Arabic, French, English or a mix.\n" +
                                                            "Extract ONLY the ingredients/additives list from this label.\n" +
                                                            "For each ingredient found:\n" +
                                                            "- If it is an E-number (like E471, E322, etc.) write it exactly as shown\n" +
                                                            "- If it is an Arabic additive name, translate it to its E-number if you know it\n" +
                                                            "- If it is a French or English additive name, keep it as is\n" +
                                                            "- List all ingredients separated by commas\n" +
                                                            "Common Arabic additive translations:\n" +
                                                            "مونوغليسيريد = E471, ليسيثين = E322, حمض الستريك = E330, بنزوات الصوديوم = E211\n" +
                                                            "نترات الصوديوم = E251, نتريت الصوديوم = E250, كبريتات الصوديوم = E221\n" +
                                                            "كاراميل = E150, تارتارين = E102, صفراء الغروب = E110\n" +
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