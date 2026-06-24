package com.example.crypto.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ForecastResult(
        String symbol,
        String targetType,
        LocalDate forecastDate,
        BigDecimal predictedValue,
        String modelName,
        int trainingPoints
) {
}