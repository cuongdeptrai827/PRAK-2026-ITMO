package com.example.crypto.analytics;

import com.example.crypto.model.AnomalyEvent;

import java.math.BigDecimal;
import java.util.List;

public class AnomalyConsoleService {

    public void printAnomalies(List<AnomalyEvent> anomalies) {
        System.out.println();
        System.out.println("Anomaly detection");
        System.out.println("--------------------------------");

        if (anomalies == null || anomalies.isEmpty()) {
            System.out.println("No anomalies detected.");
            return;
        }

        System.out.println("Detected anomalies: " + anomalies.size());

        for (AnomalyEvent anomaly : anomalies) {
            System.out.println();
            System.out.println("Asset: " + anomaly.symbol());
            System.out.println("Type: " + anomaly.eventType());
            System.out.println("Timestamp: " + anomaly.timestamp());
            System.out.println("Value: " + format(anomaly.value()));
            System.out.println("Severity: " + anomaly.severity());
            System.out.println("Description: " + anomaly.description());
        }
    }

    private String format(BigDecimal value) {
        if (value == null) {
            return "N/A";
        }

        return value.stripTrailingZeros().toPlainString();
    }
}