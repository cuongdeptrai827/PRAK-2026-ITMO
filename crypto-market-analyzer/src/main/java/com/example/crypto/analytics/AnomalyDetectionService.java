package com.example.crypto.analytics;

import com.example.crypto.model.AnomalyEvent;
import com.example.crypto.model.MarketQuote;
import com.example.crypto.statistics.MathUtils;
import com.example.crypto.storage.MarketQuoteRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AnomalyDetectionService {

    private static final BigDecimal PRICE_CHANGE_THRESHOLD_PERCENT = BigDecimal.valueOf(7);
    private static final BigDecimal EXTREME_PRICE_CHANGE_THRESHOLD_PERCENT = BigDecimal.valueOf(10);

    private static final BigDecimal VOLUME_STD_MULTIPLIER = BigDecimal.valueOf(2);
    private static final BigDecimal EXTREME_VOLUME_STD_MULTIPLIER = BigDecimal.valueOf(3);

    private static final int ROLLING_VOLATILITY_WINDOW = 7;
    private static final BigDecimal VOLATILITY_MULTIPLIER = BigDecimal.valueOf(2);

    private final MarketQuoteRepository marketQuoteRepository = new MarketQuoteRepository();

    public List<AnomalyEvent> detectAnomalies(
            List<String> assets,
            LocalDate from,
            LocalDate to
    ) {
        List<AnomalyEvent> result = new ArrayList<>();

        for (String asset : assets) {
            String symbol = asset.trim().toUpperCase();
            result.addAll(detectAnomaliesForAsset(symbol, from, to));
        }

        return result;
    }

    public List<AnomalyEvent> detectAnomaliesForAsset(
            String symbol,
            LocalDate from,
            LocalDate to
    ) {
        List<MarketQuote> quotes = marketQuoteRepository.findBestQuotesForPriceAnalysis(symbol, from, to);

        List<AnomalyEvent> anomalies = new ArrayList<>();

        if (quotes.size() < 2) {
            return anomalies;
        }

        anomalies.addAll(detectSharpPriceChanges(symbol, quotes));
        anomalies.addAll(detectAnomalousVolumes(symbol, quotes));
        anomalies.addAll(detectUnusualVolatility(symbol, quotes));

        return anomalies;
    }

    private List<AnomalyEvent> detectSharpPriceChanges(
            String symbol,
            List<MarketQuote> quotes
    ) {
        List<AnomalyEvent> anomalies = new ArrayList<>();

        for (int i = 1; i < quotes.size(); i++) {
            MarketQuote previousQuote = quotes.get(i - 1);
            MarketQuote currentQuote = quotes.get(i);

            BigDecimal previousPrice = previousQuote.getClosePrice();
            BigDecimal currentPrice = currentQuote.getClosePrice();

            if (previousPrice == null || currentPrice == null || previousPrice.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            BigDecimal priceChangePercent = currentPrice.subtract(previousPrice)
                    .divide(previousPrice, 8, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(4, RoundingMode.HALF_UP);

            BigDecimal absoluteChange = priceChangePercent.abs();

            if (absoluteChange.compareTo(PRICE_CHANGE_THRESHOLD_PERCENT) >= 0) {
                String severity = absoluteChange.compareTo(EXTREME_PRICE_CHANGE_THRESHOLD_PERCENT) >= 0
                        ? "HIGH"
                        : "MEDIUM";

                String description = "Daily close price changed by "
                        + priceChangePercent.stripTrailingZeros().toPlainString()
                        + "% compared with the previous period.";

                anomalies.add(new AnomalyEvent(
                        symbol,
                        "SHARP_PRICE_CHANGE",
                        currentQuote.getTimestamp(),
                        priceChangePercent,
                        description,
                        severity
                ));
            }
        }

        return anomalies;
    }

    private List<AnomalyEvent> detectAnomalousVolumes(
            String symbol,
            List<MarketQuote> quotes
    ) {
        List<AnomalyEvent> anomalies = new ArrayList<>();

        List<BigDecimal> volumes = quotes.stream()
                .map(MarketQuote::getVolume)
                .toList();

        BigDecimal averageVolume = MathUtils.average(volumes);
        BigDecimal volumeStd = MathUtils.standardDeviation(volumes);

        if (averageVolume == null || volumeStd == null) {
            return anomalies;
        }

        BigDecimal mediumThreshold = averageVolume.add(volumeStd.multiply(VOLUME_STD_MULTIPLIER));
        BigDecimal highThreshold = averageVolume.add(volumeStd.multiply(EXTREME_VOLUME_STD_MULTIPLIER));

        for (MarketQuote quote : quotes) {
            BigDecimal volume = quote.getVolume();

            if (volume == null) {
                continue;
            }

            if (volume.compareTo(mediumThreshold) >= 0) {
                String severity = volume.compareTo(highThreshold) >= 0 ? "HIGH" : "MEDIUM";

                String description = "Trading volume is higher than the statistical threshold. "
                        + "Average volume: " + format(averageVolume)
                        + ", current volume: " + format(volume)
                        + ".";

                anomalies.add(new AnomalyEvent(
                        symbol,
                        "ANOMALOUS_VOLUME",
                        quote.getTimestamp(),
                        volume,
                        description,
                        severity
                ));
            }
        }

        return anomalies;
    }

    private List<AnomalyEvent> detectUnusualVolatility(
            String symbol,
            List<MarketQuote> quotes
    ) {
        List<AnomalyEvent> anomalies = new ArrayList<>();

        List<BigDecimal> dailyReturns = calculateDailyReturns(quotes);
        BigDecimal baseVolatility = MathUtils.standardDeviation(dailyReturns);

        if (baseVolatility == null || baseVolatility.compareTo(BigDecimal.ZERO) == 0) {
            return anomalies;
        }

        BigDecimal threshold = baseVolatility.multiply(VOLATILITY_MULTIPLIER);

        for (int i = ROLLING_VOLATILITY_WINDOW; i < quotes.size(); i++) {
            List<BigDecimal> windowReturns = new ArrayList<>();

            int startIndex = Math.max(1, i - ROLLING_VOLATILITY_WINDOW + 1);

            for (int j = startIndex; j <= i; j++) {
                BigDecimal previousPrice = quotes.get(j - 1).getClosePrice();
                BigDecimal currentPrice = quotes.get(j).getClosePrice();

                if (previousPrice == null || currentPrice == null || previousPrice.compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }

                BigDecimal dailyReturn = currentPrice.subtract(previousPrice)
                        .divide(previousPrice, 8, RoundingMode.HALF_UP);

                windowReturns.add(dailyReturn);
            }

            BigDecimal rollingVolatility = MathUtils.standardDeviation(windowReturns);

            if (rollingVolatility == null) {
                continue;
            }

            if (rollingVolatility.compareTo(threshold) >= 0) {
                String description = "Rolling 7-period volatility is unusually high. "
                        + "Base volatility: " + format(baseVolatility)
                        + ", rolling volatility: " + format(rollingVolatility)
                        + ".";

                anomalies.add(new AnomalyEvent(
                        symbol,
                        "UNUSUAL_VOLATILITY",
                        quotes.get(i).getTimestamp(),
                        rollingVolatility,
                        description,
                        "HIGH"
                ));
            }
        }

        return anomalies;
    }

    private List<BigDecimal> calculateDailyReturns(List<MarketQuote> quotes) {
        List<BigDecimal> returns = new ArrayList<>();

        for (int i = 1; i < quotes.size(); i++) {
            BigDecimal previousPrice = quotes.get(i - 1).getClosePrice();
            BigDecimal currentPrice = quotes.get(i).getClosePrice();

            if (previousPrice == null || currentPrice == null || previousPrice.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            BigDecimal dailyReturn = currentPrice.subtract(previousPrice)
                    .divide(previousPrice, 8, RoundingMode.HALF_UP);

            returns.add(dailyReturn);
        }

        return returns;
    }

    private String format(BigDecimal value) {
        if (value == null) {
            return "N/A";
        }

        return value.setScale(4, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }
}