package com.example.crypto.model;

import java.math.BigDecimal;

public record LeaderRankingResult(
        String metricType,
        int rank,
        String symbol,
        BigDecimal value
) {
}