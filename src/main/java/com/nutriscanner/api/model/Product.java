package com.nutriscanner.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nova_group")
    private Integer novaGroup;

    @Column(nullable = false, unique = true)
    private String barcode;

    private String name;
    private String brand;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "ingredients_text", columnDefinition = "TEXT")
    private String ingredientsText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "final_score", nullable = false)
    @Builder.Default
    private Integer finalScore = 100;

    @Column(name = "score_color", nullable = false)
    @Builder.Default
    private String scoreColor = "GREEN";

    @Column(name = "score_label", nullable = false)
    @Builder.Default
    private String scoreLabel = "Non évalué";

    @Column(name = "data_completeness", nullable = false)
    @Builder.Default
    private String dataCompleteness = "FULL";

    @Column(nullable = false)
    @Builder.Default
    private String source = "OFF";

    @Column(name = "is_tunisian", nullable = false)
    @Builder.Default
    private Boolean isTunisian = false;

    @Column(name = "scan_count", nullable = false)
    @Builder.Default
    private Integer scanCount = 0;

    @Column(name = "last_fetched_at", nullable = false)
    @Builder.Default
    private LocalDateTime lastFetchedAt = LocalDateTime.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}