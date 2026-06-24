package com.example.crypto.analytics;

import com.example.crypto.model.AnalysisResult;
import com.example.crypto.model.MarketQuote;
import com.example.crypto.statistics.MathUtils;
import com.example.crypto.storage.MarketQuoteRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AnalyticsService {

    private final MarketQuoteRepository marketQuoteRepository = new MarketQuoteRepository();

    public AnalysisResult analyze(String symbol, LocalDate from, LocalDate to) {
        List<MarketQuote> priceQuotes = marketQuoteRepository.findBestQuotesForPriceAnalysis(symbol, from, to);

        if (priceQuotes.isEmpty()) {
            throw new RuntimeException("No market quotes found for asset: " + symbol);
        }

        List<BigDecimal> prices = priceQuotes.stream()
                .map(MarketQuote::getClosePrice)
                .toList();

        List<BigDecimal> volumes = priceQuotes.stream()
                .map(MarketQuote::getVolume)
                .toList();

        BigDecimal firstPrice = priceQuotes.get(0).getClosePrice();
        BigDecimal lastPrice = priceQuotes.get(priceQuotes.size() - 1).getClosePrice();

        BigDecimal firstVolume = priceQuotes.get(0).getVolume();
        BigDecimal lastVolume = priceQuotes.get(priceQuotes.size() - 1).getVolume();

        List<MarketQuote> marketCapQuotes = marketQuoteRepository.findByAssetSymbolAndSource(
                symbol,
                "CoinGecko",
                from,
                to
        );

        BigDecimal firstMarketCap = null;
        BigDecimal lastMarketCap = null;

        if (!marketCapQuotes.isEmpty()) {
            firstMarketCap = marketCapQuotes.get(0).getMarketCap();
            lastMarketCap = marketCapQuotes.get(marketCapQuotes.size() - 1).getMarketCap();
        }

        return new AnalysisResult(
                symbol.toUpperCase(),
                priceQuotes.get(0).getSource(),
                priceQuotes.size(),

                MathUtils.min(prices),
                MathUtils.max(prices),
                MathUtils.average(prices),
                MathUtils.median(prices),
                MathUtils.standardDeviation(prices),

                firstPrice,
                lastPrice,
                MathUtils.difference(lastPrice, firstPrice),
                MathUtils.percentChange(lastPrice, firstPrice),

                MathUtils.average(volumes),
                MathUtils.difference(lastVolume, firstVolume),
                MathUtils.percentChange(lastVolume, firstVolume),

                firstMarketCap,
                lastMarketCap,
                MathUtils.difference(lastMarketCap, firstMarketCap),
                MathUtils.percentChange(lastMarketCap, firstMarketCap)
        );
    }
}