package com.nutriscanner.api.repository;

import com.nutriscanner.api.model.Additive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface AdditiveRepository extends JpaRepository<Additive, UUID> {
    Optional<Additive> findByCode(String code);
    @Query("SELECT a FROM Additive a WHERE " +
            "LOWER(a.code) = LOWER(:term) OR " +
            "LOWER(a.commonNames) LIKE LOWER(CONCAT('%', :term, '%'))")
    Optional<Additive> findByCodeOrCommonName(@Param("term") String term);
}