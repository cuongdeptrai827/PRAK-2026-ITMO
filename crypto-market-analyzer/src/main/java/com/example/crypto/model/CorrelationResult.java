package com.example.crypto.model;

import java.math.BigDecimal;

public record CorrelationResult(
        String asset1,
        String asset2,
        String method,
        BigDecimal correlationValue,
        int dataPoints
) {
}