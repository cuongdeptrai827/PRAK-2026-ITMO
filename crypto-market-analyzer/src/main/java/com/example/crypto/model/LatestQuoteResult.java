package com.example.crypto.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LatestQuoteResult(
        String symbol,
        LocalDateTime timestamp,
        String source,
        BigDecimal closePrice,
        BigDecimal volume,
        BigDecimal marketCap
) {
}