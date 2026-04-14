package com.nutriscanner.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "product_additives")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAdditive {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "additive_id", nullable = false)
    private Additive additive;

    @Column(name = "deduction_applied", nullable = false)
    @Builder.Default
    private Integer deductionApplied = 0;
}