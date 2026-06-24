package com.example.crypto.reporting;

import com.example.crypto.analytics.AnomalyDetectionService;
import com.example.crypto.analytics.AnalyticsService;
import com.example.crypto.analytics.ForecastService;
import com.example.crypto.model.AnalysisResult;
import com.example.crypto.model.AnomalyEvent;
import com.example.crypto.model.ChartGenerationResult;
import com.example.crypto.model.CorrelationResult;
import com.example.crypto.model.ForecastResult;
import com.example.crypto.model.VolatilityResult;
import com.example.crypto.statistics.CorrelationService;
import com.example.crypto.statistics.VolatilityService;
import com.example.crypto.storage.anomaly.AnomalyPersistenceService;
import com.example.crypto.storage.forecast.ForecastPersistenceService;
import com.example.crypto.storage.metric.MetricPersistenceService;
import com.example.crypto.visualization.ChartGenerator;
import com.example.crypto.analytics.LeaderRankingService;
import com.example.crypto.model.LeaderRankingResult;
import com.example.crypto.analytics.LatestQuoteService;
import com.example.crypto.model.LatestQuoteResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReportService {

    private final AnalyticsService analyticsService = new AnalyticsService();
    private final VolatilityService volatilityService = new VolatilityService();
    private final CorrelationService correlationService = new CorrelationService();
    private final AnomalyDetectionService anomalyDetectionService = new AnomalyDetectionService();
    private final ForecastService forecastService = new ForecastService();

    private final MetricPersistenceService metricPersistenceService = new MetricPersistenceService();
    private final AnomalyPersistenceService anomalyPersistenceService = new AnomalyPersistenceService();
    private final ForecastPersistenceService forecastPersistenceService = new ForecastPersistenceService();

    private final ChartGenerator chartGenerator = new ChartGenerator();
    private final PdfReportGenerator pdfReportGenerator = new PdfReportGenerator();

    private final LeaderRankingService leaderRankingService = new LeaderRankingService();
    private final LatestQuoteService latestQuoteService = new LatestQuoteService();

    public void generateReport(
            List<String> assets,
            LocalDate from,
            LocalDate to,
            String source,
            String reportTypeValue,
            String outputFile
    ) {
        ReportType reportType = ReportType.fromString(reportTypeValue);

        System.out.println("Report generation: started.");

        List<AnalysisResult> analysisResults = new ArrayList<>();
        List<VolatilityResult> volatilityResults = new ArrayList<>();
        CorrelationResult correlationResult = null;
        CorrelationResult spearmanCorrelationResult = null;
        List<AnomalyEvent> anomalies = Collections.emptyList();
        List<ForecastResult> forecasts = Collections.emptyList();
        List<LeaderRankingResult> leaderRankings = Collections.emptyList();
        ChartGenerationResult chartGenerationResult = null;
        List<LatestQuoteResult> latestQuotes = Collections.emptyList();

        switch (reportType) {
            case ASSET -> {
                validateAssetReport(assets);

                latestQuotes = latestQuoteService.getLatestQuotes(assets, from, to);
                analysisResults = calculateAnalysis(assets, from, to);
                volatilityResults = calculateVolatility(assets, from, to);
                chartGenerationResult = chartGenerator.generateCharts(assets, from, to, "charts");

                metricPersistenceService.saveVolatilityResults(volatilityResults, from, to);

                System.out.println("Asset analysis: completed.");
                System.out.println("Metrics: saved.");
                System.out.println("Charts: generated.");
            }

            case COMPARISON -> {
                validateMultiAssetReport(assets, "comparison");


                latestQuotes = latestQuoteService.getLatestQuotes(assets, from, to);
                analysisResults = calculateAnalysis(assets, from, to);
                volatilityResults = calculateVolatility(assets, from, to);
                leaderRankings = leaderRankingService.calculateRankings(analysisResults);
                chartGenerationResult = chartGenerator.generateCharts(assets, from, to, "charts");

                metricPersistenceService.saveVolatilityResults(volatilityResults, from, to);

                System.out.println("Comparison analysis: completed.");
                System.out.println("Leader ranking: completed.");
                System.out.println("Metrics: saved.");
                System.out.println("Charts: generated.");
            }

            case CORRELATION -> {
                validateMultiAssetReport(assets, "correlation");

                correlationResult = calculateCorrelation(assets, from, to);

                String asset1 = assets.get(0).trim().toUpperCase();
                String asset2 = assets.get(1).trim().toUpperCase();

                spearmanCorrelationResult = correlationService.calculateSpearman(asset1, asset2, from, to);

                metricPersistenceService.saveCorrelationResult(correlationResult, from, to);
                metricPersistenceService.saveCorrelationResult(spearmanCorrelationResult, from, to);

                String heatmapPath = chartGenerator.generateCorrelationHeatmap(correlationResult, "charts");
                chartGenerationResult = new ChartGenerationResult(null, null, null, null, heatmapPath);

                System.out.println("Correlation analysis: completed.");
                System.out.println("Spearman correlation: completed.");
                System.out.println("Correlation heatmap: generated.");
                System.out.println("Correlation metrics: saved.");
            }

            case ANOMALIES -> {
                anomalies = anomalyDetectionService.detectAnomalies(assets, from, to);
                anomalyPersistenceService.saveAnomalies(anomalies, assets, from, to);

                System.out.println("Anomalies: " + anomalies.size() + " events detected and saved.");
            }

            case FORECAST -> {
                forecasts = forecastService.forecast(assets, from, to);
                forecastPersistenceService.saveForecastResults(forecasts);

                System.out.println("Forecast: " + forecasts.size() + " rows generated and saved.");
            }
        }

        pdfReportGenerator.generateReport(
                assets,
                from,
                to,
                source,
                reportTypeValue,
                outputFile,
                latestQuotes,
                analysisResults,
                volatilityResults,
                correlationResult,
                spearmanCorrelationResult,
                leaderRankings,
                forecasts,
                chartGenerationResult,
                anomalies
        );

        System.out.println("Report: generated successfully.");
        System.out.println("Output file: " + outputFile);
    }

    private List<AnalysisResult> calculateAnalysis(
            List<String> assets,
            LocalDate from,
            LocalDate to
    ) {
        List<AnalysisResult> results = new ArrayList<>();

        for (String asset : assets) {
            String symbol = asset.trim().toUpperCase();
            results.add(analyticsService.analyze(symbol, from, to));
        }

        return results;
    }

    private List<VolatilityResult> calculateVolatility(
            List<String> assets,
            LocalDate from,
            LocalDate to
    ) {
        List<VolatilityResult> results = new ArrayList<>();

        for (String asset : assets) {
            String symbol = asset.trim().toUpperCase();
            results.add(volatilityService.calculate(symbol, from, to));
        }

        return results;
    }

    private CorrelationResult calculateCorrelation(
            List<String> assets,
            LocalDate from,
            LocalDate to
    ) {
        String asset1 = assets.get(0).trim().toUpperCase();
        String asset2 = assets.get(1).trim().toUpperCase();

        return correlationService.calculatePearson(asset1, asset2, from, to);
    }

    private void validateAssetReport(List<String> assets) {
        if (assets.size() != 1) {
            throw new IllegalArgumentException(
                    "Report type 'asset' requires exactly one asset. Example: --assets BTC --report asset"
            );
        }
    }

    private void validateMultiAssetReport(List<String> assets, String reportType) {
        if (assets.size() < 2) {
            throw new IllegalArgumentException(
                    "Report type '" + reportType + "' requires at least two assets. Example: --assets BTC,ETH"
            );
        }
    }
}