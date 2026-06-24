package com.example.crypto.model;

import java.math.BigDecimal;

public record AnalysisResult(
        String symbol,
        String source,
        int recordsCount,

        BigDecimal minPrice,
        BigDecimal maxPrice,
        BigDecimal averagePrice,
        BigDecimal medianPrice,
        BigDecimal standardDeviation,

        BigDecimal firstPrice,
        BigDecimal lastPrice,
        BigDecimal priceChange,
        BigDecimal priceChangePercent,

        BigDecimal averageVolume,
        BigDecimal volumeChange,
        BigDecimal volumeChangePercent,

        BigDecimal firstMarketCap,
        BigDecimal lastMarketCap,
        BigDecimal marketCapChange,
        BigDecimal marketCapChangePercent
) {
}