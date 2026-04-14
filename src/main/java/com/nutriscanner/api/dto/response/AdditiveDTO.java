package com.nutriscanner.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdditiveDTO {
    private String code;
    private String nameFr;
    private String riskLevel;
    private Integer deduction;
    private String description;
}