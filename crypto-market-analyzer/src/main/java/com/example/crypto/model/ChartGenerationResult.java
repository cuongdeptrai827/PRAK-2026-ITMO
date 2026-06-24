package com.example.crypto.model;

public record ChartGenerationResult(
        String priceChartPath,
        String volumeChartPath,
        String marketCapChartPath,
        String returnsChartPath,
        String correlationHeatmapPath
) {
}