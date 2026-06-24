package com.example.crypto.analytics;

import com.example.crypto.model.AnalysisResult;
import com.example.crypto.model.LeaderRankingResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class LeaderRankingService {

    public List<LeaderRankingResult> calculateRankings(List<AnalysisResult> analysisResults) {
        List<LeaderRankingResult> rankings = new ArrayList<>();

        rankings.addAll(rankByMetric(
                analysisResults,
                "MARKET_CAP",
                AnalysisResult::lastMarketCap
        ));

        rankings.addAll(rankByMetric(
                analysisResults,
                "TRADING_VOLUME",
                AnalysisResult::averageVolume
        ));

        rankings.addAll(rankByMetric(
                analysisResults,
                "RETURN",
                AnalysisResult::priceChangePercent
        ));

        return rankings;
    }

    private List<LeaderRankingResult> rankByMetric(
            List<AnalysisResult> analysisResults,
            String metricType,
            Function<AnalysisResult, BigDecimal> valueExtractor
    ) {
        if (analysisResults == null || analysisResults.isEmpty()) {
            return List.of();
        }

        List<AnalysisResult> filteredResults = analysisResults.stream()
                .filter(result -> valueExtractor.apply(result) != null)
                .sorted(Comparator.comparing(
                        valueExtractor,
                        Comparator.reverseOrder()
                ))
                .toList();

        List<LeaderRankingResult> rankings = new ArrayList<>();

        for (int i = 0; i < filteredResults.size(); i++) {
            AnalysisResult result = filteredResults.get(i);

            rankings.add(new LeaderRankingResult(
                    metricType,
                    i + 1,
                    result.symbol(),
                    valueExtractor.apply(result)
            ));
        }

        return rankings;
    }
}