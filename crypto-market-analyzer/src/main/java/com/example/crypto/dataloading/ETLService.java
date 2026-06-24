package com.example.crypto.dataloading;

import com.example.crypto.model.BinanceKlinePoint;
import com.example.crypto.model.CoinGeckoMarketPoint;
import com.example.crypto.model.CryptoAsset;
import com.example.crypto.model.MarketQuote;
import com.example.crypto.storage.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ETLService {

    private final CoinGeckoClient coinGeckoClient = new CoinGeckoClient();
    private final BinanceClient binanceClient = new BinanceClient();

    private final CryptoAssetRepository cryptoAssetRepository = new CryptoAssetRepository();
    private final ExchangeRepository exchangeRepository = new ExchangeRepository();
    private final TradingPairRepository tradingPairRepository = new TradingPairRepository();
    private final MarketQuoteRepository marketQuoteRepository = new MarketQuoteRepository();
    private final EtlLogRepository etlLogRepository = new EtlLogRepository();

    private static final Map<String, String> ASSET_NAMES = Map.of(
            "BTC", "Bitcoin",
            "ETH", "Ethereum"
    );

    private static final Map<String, String> COINGECKO_IDS = Map.of(
            "BTC", "bitcoin",
            "ETH", "ethereum"
    );

    private static final Map<String, String> BINANCE_SYMBOLS = Map.of(
            "BTC", "BTCUSDT",
            "ETH", "ETHUSDT"
    );

    public void loadMarketData(List<String> assets, LocalDate from, LocalDate to, String source) {
        System.out.println("Starting ETL process...");

        for (String rawSymbol : assets) {
            String symbol = rawSymbol.trim().toUpperCase();

            if (!ASSET_NAMES.containsKey(symbol)) {
                System.out.println("Unsupported asset skipped: " + symbol);
                continue;
            }

            int assetId = cryptoAssetRepository.saveAndGetId(
                    new CryptoAsset(symbol, ASSET_NAMES.get(symbol))
            );

            if (source.equalsIgnoreCase("coingecko") || source.equalsIgnoreCase("both")) {
                loadFromCoinGecko(symbol, assetId, from, to);
            }

            if (source.equalsIgnoreCase("binance") || source.equalsIgnoreCase("both")) {
                loadFromBinance(symbol, assetId, from, to);
            }
        }

        System.out.println("ETL process finished.");
    }

    private void loadFromCoinGecko(String symbol, int assetId, LocalDate from, LocalDate to) {
        LocalDateTime startedAt = LocalDateTime.now();

        try {
            String coinId = COINGECKO_IDS.get(symbol);

            int exchangeId = exchangeRepository.saveAndGetId("CoinGecko");
            int pairId = tradingPairRepository.saveAndGetId(
                    assetId,
                    "USD",
                    exchangeId,
                    symbol + "USD"
            );

            List<CoinGeckoMarketPoint> points = coinGeckoClient.getMarketChartRange(coinId, from, to);

            for (CoinGeckoMarketPoint point : points) {
                MarketQuote quote = new MarketQuote(
                        assetId,
                        exchangeId,
                        pairId,
                        point.timestamp(),
                        null,
                        point.closePrice(),
                        null,
                        null,
                        point.volume(),
                        point.marketCap(),
                        "CoinGecko"
                );

                marketQuoteRepository.upsert(quote);
            }

            String message = "Loaded " + points.size() + " records for " + symbol + " from CoinGecko.";
            System.out.println(message);
            etlLogRepository.save("CoinGecko", "SUCCESS", startedAt, LocalDateTime.now(), message);

        } catch (Exception e) {
            String message = "Failed to load " + symbol + " from CoinGecko: " + e.getMessage();
            System.out.println(message);
            e.printStackTrace();

            etlLogRepository.save("CoinGecko", "ERROR", startedAt, LocalDateTime.now(), message);
        }
    }

    private void loadFromBinance(String symbol, int assetId, LocalDate from, LocalDate to) {
        LocalDateTime startedAt = LocalDateTime.now();

        try {
            String binanceSymbol = BINANCE_SYMBOLS.get(symbol);

            int exchangeId = exchangeRepository.saveAndGetId("Binance");
            int pairId = tradingPairRepository.saveAndGetId(
                    assetId,
                    "USDT",
                    exchangeId,
                    binanceSymbol
            );

            List<BinanceKlinePoint> points = binanceClient.getDailyKlines(binanceSymbol, from, to);

            for (BinanceKlinePoint point : points) {
                MarketQuote quote = new MarketQuote(
                        assetId,
                        exchangeId,
                        pairId,
                        point.timestamp(),
                        point.openPrice(),
                        point.closePrice(),
                        point.highPrice(),
                        point.lowPrice(),
                        point.volume(),
                        null,
                        "Binance"
                );

                marketQuoteRepository.upsert(quote);
            }

            String message = "Loaded " + points.size() + " records for " + symbol + " from Binance.";
            System.out.println(message);
            etlLogRepository.save("Binance", "SUCCESS", startedAt, LocalDateTime.now(), message);

        } catch (Exception e) {
            String message = "Failed to load " + symbol + " from Binance: " + e.getMessage();
            System.out.println(message);
            etlLogRepository.save("Binance", "ERROR", startedAt, LocalDateTime.now(), message);
        }
    }
}