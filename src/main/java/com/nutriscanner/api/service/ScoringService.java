package com.nutriscanner.api.service;

import com.nutriscanner.api.model.Additive;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ScoringService {

    public int calculateScore(List<Additive> additives, Integer novaGroup) {
        int novaPoints = getNovaBase(novaGroup);
        double additivePoints = 10.0;

        if (additives != null && !additives.isEmpty()) {
            for (Additive a : additives) {
                additivePoints -= getDeductionDouble(a.getRiskLevel());
            }
            additivePoints = Math.max(0, additivePoints);

            int count = additives.size();
            if (count >= 9)      additivePoints -= 2.0;
            else if (count >= 6) additivePoints -= 1.0;
            else if (count >= 4) additivePoints -= 0.5;

            additivePoints = Math.max(0, additivePoints);
        }

        double raw = novaPoints + additivePoints;
        raw = Math.max(0, Math.min(20, raw));
        return (int) Math.round(raw);
    }

    public int getNovaBase(Integer novaGroup) {
        if (novaGroup == null) return 4;
        return switch (novaGroup) {
            case 1 -> 10;
            case 2 -> 8;
            case 3 -> 6;
            case 4 -> 4;
            default -> 4;
        };
    }

    public int getDeduction(String riskLevel) {
        return switch (riskLevel) {
            case "LOW"         -> 0;
            case "MEDIUM"      -> 1;
            case "MEDIUM_HIGH" -> 2;
            case "HIGH"        -> 4;
            default            -> 1;
        };
    }

    private double getDeductionDouble(String riskLevel) {
        return switch (riskLevel) {
            case "LOW"         -> 0.0;
            case "MEDIUM"      -> 1.0;
            case "MEDIUM_HIGH" -> 2.0;
            case "HIGH"        -> 4.0;
            default            -> 1.0;
        };
    }

    // With additives list — applies hard overrides
    public String getColor(int score, List<Additive> additives) {
        long highCount = additives == null ? 0 :
                additives.stream()
                        .filter(a -> "HIGH".equals(a.getRiskLevel()))
                        .count();

        // 2+ HIGH → always RED
        if (highCount >= 2) return "RED";

        // 1 HIGH → max AMBER (never GREEN or ORANGE)
        if (highCount == 1) {
            if (score >= 6) return "AMBER";
            return "RED";
        }

        // Normal bands
        if (score >= 16) return "GREEN";
        if (score >= 11) return "ORANGE";
        if (score >= 6)  return "AMBER";
        return "RED";
    }

    // Without additives — for cached products
    public String getColor(int score) {
        if (score >= 16) return "GREEN";
        if (score >= 11) return "ORANGE";
        if (score >= 6)  return "AMBER";
        return "RED";
    }

    public String getLabel(int score) {
        if (score >= 16) return "Sûr";
        if (score >= 11) return "Modéré";
        if (score >= 6)  return "Risqué";
        return "Dangereux";
    }
}