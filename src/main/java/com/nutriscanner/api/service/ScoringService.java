package com.nutriscanner.api.service;

import com.nutriscanner.api.model.Additive;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ScoringService {

    public int calculateScore(List<Additive> additives, Integer novaGroup) {
        int novaPoints = getNovaBase(novaGroup);
        double additivePoints = 14.0;

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
        if (novaGroup == null) return 0;
        return switch (novaGroup) {
            case 1 -> 6;
            case 2 -> 5;
            case 3 -> 3;
            case 4 -> 2;
            default -> 0;
        };
    }

    public int getDeduction(String riskLevel) {
        return switch (riskLevel) {
            case "LOW"         -> 0;
            case "MEDIUM"      -> 1;
            case "MEDIUM_HIGH" -> 3;
            case "HIGH"        -> 5;
            default            -> 1;
        };
    }

    private double getDeductionDouble(String riskLevel) {
        return switch (riskLevel) {
            case "LOW"         -> 0.0;
            case "MEDIUM"      -> 1.0;
            case "MEDIUM_HIGH" -> 3.0;
            case "HIGH"        -> 5.0;
            default            -> 1.0;
        };
    }

    public String getColor(int score, List<Additive> additives) {
        long highCount = additives == null ? 0 :
                additives.stream()
                        .filter(a -> "HIGH".equals(a.getRiskLevel()))
                        .count();

        if (highCount >= 2) return "RED";
        if (highCount == 1) {
            if (score >= 6) return "AMBER";
            return "RED";
        }

        if (score >= 16) return "GREEN";
        if (score >= 11) return "ORANGE";
        if (score >= 6)  return "AMBER";
        return "RED";
    }

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