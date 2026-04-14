package com.nutriscanner.api.service;

import com.nutriscanner.api.dto.response.FavoriteDTO;
import com.nutriscanner.api.model.Favorite;
import com.nutriscanner.api.model.Product;
import com.nutriscanner.api.model.User;
import com.nutriscanner.api.repository.FavoriteRepository;
import com.nutriscanner.api.repository.ProductRepository;
import com.nutriscanner.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public void addFavorite(UUID userId, String barcode) {
        if (favoriteRepository.existsByUserIdAndProductBarcode(userId, barcode)) {
            return; // already favorited — silent idempotency
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new RuntimeException("PRODUCT_NOT_FOUND"));
        Favorite favorite = Favorite.builder()
                .user(user)
                .product(product)
                .build();
        favoriteRepository.save(favorite);
    }

    public void removeFavorite(UUID userId, String barcode) {
        favoriteRepository.findByUserIdAndProductBarcode(userId, barcode)
                .ifPresent(favoriteRepository::delete);
    }

    public List<FavoriteDTO> getFavorites(UUID userId) {
        return favoriteRepository.findByUserIdOrderBySavedAtDesc(userId)
                .stream()
                .map(f -> FavoriteDTO.builder()
                        .barcode(f.getProduct().getBarcode())
                        .productName(f.getProduct().getName())
                        .brand(f.getProduct().getBrand())
                        .imageUrl(f.getProduct().getImageUrl())
                        .finalScore(f.getProduct().getFinalScore())
                        .scoreColor(f.getProduct().getScoreColor())
                        .savedAt(f.getSavedAt().toString())
                        .build())
                .toList();
    }
}