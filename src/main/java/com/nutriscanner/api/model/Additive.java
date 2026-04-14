package com.nutriscanner.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "additives")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Additive {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)

    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "name_fr", nullable = false)
    private String nameFr;

    @Column(name = "risk_level", nullable = false)
    private String riskLevel;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "efsa_opinion")
    private String efsaOpinion;

    @Column(name = "ai_identified", nullable = false)
    @Builder.Default
    private Boolean aiIdentified = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean verified = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "common_names", columnDefinition = "TEXT")
    private String commonNames;
}