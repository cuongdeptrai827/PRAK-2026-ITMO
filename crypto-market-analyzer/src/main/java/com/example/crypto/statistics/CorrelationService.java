package com.example.crypto.statistics;

import com.example.crypto.model.CorrelationResult;
import com.example.crypto.model.MarketQuote;
import com.example.crypto.storage.MarketQuoteRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

public class CorrelationService {

    private final MarketQuoteRepository marketQuoteRepository = new MarketQuoteRepository();

    public CorrelationResult calculatePearson(String asset1, String asset2, LocalDate from, LocalDate to) {
        List<MarketQuote> quotes1 = marketQuoteRepository.findBestQuotesForPriceAnalysis(asset1, from, to);
        List<MarketQuote> quotes2 = marketQuoteRepository.findBestQuotesForPriceAnalysis(asset2, from, to);

        Map<LocalDate, BigDecimal> returns1 = calculateReturnsByDate(quotes1);
        Map<LocalDate, BigDecimal> returns2 = calculateReturnsByDate(quotes2);

        List<Double> x = new ArrayList<>();
        List<Double> y = new ArrayList<>();

        for (LocalDate date : returns1.keySet()) {
            if (returns2.containsKey(date)) {
                x.add(returns1.get(date).doubleValue());
                y.add(returns2.get(date).doubleValue());
            }
        }

        if (x.size() < 2) {
            throw new RuntimeException("Not enough common return points to calculate correlation.");
        }

        double correlation = pearson(x, y);

        return new CorrelationResult(
                asset1.toUpperCase(),
                asset2.toUpperCase(),
                "Pearson",
                BigDecimal.valueOf(correlation).setScale(6, RoundingMode.HALF_UP),
                x.size()
        );
    }

    private Map<LocalDate, BigDecimal> calculateReturnsByDate(List<MarketQuote> quotes) {
        Map<LocalDate, BigDecimal> returns = new LinkedHashMap<>();

        for (int i = 1; i < quotes.size(); i++) {
            BigDecimal previousPrice = quotes.get(i - 1).getClosePrice();
            BigDecimal currentPrice = quotes.get(i).getClosePrice();

            if (previousPrice == null || currentPrice == null || previousPrice.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            BigDecimal dailyReturn = currentPrice.subtract(previousPrice)
                    .divide(previousPrice, 8, RoundingMode.HALF_UP);

            LocalDate date = quotes.get(i).getTimestamp().toLocalDate();
            returns.put(date, dailyReturn);
        }

        return returns;
    }

    private double pearson(List<Double> x, List<Double> y) {
        int n = x.size();

        double averageX = x.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double averageY = y.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double numerator = 0.0;
        double sumSquaredX = 0.0;
        double sumSquaredY = 0.0;

        for (int i = 0; i < n; i++) {
            double dx = x.get(i) - averageX;
            double dy = y.get(i) - averageY;

            numerator += dx * dy;
            sumSquaredX += dx * dx;
            sumSquaredY += dy * dy;
        }

        double denominator = Math.sqrt(sumSquaredX * sumSquaredY);

        if (denominator == 0.0) {
            return 0.0;
        }

        return numerator / denominator;
    }


    public CorrelationResult calculateSpearman(
            String asset1,
            String asset2,
            LocalDate from,
            LocalDate to
    ) {
        List<MarketQuote> quotes1 = marketQuoteRepository.findBestQuotesForPriceAnalysis(asset1, from, to);
        List<MarketQuote> quotes2 = marketQuoteRepository.findBestQuotesForPriceAnalysis(asset2, from, to);

        Map<LocalDate, BigDecimal> returns1 = calculateDailyReturnsByDate(quotes1);
        Map<LocalDate, BigDecimal> returns2 = calculateDailyReturnsByDate(quotes2);

        List<BigDecimal> alignedReturns1 = new ArrayList<>();
        List<BigDecimal> alignedReturns2 = new ArrayList<>();

        for (LocalDate date : returns1.keySet()) {
            if (returns2.containsKey(date)) {
                alignedReturns1.add(returns1.get(date));
                alignedReturns2.add(returns2.get(date));
            }
        }

        if (alignedReturns1.size() < 2) {
            throw new RuntimeException("Not enough common data points to calculate Spearman correlation.");
        }

        List<BigDecimal> ranks1 = rank(alignedReturns1);
        List<BigDecimal> ranks2 = rank(alignedReturns2);

        BigDecimal spearman = calculatePearsonValue(ranks1, ranks2);

        return new CorrelationResult(
                asset1.toUpperCase(),
                asset2.toUpperCase(),
                "Spearman",
                spearman,
                alignedReturns1.size()
        );
    }

    private Map<LocalDate, BigDecimal> calculateDailyReturnsByDate(List<MarketQuote> quotes) {
        Map<LocalDate, BigDecimal> result = new LinkedHashMap<>();

        for (int i = 1; i < quotes.size(); i++) {
            MarketQuote previousQuote = quotes.get(i - 1);
            MarketQuote currentQuote = quotes.get(i);

            BigDecimal previousPrice = previousQuote.getClosePrice();
            BigDecimal currentPrice = currentQuote.getClosePrice();

            if (previousPrice == null || currentPrice == null || previousPrice.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            BigDecimal dailyReturn = currentPrice.subtract(previousPrice)
                    .divide(previousPrice, 8, RoundingMode.HALF_UP);

            result.put(currentQuote.getTimestamp().toLocalDate(), dailyReturn);
        }

        return result;
    }

    private List<BigDecimal> rank(List<BigDecimal> values) {
        List<RankItem> items = new ArrayList<>();

        for (int i = 0; i < values.size(); i++) {
            items.add(new RankItem(i, values.get(i)));
        }

        items.sort(Comparator.comparing(RankItem::value));

        BigDecimal[] ranks = new BigDecimal[values.size()];

        for (int i = 0; i < items.size(); i++) {
            ranks[items.get(i).index()] = BigDecimal.valueOf(i + 1);
        }

        return List.of(ranks);
    }

    private BigDecimal calculatePearsonValue(
            List<BigDecimal> values1,
            List<BigDecimal> values2
    ) {
        int n = values1.size();

        double sumX = 0.0;
        double sumY = 0.0;
        double sumXY = 0.0;
        double sumX2 = 0.0;
        double sumY2 = 0.0;

        for (int i = 0; i < n; i++) {
            double x = values1.get(i).doubleValue();
            double y = values2.get(i).doubleValue();

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
            sumY2 += y * y;
        }

        double numerator = n * sumXY - sumX * sumY;
        double denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));

        if (denominator == 0.0) {
            return BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);
        }

        return BigDecimal.valueOf(numerator / denominator)
                .setScale(6, RoundingMode.HALF_UP);
    }

    private record RankItem(
            int index,
            BigDecimal value
    ) {
    }
}