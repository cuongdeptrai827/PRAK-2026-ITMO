package com.example.crypto.analytics;

import com.example.crypto.model.ForecastResult;
import com.example.crypto.model.MarketQuote;
import com.example.crypto.storage.MarketQuoteRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ForecastService {

    private static final int FORECAST_DAYS = 7;
    private static final String MODEL_NAME = "LinearRegression";

    private final MarketQuoteRepository marketQuoteRepository = new MarketQuoteRepository();

    public List<ForecastResult> forecast(
            List<String> assets,
            LocalDate from,
            LocalDate to
    ) {
        List<ForecastResult> results = new ArrayList<>();

        for (String asset : assets) {
            String symbol = asset.trim().toUpperCase();

            results.addAll(forecastTarget(
                    symbol,
                    from,
                    to,
                    "PRICE",
                    MarketQuote::getClosePrice
            ));

            results.addAll(forecastTarget(
                    symbol,
                    from,
                    to,
                    "VOLUME",
                    MarketQuote::getVolume
            ));
        }

        return results;
    }

    private List<ForecastResult> forecastTarget(
            String symbol,
            LocalDate from,
            LocalDate to,
            String targetType,
            Function<MarketQuote, BigDecimal> valueExtractor
    ) {
        List<MarketQuote> quotes = marketQuoteRepository.findBestQuotesForPriceAnalysis(symbol, from, to);
        List<BigDecimal> values = extractValues(quotes, valueExtractor);

        if (values.size() < 2) {
            return List.of();
        }

        RegressionLine regressionLine = calculateLinearRegression(values);

        List<ForecastResult> results = new ArrayList<>();

        for (int day = 1; day <= FORECAST_DAYS; day++) {
            int forecastX = values.size() - 1 + day;
            double predicted = regressionLine.slope() * forecastX + regressionLine.intercept();

            if (predicted < 0) {
                predicted = 0;
            }

            BigDecimal predictedValue = BigDecimal.valueOf(predicted)
                    .setScale(8, RoundingMode.HALF_UP);

            results.add(new ForecastResult(
                    symbol,
                    targetType,
                    to.plusDays(day),
                    predictedValue,
                    MODEL_NAME,
                    values.size()
            ));
        }

        return results;
    }

    private List<BigDecimal> extractValues(
            List<MarketQuote> quotes,
            Function<MarketQuote, BigDecimal> valueExtractor
    ) {
        List<BigDecimal> values = new ArrayList<>();

        for (MarketQuote quote : quotes) {
            BigDecimal value = valueExtractor.apply(quote);

            if (value != null) {
                values.add(value);
            }
        }

        return values;
    }

    private RegressionLine calculateLinearRegression(List<BigDecimal> values) {
        int n = values.size();

        double sumX = 0.0;
        double sumY = 0.0;
        double sumXY = 0.0;
        double sumX2 = 0.0;

        for (int i = 0; i < n; i++) {
            double x = i;
            double y = values.get(i).doubleValue();

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double denominator = n * sumX2 - sumX * sumX;

        if (denominator == 0.0) {
            double average = sumY / n;
            return new RegressionLine(0.0, average);
        }

        double slope = (n * sumXY - sumX * sumY) / denominator;
        double intercept = (sumY - slope * sumX) / n;

        return new RegressionLine(slope, intercept);
    }

    private record RegressionLine(
            double slope,
            double intercept
    ) {
    }
}