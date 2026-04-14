package com.nutriscanner.api.service;

import com.nutriscanner.api.dto.response.HistoryDTO;

import com.nutriscanner.api.model.Product;
import com.nutriscanner.api.model.ScanHistory;
import com.nutriscanner.api.model.User;
import com.nutriscanner.api.repository.ScanHistoryRepository;
import com.nutriscanner.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final ScanHistoryRepository scanHistoryRepository;
    private final UserRepository userRepository;

    public void saveScan(UUID userId, Product product) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        ScanHistory history = ScanHistory.builder()
                .user(user)
                .product(product)
                .scoreAtScan(product.getFinalScore())
                .build();

        scanHistoryRepository.save(history);
    }

    public List<HistoryDTO> getHistory(UUID userId) {
        List<ScanHistory> history = scanHistoryRepository
                .findByUserIdOrderByScannedAtDesc(userId);

        return history.stream()
                .map(h -> HistoryDTO.builder()
                        .barcode(h.getProduct().getBarcode())
                        .productName(h.getProduct().getName())
                        .brand(h.getProduct().getBrand())
                        .imageUrl(h.getProduct().getImageUrl())
                        .scoreAtScan(h.getScoreAtScan())
                        .scoreColor(h.getProduct().getScoreColor())
                        .scannedAt(h.getScannedAt().toString())
                        .build())
                .toList();
    }
}