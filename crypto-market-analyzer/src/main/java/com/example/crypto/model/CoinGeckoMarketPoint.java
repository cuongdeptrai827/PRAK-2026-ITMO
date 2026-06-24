package com.example.crypto.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CoinGeckoMarketPoint(
        LocalDateTime timestamp,
        BigDecimal closePrice,
        BigDecimal marketCap,
        BigDecimal volume
) {
}