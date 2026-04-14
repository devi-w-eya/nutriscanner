package com.nutriscanner.api.service;

import com.nutriscanner.api.model.Additive;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ScoringService {

    public int calculateScore(List<Additive> additives, Integer novaGroup) {

        int novaCeiling = getNovaBase(novaGroup);

        // Check if any HIGH risk additive exists
        boolean containsHigh = additives != null && additives.stream()
                .anyMatch(a -> "HIGH".equals(a.getRiskLevel()));

        if (containsHigh) {
            // DANGER MODE — NOVA ceiling kept, cap at 6
            int score = novaCeiling;
            for (Additive additive : additives) {
                switch (additive.getRiskLevel()) {
                    case "HIGH"   -> score -= 8;
                    case "MEDIUM" -> score -= 3;
                    // LOW → ignored
                }
            }
            score = Math.max(0, score);
            return Math.min(score, 6); // cap at 6 → always RED
        }

        // NORMAL MODE — NOVA ceiling + deductions
        int score = novaCeiling;
        if (additives != null) {
            for (Additive additive : additives) {
                score -= getDeduction(additive.getRiskLevel());
            }
        }

        return Math.max(0, score);
    }

    public int getNovaBase(Integer novaGroup) {
        if (novaGroup == null) return 15;
        return switch (novaGroup) {
            case 1 -> 20;
            case 2 -> 18;
            case 3 -> 15;
            case 4 -> 13;
            default -> 15;
        };
    }

    public int getDeduction(String riskLevel) {
        return switch (riskLevel) {
            case "LOW"    -> 2;
            case "MEDIUM" -> 4;
            case "HIGH"   -> 8;
            default       -> 0;
        };
    }

    public String getColor(int score) {
        if (score >= 14) return "GREEN";
        if (score >= 7)  return "ORANGE";
        return "RED";
    }

    public String getLabel(int score) {
        if (score >= 14) return "Sûr";
        if (score >= 7)  return "Modérément dangereux";
        return "Dangereux";
    }

}