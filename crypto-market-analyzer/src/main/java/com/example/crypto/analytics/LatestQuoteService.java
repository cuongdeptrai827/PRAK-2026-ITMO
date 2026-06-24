package com.example.crypto.analytics;

import com.example.crypto.model.LatestQuoteResult;
import com.example.crypto.model.MarketQuote;
import com.example.crypto.storage.MarketQuoteRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LatestQuoteService {

    private final MarketQuoteRepository marketQuoteRepository = new MarketQuoteRepository();

    public List<LatestQuoteResult> getLatestQuotes(
            List<String> assets,
            LocalDate from,
            LocalDate to
    ) {
        List<LatestQuoteResult> results = new ArrayList<>();

        for (String asset : assets) {
            String symbol = asset.trim().toUpperCase();

            LatestQuoteResult latestQuote = getLatestQuote(symbol, from, to);

            if (latestQuote != null) {
                results.add(latestQuote);
            }
        }

        return results;
    }

    private LatestQuoteResult getLatestQuote(
            String symbol,
            LocalDate from,
            LocalDate to
    ) {
        List<MarketQuote> priceQuotes = marketQuoteRepository.findBestQuotesForPriceAnalysis(symbol, from, to);

        if (priceQuotes.isEmpty()) {
            return null;
        }

        MarketQuote latestPriceQuote = priceQuotes.get(priceQuotes.size() - 1);

        BigDecimal marketCap = findLatestMarketCap(symbol, from, to);

        return new LatestQuoteResult(
                symbol,
                latestPriceQuote.getTimestamp(),
                latestPriceQuote.getSource(),
                latestPriceQuote.getClosePrice(),
                latestPriceQuote.getVolume(),
                marketCap
        );
    }

    private BigDecimal findLatestMarketCap(
            String symbol,
            LocalDate from,
            LocalDate to
    ) {
        List<MarketQuote> coinGeckoQuotes = marketQuoteRepository.findByAssetSymbolAndSource(
                symbol,
                "CoinGecko",
                from,
                to
        );

        for (int i = coinGeckoQuotes.size() - 1; i >= 0; i--) {
            BigDecimal marketCap = coinGeckoQuotes.get(i).getMarketCap();

            if (marketCap != null) {
                return marketCap;
            }
        }

        return null;
    }
}