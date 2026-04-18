package com.nutriscanner.api.repository;

import com.nutriscanner.api.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    List<Favorite> findByUserIdOrderBySavedAtDesc(UUID userId);
    Optional<Favorite> findByUserIdAndProductBarcode(UUID userId, String barcode);
    boolean existsByUserIdAndProductBarcode(UUID userId, String barcode);
    void deleteByUserId(UUID userId);
}