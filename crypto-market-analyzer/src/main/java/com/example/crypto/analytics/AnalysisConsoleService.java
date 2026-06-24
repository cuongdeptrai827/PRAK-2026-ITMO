package com.example.crypto.analytics;

import com.example.crypto.model.AnalysisResult;
import com.example.crypto.model.CorrelationResult;
import com.example.crypto.model.VolatilityResult;
import com.example.crypto.statistics.CorrelationService;
import com.example.crypto.statistics.VolatilityService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AnalysisConsoleService {

    private final AnalyticsService analyticsService = new AnalyticsService();
    private final VolatilityService volatilityService = new VolatilityService();
    private final CorrelationService correlationService = new CorrelationService();

    public void runAnalysis(List<String> assets, LocalDate from, LocalDate to) {
        System.out.println();
        System.out.println("Starting analytical calculations...");
        System.out.println("================================");

        for (String asset : assets) {
            String symbol = asset.trim().toUpperCase();

            AnalysisResult analysisResult = analyticsService.analyze(symbol, from, to);
            VolatilityResult volatilityResult = volatilityService.calculate(symbol, from, to);

            printAnalysisResult(analysisResult);
            printVolatilityResult(volatilityResult);
        }

        if (assets.size() >= 2) {
            String asset1 = assets.get(0).trim().toUpperCase();
            String asset2 = assets.get(1).trim().toUpperCase();

            CorrelationResult correlationResult = correlationService.calculatePearson(asset1, asset2, from, to);
            printCorrelationResult(correlationResult);
        }

        System.out.println();
        System.out.println("Analytical calculations finished.");
        System.out.println("================================");
    }

    private void printAnalysisResult(AnalysisResult result) {
        System.out.println();
        System.out.println("Basic analysis for " + result.symbol());
        System.out.println("--------------------------------");
        System.out.println("Source: " + result.source());
        System.out.println("Records count: " + result.recordsCount());

        System.out.println("Min price: " + format(result.minPrice()));
        System.out.println("Max price: " + format(result.maxPrice()));
        System.out.println("Average price: " + format(result.averagePrice()));
        System.out.println("Median price: " + format(result.medianPrice()));
        System.out.println("Standard deviation: " + format(result.standardDeviation()));

        System.out.println("First price: " + format(result.firstPrice()));
        System.out.println("Last price: " + format(result.lastPrice()));
        System.out.println("Price change: " + format(result.priceChange()));
        System.out.println("Price change %: " + format(result.priceChangePercent()) + "%");

        System.out.println("Average volume: " + format(result.averageVolume()));
        System.out.println("Volume change: " + format(result.volumeChange()));
        System.out.println("Volume change %: " + format(result.volumeChangePercent()) + "%");

        System.out.println("First market cap: " + format(result.firstMarketCap()));
        System.out.println("Last market cap: " + format(result.lastMarketCap()));
        System.out.println("Market cap change: " + format(result.marketCapChange()));
        System.out.println("Market cap change %: " + format(result.marketCapChangePercent()) + "%");
    }

    private void printVolatilityResult(VolatilityResult result) {
        System.out.println();
        System.out.println("Volatility for " + result.symbol());
        System.out.println("--------------------------------");
        System.out.println("Daily volatility: " + format(result.dailyVolatility()));
        System.out.println("Weekly volatility: " + format(result.weeklyVolatility()));
        System.out.println("Monthly volatility: " + format(result.monthlyVolatility()));
        System.out.println("RMSD return: " + format(result.rmsdReturn()));
    }

    private void printCorrelationResult(CorrelationResult result) {
        System.out.println();
        System.out.println("Correlation analysis");
        System.out.println("--------------------------------");
        System.out.println("Assets: " + result.asset1() + " / " + result.asset2());
        System.out.println("Method: " + result.method());
        System.out.println("Correlation value: " + format(result.correlationValue()));
        System.out.println("Common data points: " + result.dataPoints());

        BigDecimal value = result.correlationValue();

        if (value != null) {
            double correlation = value.doubleValue();

            if (correlation > 0.7) {
                System.out.println("Interpretation: strong positive relationship.");
            } else if (correlation > 0.3) {
                System.out.println("Interpretation: moderate positive relationship.");
            } else if (correlation > -0.3) {
                System.out.println("Interpretation: weak or almost no relationship.");
            } else if (correlation > -0.7) {
                System.out.println("Interpretation: moderate negative relationship.");
            } else {
                System.out.println("Interpretation: strong negative relationship.");
            }
        }
    }

    private String format(BigDecimal value) {
        if (value == null) {
            return "N/A";
        }

        return value.stripTrailingZeros().toPlainString();
    }
}