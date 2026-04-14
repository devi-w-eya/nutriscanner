package com.nutriscanner.api.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScanLabelRequest {
    private String barcode;
    private String image;
}