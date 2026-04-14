package com.nutriscanner.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class OpenFoodFactsService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String OFF_URL =
            "https://world.openfoodfacts.org/api/v2/product/%s.json" +
                    "?fields=product_name,brands,image_front_url,additives_tags," +
                    "additives_original_tags,ingredients_text,categories_tags,nova_group";

    public OFFProduct fetchByBarcode(String barcode) {
        try {
            String url = String.format(OFF_URL, barcode);
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            int status = root.path("status").asInt();
            if (status == 0) return null;

            JsonNode product = root.path("product");

            OFFProduct result = new OFFProduct();
            result.name = product.path("product_name").asText(null);
            result.brand = product.path("brands").asText(null);
            result.imageUrl = product.path("image_front_url").asText(null);
            result.ingredientsText = product.path("ingredients_text").asText(null);
            JsonNode novaNode = product.path("nova_group");
            result.novaGroup = novaNode.isNull() || novaNode.isMissingNode() ? null : novaNode.asInt();

            // Try additives_tags first, fall back to additives_original_tags
            JsonNode additivesNode = product.path("additives_tags");
            if (!additivesNode.isArray() || additivesNode.isEmpty()) {
                additivesNode = product.path("additives_original_tags");
            }

            if (additivesNode.isArray()) {
                for (JsonNode node : additivesNode) {
                    String raw = node.asText();
                    if (raw.contains(":")) {
                        raw = raw.substring(raw.indexOf(":") + 1);
                    }
                    result.additivesCodes.add(raw.toUpperCase());
                }
            }

            System.out.println("Additives from OFF: " + result.additivesCodes);

            // Extract category
            JsonNode categoriesNode = product.path("categories_tags");
            if (categoriesNode.isArray() && categoriesNode.size() > 0) {
                result.categoryTag = categoriesNode.get(0).asText(null);
            }

            return result;

        } catch (Exception e) {
            System.out.println("OFF error: " + e.getMessage());
            return null;
        }
    }

    public static class OFFProduct {
        public String name;
        public String brand;
        public String imageUrl;
        public String ingredientsText;
        public String categoryTag;
        public Integer novaGroup;
        public java.util.List<String> additivesCodes = new java.util.ArrayList<>();
    }
}