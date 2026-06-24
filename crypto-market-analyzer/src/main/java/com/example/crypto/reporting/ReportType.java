package com.example.crypto.reporting;

public enum ReportType {
    ASSET,
    COMPARISON,
    CORRELATION,
    ANOMALIES,
    FORECAST;

    public static ReportType fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Report type is required.");
        }

        return switch (value.trim().toLowerCase()) {
            case "asset" -> ASSET;
            case "comparison" -> COMPARISON;
            case "correlation" -> CORRELATION;
            case "anomalies" -> ANOMALIES;
            case "forecast" -> FORECAST;
            default -> throw new IllegalArgumentException(
                    "Unsupported report type: " + value +
                            ". Supported values: asset, comparison, correlation, anomalies, forecast."
            );
        };
    }
}