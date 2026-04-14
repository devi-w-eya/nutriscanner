package com.nutriscanner.api.service;

import com.nutriscanner.api.dto.response.AdditiveDTO;
import com.nutriscanner.api.dto.response.RecommendationDTO;
import com.nutriscanner.api.dto.response.ScoreDTO;
import com.nutriscanner.api.model.Additive;
import com.nutriscanner.api.model.Product;
import com.nutriscanner.api.model.ProductAdditive;
import com.nutriscanner.api.repository.AdditiveRepository;
import com.nutriscanner.api.repository.CategoryRepository;
import com.nutriscanner.api.repository.ProductAdditiveRepository;
import com.nutriscanner.api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final AdditiveRepository additiveRepository;
    private final ProductAdditiveRepository productAdditiveRepository;
    private final CategoryRepository categoryRepository;
    private final OpenFoodFactsService offService;
    private final ScoringService scoringService;

    public ScoreDTO getProductByBarcode(String barcode) {

        // Step 1 — validate barcode format
        if (!barcode.matches("\\d{8,14}")) {
            throw new RuntimeException("INVALID_BARCODE");
        }

        // Step 2 — cache hit
        Optional<Product> cached = productRepository.findByBarcode(barcode);
        if (cached.isPresent()) {
            Product cachedProduct = cached.get();
            cachedProduct.setScanCount(cachedProduct.getScanCount() + 1);
            productRepository.save(cachedProduct);
            return buildScoreDTO(cachedProduct);
        }

        // Step 3 — call OFF
        OpenFoodFactsService.OFFProduct offProduct = offService.fetchByBarcode(barcode);
        if (offProduct == null) {
            throw new RuntimeException("PRODUCT_NOT_FOUND");
        }

        // Step 3b — check if enough data to score
        boolean hasNovaGroup = offProduct.novaGroup != null;
        boolean hasAdditives = !offProduct.additivesCodes.isEmpty();

        if (!hasNovaGroup && !hasAdditives) {
            Product product = Product.builder()
                    .barcode(barcode)
                    .name(offProduct.name)
                    .brand(offProduct.brand)
                    .imageUrl(offProduct.imageUrl)
                    .ingredientsText(offProduct.ingredientsText)
                    .novaGroup(null)
                    .source("OFF")
                    .finalScore(0)
                    .scoreColor("UNKNOWN")
                    .scoreLabel("Données insuffisantes")
                    .dataCompleteness("INSUFFICIENT")
                    .build();
            Product saved = productRepository.save(product);
            saved.setScanCount(1);
            productRepository.save(saved);
            return buildScoreDTO(saved);
        }

        // Step 4 — build product
        Product product = Product.builder()
                .barcode(barcode)
                .name(offProduct.name)
                .brand(offProduct.brand)
                .imageUrl(offProduct.imageUrl)
                .ingredientsText(offProduct.ingredientsText)
                .novaGroup(offProduct.novaGroup)
                .source("OFF")
                .build();

        // Step 5 — map category
        if (offProduct.categoryTag != null) {
            String tag = offProduct.categoryTag.toLowerCase();
            String categoryName = null;

            if (tag.contains("beverage") || tag.contains("drink") ||
                    tag.contains("soda") || tag.contains("water") ||
                    tag.contains("juice") || tag.contains("boisson")) {
                categoryName = "Beverages";
            } else if (tag.contains("dairy") || tag.contains("milk") ||
                    tag.contains("cheese") || tag.contains("yogurt") ||
                    tag.contains("laitier") || tag.contains("fromage")) {
                categoryName = "Dairy";
            } else if (tag.contains("biscuit") || tag.contains("cookie") ||
                    tag.contains("snack") || tag.contains("chip") ||
                    tag.contains("crisp")) {
                categoryName = "Snacks";
            } else if (tag.contains("chocolate") || tag.contains("candy") ||
                    tag.contains("sweet") || tag.contains("confection") ||
                    tag.contains("bonbon")) {
                categoryName = "Sweets";
            } else if (tag.contains("bread") || tag.contains("pastry") ||
                    tag.contains("cake") || tag.contains("pain") ||
                    tag.contains("patisserie")) {
                categoryName = "Bread and Pastry";
            } else if (tag.contains("cereal") || tag.contains("grain") ||
                    tag.contains("cereale")) {
                categoryName = "Cereals";
            } else if (tag.contains("meat") || tag.contains("fish") ||
                    tag.contains("seafood") || tag.contains("viande") ||
                    tag.contains("poisson")) {
                categoryName = "Meat and Fish";
            } else if (tag.contains("oil") || tag.contains("fat") ||
                    tag.contains("butter") || tag.contains("huile") ||
                    tag.contains("margarine")) {
                categoryName = "Oils and Fats";
            } else if (tag.contains("spread") || tag.contains("jam") ||
                    tag.contains("tartiner") || tag.contains("confiture")) {
                categoryName = "Spreads";
            } else if (tag.contains("sauce") || tag.contains("condiment") ||
                    tag.contains("dressing") || tag.contains("vinegar")) {
                categoryName = "Condiments";
            } else if (tag.contains("meal") || tag.contains("prepared") ||
                    tag.contains("frozen") || tag.contains("surgele")) {
                categoryName = "Ready Meals";
            }

            if (categoryName != null) {
                String finalCategoryName = categoryName;
                categoryRepository.findByNameEn(finalCategoryName)
                        .ifPresent(product::setCategory);
            }
        }

        // Step 6 — look up additives from additives_tags
        List<Additive> foundAdditives = new ArrayList<>();
        Set<String> addedCodes = new java.util.HashSet<>();

        for (String code : offProduct.additivesCodes) {
            additiveRepository.findByCode(code).ifPresent(a -> {
                if (addedCodes.add(a.getCode())) {
                    foundAdditives.add(a);
                }
            });
        }

// Step 6b — also parse ingredients_text for additives listed by name
        List<Additive> fromText = parseIngredientsText(offProduct.ingredientsText);
        for (Additive a : fromText) {
            if (addedCodes.add(a.getCode())) {
                foundAdditives.add(a);
            }
        }

// Step 6c — unknown E-numbers get MEDIUM penalty
        for (String code : offProduct.additivesCodes) {
            if (!addedCodes.contains(code.toUpperCase())) {
                Additive unknown = Additive.builder()
                        .code(code)
                        .nameFr("Additif non répertorié (" + code + ")")
                        .riskLevel("MEDIUM")
                        .description("Additif non présent dans notre base de données. Classé modérément préoccupant par précaution.")
                        .aiIdentified(false)
                        .verified(false)
                        .build();
                foundAdditives.add(unknown);
                addedCodes.add(code.toUpperCase());
            }
        }

        // Step 7 — calculate score
        int score = scoringService.calculateScore(foundAdditives, offProduct.novaGroup);
        product.setFinalScore(score);
        product.setScoreColor(scoringService.getColor(score));
        product.setScoreLabel(scoringService.getLabel(score));

        // Step 8 — set data completeness
        product.setDataCompleteness(offProduct.additivesCodes.isEmpty() ? "PARTIAL" : "FULL");

        // Step 9 — save product
        Product saved = productRepository.save(product);

        // Step 10 — save product_additives
        for (Additive additive : foundAdditives) {
            ProductAdditive link = ProductAdditive.builder()
                    .product(saved)
                    .additive(additive)
                    .deductionApplied(scoringService.getDeduction(additive.getRiskLevel()))
                    .build();
            productAdditiveRepository.save(link);
        }

        // Step 11 — increment scan count
        saved.setScanCount(1);
        productRepository.save(saved);

        return buildScoreDTO(saved);
    }

    public List<RecommendationDTO> getRecommendations(String barcode) {
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new RuntimeException("PRODUCT_NOT_FOUND"));

        if (product.getCategory() == null) {
            return List.of();
        }

        List<Product> recommendations = productRepository.findRecommendations(
                product.getCategory(),
                product.getFinalScore(),
                product.getId(),
                PageRequest.of(0, 3)
        );

        return recommendations.stream()
                .map(r -> RecommendationDTO.builder()
                        .barcode(r.getBarcode())
                        .productName(r.getName())
                        .brand(r.getBrand())
                        .imageUrl(r.getImageUrl())
                        .finalScore(r.getFinalScore())
                        .scoreColor(r.getScoreColor())
                        .build())
                .toList();
    }

    private ScoreDTO buildScoreDTO(Product product) {
        List<ProductAdditive> links = productAdditiveRepository
                .findByProductId(product.getId());

        List<AdditiveDTO> additiveDTOs = links.stream()
                .map(link -> AdditiveDTO.builder()
                        .code(link.getAdditive().getCode())
                        .nameFr(link.getAdditive().getNameFr())
                        .riskLevel(link.getAdditive().getRiskLevel())
                        .deduction(link.getDeductionApplied())
                        .description(link.getAdditive().getDescription())
                        .build())
                .toList();

        int totalDeductions = additiveDTOs.stream()
                .mapToInt(AdditiveDTO::getDeduction)
                .sum();

        int novaBase = product.getNovaGroup() != null
                ? scoringService.getNovaBase(product.getNovaGroup())
                : 0;

        return ScoreDTO.builder()
                .barcode(product.getBarcode())
                .productName(product.getName())
                .brand(product.getBrand())
                .imageUrl(product.getImageUrl())
                .finalScore(product.getFinalScore())
                .scoreColor(product.getScoreColor())
                .scoreLabel(product.getScoreLabel())
                .dataCompleteness(product.getDataCompleteness())
                .novaGroup(product.getNovaGroup())
                .additives(additiveDTOs)
                .scoreBreakdown(ScoreDTO.ScoreBreakdown.builder()
                        .baseScore(novaBase)
                        .totalDeductions(totalDeductions)
                        .ultraProcessedPenalty(0)
                        .build())
                .build();
    }

    public void saveHistoryAsync(UUID userId, String barcode, HistoryService historyService) {
        productRepository.findByBarcode(barcode).ifPresent(product ->
                historyService.saveScan(userId, product));
    }
    private List<Additive> parseIngredientsText(String ingredientsText) {
        List<Additive> found = new ArrayList<>();
        if (ingredientsText == null || ingredientsText.isBlank()) return found;

        Set<String> addedCodes = new java.util.HashSet<>();

        // Step 1 — find E-numbers directly using regex
        java.util.regex.Pattern pattern = java.util.regex.Pattern
                .compile("[Ee]\\d{3,4}[a-zA-Z]?");
        java.util.regex.Matcher matcher = pattern.matcher(ingredientsText);
        while (matcher.find()) {
            String code = matcher.group().toUpperCase();
            if (!addedCodes.contains(code)) {
                additiveRepository.findByCode(code).ifPresent(a -> {
                    found.add(a);
                    addedCodes.add(a.getCode());
                });
            }
        }

        // Step 2 — find additives by common name
        List<Additive> allAdditives = additiveRepository.findAll();
        String lowerText = ingredientsText.toLowerCase();
        for (Additive additive : allAdditives) {
            if (addedCodes.contains(additive.getCode())) continue;
            if (additive.getCommonNames() == null) continue;
            String[] names = additive.getCommonNames().split(",");
            for (String name : names) {
                String trimmed = name.trim().toLowerCase();
                if (trimmed.length() > 3 && lowerText.contains(trimmed)) {
                    found.add(additive);
                    addedCodes.add(additive.getCode());
                    break;
                }
            }
        }

        return found;
    }
    public ScoreDTO scanLabel(String barcode, String base64Image,
                              GeminiVisionService geminiVisionService) {

        // Step 1 — validate barcode
        if (!barcode.matches("\\d{8,14}")) {
            throw new RuntimeException("INVALID_BARCODE");
        }

        // Step 2 — check cache first
        Optional<Product> cached = productRepository.findByBarcode(barcode);
        if (cached.isPresent() &&
                !"INSUFFICIENT".equals(cached.get().getDataCompleteness()) &&
                !"PARTIAL".equals(cached.get().getDataCompleteness())) {
            return buildScoreDTO(cached.get());
        }

        // Step 3 — call Gemini Vision
        String extractedText = geminiVisionService.extractIngredients(base64Image);
        if (extractedText == null || extractedText.isBlank()) {
            throw new RuntimeException("AI_UNAVAILABLE");
        }

        System.out.println("Gemini extracted: " + extractedText);

        // Step 4 — parse extracted ingredients
        List<Additive> foundAdditives = parseIngredientsText(extractedText);

        // Step 5 — unknown E-numbers from extracted text
        java.util.regex.Pattern pattern = java.util.regex.Pattern
                .compile("[Ee]\\d{3,4}[a-zA-Z]?");
        java.util.regex.Matcher matcher = pattern.matcher(extractedText);
        Set<String> knownCodes = new HashSet<>();
        foundAdditives.forEach(a -> knownCodes.add(a.getCode()));

        while (matcher.find()) {
            String code = matcher.group().toUpperCase();
            if (!knownCodes.contains(code)) {
                Additive unknown = Additive.builder()
                        .code(code)
                        .nameFr("Additif non répertorié (" + code + ")")
                        .riskLevel("MEDIUM")
                        .description("Additif non présent dans notre base de données.")
                        .aiIdentified(true)
                        .verified(false)
                        .build();
                foundAdditives.add(unknown);
                knownCodes.add(code);
            }
        }

        // Step 6 — get NOVA from OFF if available
        Integer novaGroup = null;
        String productName = barcode;
        String brand = null;
        String imageUrl = null;

        OpenFoodFactsService.OFFProduct offProduct = offService.fetchByBarcode(barcode);
        if (offProduct != null) {
            novaGroup = offProduct.novaGroup;
            productName = offProduct.name != null ? offProduct.name : barcode;
            brand = offProduct.brand;
            imageUrl = offProduct.imageUrl;
        }

        // Step 7 — calculate score
        int score = scoringService.calculateScore(foundAdditives, novaGroup);

        // Step 8 — build and save product
        Product product = Product.builder()
                .barcode(barcode)
                .name(productName)
                .brand(brand)
                .imageUrl(imageUrl)
                .ingredientsText(extractedText)
                .novaGroup(novaGroup)
                .finalScore(score)
                .scoreColor(scoringService.getColor(score))
                .scoreLabel(scoringService.getLabel(score))
                .dataCompleteness("FULL")
                .source("GEMINI_VISION")
                .build();

        Product saved = productRepository.save(product);

        for (Additive additive : foundAdditives) {
            ProductAdditive link = ProductAdditive.builder()
                    .product(saved)
                    .additive(additive)
                    .deductionApplied(scoringService.getDeduction(additive.getRiskLevel()))
                    .build();
            productAdditiveRepository.save(link);
        }

        saved.setScanCount(1);
        productRepository.save(saved);

        return buildScoreDTO(saved);
    }
}