package com.nutriscanner.api.repository;

import com.nutriscanner.api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;
import com.nutriscanner.api.model.Category;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findByBarcode(String barcode);
    boolean existsByBarcode(String barcode);
    @Query("SELECT p FROM Product p WHERE p.category = :category " +
            "AND p.finalScore > :score AND p.id != :id " +
            "AND p.scoreColor != 'UNKNOWN' " +
            "ORDER BY p.finalScore DESC, p.scanCount DESC")
    List<Product> findRecommendations(
            @Param("category") Category category,
            @Param("score") int score,
            @Param("id") UUID id,
            org.springframework.data.domain.Pageable pageable);
}
