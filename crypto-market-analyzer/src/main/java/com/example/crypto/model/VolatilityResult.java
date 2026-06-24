package com.example.crypto.model;

import java.math.BigDecimal;

public record VolatilityResult(
        String symbol,
        BigDecimal dailyVolatility,
        BigDecimal weeklyVolatility,
        BigDecimal monthlyVolatility,
        BigDecimal rmsdReturn
) {
}