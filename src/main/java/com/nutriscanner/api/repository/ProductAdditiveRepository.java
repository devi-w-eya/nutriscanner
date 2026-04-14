package com.nutriscanner.api.repository;

import com.nutriscanner.api.model.ProductAdditive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductAdditiveRepository extends JpaRepository<ProductAdditive, UUID> {
    List<ProductAdditive> findByProductId(UUID productId);
}