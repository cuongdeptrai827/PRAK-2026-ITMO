package com.example.crypto.analytics;

import com.example.crypto.model.ForecastResult;

import java.math.BigDecimal;
import java.util.List;

public class ForecastConsoleService {

    public void printForecasts(List<ForecastResult> forecasts) {
        System.out.println();
        System.out.println("Forecast results");
        System.out.println("--------------------------------");

        if (forecasts == null || forecasts.isEmpty()) {
            System.out.println("No forecast results generated.");
            return;
        }

        String currentGroup = "";

        for (ForecastResult forecast : forecasts) {
            String group = forecast.symbol() + " / " + forecast.targetType();

            if (!group.equals(currentGroup)) {
                currentGroup = group;
                System.out.println();
                System.out.println(currentGroup);
            }

            System.out.println(
                    forecast.forecastDate()
                            + " -> "
                            + format(forecast.predictedValue())
                            + " ["
                            + forecast.modelName()
                            + ", training points: "
                            + forecast.trainingPoints()
                            + "]"
            );
        }
    }

    private String format(BigDecimal value) {
        if (value == null) {
            return "N/A";
        }

        return value.setScale(4, java.math.RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }
}