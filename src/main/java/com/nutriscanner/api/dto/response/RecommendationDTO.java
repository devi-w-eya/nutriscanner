package com.nutriscanner.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecommendationDTO {
    private String barcode;
    private String productName;
    private String brand;
    private String imageUrl;
    private Integer finalScore;
    private String scoreColor;
}