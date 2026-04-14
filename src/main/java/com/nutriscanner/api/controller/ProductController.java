package com.nutriscanner.api.controller;

import com.nutriscanner.api.dto.request.ScanLabelRequest;
import com.nutriscanner.api.dto.response.ScoreDTO;
import com.nutriscanner.api.service.GeminiVisionService;
import com.nutriscanner.api.service.HistoryService;
import com.nutriscanner.api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import org.springframework.http.HttpMethod;
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;
    private final HistoryService historyService;
    private final GeminiVisionService geminiVisionService;

    @GetMapping("/product/{barcode}")
    public ResponseEntity<ScoreDTO> getProduct(
            @PathVariable String barcode,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        ScoreDTO result = productService.getProductByBarcode(barcode);

        if (userId != null) {
            productService.saveHistoryAsync(UUID.fromString(userId), barcode, historyService);
        }

        return ResponseEntity.ok(result);
    }
    @PostMapping("/product/scan-label")
    public ResponseEntity<ScoreDTO> scanLabel(
            @RequestBody ScanLabelRequest request) {
        return ResponseEntity.ok(
                productService.scanLabel(
                        request.getBarcode(),
                        request.getImage(),
                        geminiVisionService
                )
        );
    }

}