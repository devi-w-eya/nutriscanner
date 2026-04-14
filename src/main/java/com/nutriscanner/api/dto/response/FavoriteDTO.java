package com.nutriscanner.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FavoriteDTO {
    private String barcode;
    private String productName;
    private String brand;
    private String imageUrl;
    private Integer finalScore;
    private String scoreColor;
    private String savedAt;
}