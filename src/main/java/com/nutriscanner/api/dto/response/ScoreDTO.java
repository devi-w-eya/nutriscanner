package com.nutriscanner.api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ScoreDTO {
    private String barcode;
    private String productName;
    private String brand;
    private String imageUrl;
    private Integer finalScore;
    private String scoreColor;
    private String scoreLabel;
    private String dataCompleteness;
    private List<AdditiveDTO> additives;
    private ScoreBreakdown scoreBreakdown;
    private Integer novaGroup;
    @Data
    @Builder
    public static class ScoreBreakdown {
        private Integer baseScore;
        private Integer totalDeductions;
        private Integer ultraProcessedPenalty;
    }
}