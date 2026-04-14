package com.nutriscanner.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HistoryDTO {
    private String barcode;
    private String productName;
    private String brand;
    private String imageUrl;
    private Integer scoreAtScan;
    private String scoreColor;
    private String scannedAt;
}