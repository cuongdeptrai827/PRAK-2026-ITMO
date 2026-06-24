package com.example.crypto.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AnomalyEvent(
        String symbol,
        String eventType,
        LocalDateTime timestamp,
        BigDecimal value,
        String description,
        String severity
) {
}