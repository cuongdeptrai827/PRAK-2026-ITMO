package com.example.crypto.statistics;

import com.example.crypto.model.MarketQuote;
import com.example.crypto.model.VolatilityResult;
import com.example.crypto.storage.MarketQuoteRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VolatilityService {

    private final MarketQuoteRepository marketQuoteRepository = new MarketQuoteRepository();

    public VolatilityResult calculate(String symbol, LocalDate from, LocalDate to) {
        List<MarketQuote> quotes = marketQuoteRepository.findBestQuotesForPriceAnalysis(symbol, from, to);

        if (quotes.size() < 2) {
            throw new RuntimeException("Not enough quotes to calculate volatility for " + symbol);
        }

        List<BigDecimal> dailyReturns = calculateReturnsFromQuotes(quotes);

        BigDecimal dailyVolatility = MathUtils.standardDeviation(dailyReturns);
        BigDecimal weeklyVolatility = MathUtils.standardDeviation(calculatePeriodReturnsByWeek(quotes));
        BigDecimal monthlyVolatility = MathUtils.standardDeviation(calculatePeriodReturnsByMonth(quotes));
        BigDecimal rmsdReturn = MathUtils.rmsd(dailyReturns);

        return new VolatilityResult(
                symbol.toUpperCase(),
                dailyVolatility,
                weeklyVolatility,
                monthlyVolatility,
                rmsdReturn
        );
    }

    public List<BigDecimal> calculateReturnsFromQuotes(List<MarketQuote> quotes) {
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

    private List<BigDecimal> calculatePeriodReturnsByWeek(List<MarketQuote> quotes) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        Map<String, BigDecimal> weeklyClosePrices = new LinkedHashMap<>();

        for (MarketQuote quote : quotes) {
            LocalDate date = quote.getTimestamp().toLocalDate();
            int year = date.get(weekFields.weekBasedYear());
            int week = date.get(weekFields.weekOfWeekBasedYear());

            String key = year + "-W" + week;
            weeklyClosePrices.put(key, quote.getClosePrice());
        }

        return calculateReturnsFromPrices(new ArrayList<>(weeklyClosePrices.values()));
    }

    private List<BigDecimal> calculatePeriodReturnsByMonth(List<MarketQuote> quotes) {
        Map<YearMonth, BigDecimal> monthlyClosePrices = new LinkedHashMap<>();

        for (MarketQuote quote : quotes) {
            YearMonth yearMonth = YearMonth.from(quote.getTimestamp());
            monthlyClosePrices.put(yearMonth, quote.getClosePrice());
        }

        return calculateReturnsFromPrices(new ArrayList<>(monthlyClosePrices.values()));
    }

    private List<BigDecimal> calculateReturnsFromPrices(List<BigDecimal> prices) {
        List<BigDecimal> returns = new ArrayList<>();

        for (int i = 1; i < prices.size(); i++) {
            BigDecimal previousPrice = prices.get(i - 1);
            BigDecimal currentPrice = prices.get(i);

            if (previousPrice == null || currentPrice == null || previousPrice.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            BigDecimal periodReturn = currentPrice.subtract(previousPrice)
                    .divide(previousPrice, 8, RoundingMode.HALF_UP);

            returns.add(periodReturn);
        }

        return returns;
    }
}