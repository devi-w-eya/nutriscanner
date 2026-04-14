package com.nutriscanner.api.repository;

import com.nutriscanner.api.model.AssistantCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssistantCacheRepository extends JpaRepository<AssistantCache, UUID> {
    Optional<AssistantCache> findByProductIdAndQuestionHash(UUID productId, String questionHash);
}