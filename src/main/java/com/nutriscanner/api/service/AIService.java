package com.nutriscanner.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutriscanner.api.model.AssistantCache;
import com.nutriscanner.api.model.Product;
import com.nutriscanner.api.model.ProductAdditive;
import com.nutriscanner.api.repository.AssistantCacheRepository;
import com.nutriscanner.api.repository.ProductAdditiveRepository;
import com.nutriscanner.api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIService {

    private final ProductRepository productRepository;
    private final ProductAdditiveRepository productAdditiveRepository;
    private final AssistantCacheRepository assistantCacheRepository;
    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${groq.api.key}")
    private String groqApiKey;

    private static final String GROQ_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    public String ask(String barcode, String question) {

        System.out.println("AI ask called with barcode: " + barcode);

        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new RuntimeException("PRODUCT_NOT_FOUND"));

        System.out.println("Product found: " + product.getName());

        String questionHash = md5(question.toLowerCase().trim());
        var cached = assistantCacheRepository
                .findByProductIdAndQuestionHash(product.getId(), questionHash);
        if (cached.isPresent()) {
            return cached.get().getAnswerText();
        }

        List<ProductAdditive> links = productAdditiveRepository
                .findByProductId(product.getId());

        StringBuilder additiveList = new StringBuilder();
        for (ProductAdditive link : links) {
            additiveList.append("- ")
                    .append(link.getAdditive().getCode())
                    .append(" (")
                    .append(link.getAdditive().getNameFr())
                    .append(") : ")
                    .append(link.getAdditive().getRiskLevel())
                    .append(" — ")
                    .append(link.getAdditive().getDescription())
                    .append("\n");
        }

        String dataNote = "INSUFFICIENT".equals(product.getDataCompleteness())
                ? "\nNote: Les données additifs de ce produit sont incomplètes."
                : "";

        String prompt = "Tu es un assistant de sécurité alimentaire pour le marché tunisien.\n"
                + "Tu commentes uniquement les additifs alimentaires et leurs risques documentés.\n"
                + "Tu ne donnes pas de conseils nutritionnels. Tu ne fais pas de diagnostic médical.\n"
                + "Tu réponds en français en maximum 3 phrases courtes.\n\n"
                + "Produit: " + product.getName() + " par " + product.getBrand() + "\n"
                + "Score de sécurité: " + product.getFinalScore() + "/20 (" + product.getScoreLabel() + ")\n"
                + "Groupe NOVA: " + (product.getNovaGroup() != null ? product.getNovaGroup() : "inconnu") + "\n"
                + "Additifs détectés:\n"
                + (additiveList.length() == 0 ? "Aucun additif détecté\n" : additiveList.toString())
                + dataNote + "\n"
                + "Question: " + question;

        String answer = callGroq(prompt);

        AssistantCache cache = AssistantCache.builder()
                .product(product)
                .questionHash(questionHash)
                .questionText(question)
                .answerText(answer)
                .build();
        assistantCacheRepository.save(cache);

        return answer;
    }

    private String callGroq(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);

            Map<String, Object> body = Map.of(
                    "model", "llama-3.3-70b-versatile",
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    ),
                    "max_tokens", 300,
                    "temperature", 0.7
            );

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(body, headers);
            String response = restTemplate.postForObject(
                    GROQ_URL, request, String.class);

            JsonNode root = objectMapper.readTree(response);
            return root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

        } catch (Exception e) {
            System.out.println("Groq error: " + e.getMessage());
            throw new RuntimeException("AI_UNAVAILABLE");
        }
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return input;
        }
    }
}