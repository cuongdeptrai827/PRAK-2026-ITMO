package com.example.crypto.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BinanceKlinePoint(
        LocalDateTime timestamp,
        BigDecimal openPrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal closePrice,
        BigDecimal volume
) {
}