package com.nutriscanner.api.repository;

import com.nutriscanner.api.model.ScanHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScanHistoryRepository extends JpaRepository<ScanHistory, UUID> {
    List<ScanHistory> findByUserIdOrderByScannedAtDesc(UUID userId);
    void deleteByUserId(UUID userId);
}