package com.example.crypto.storage;

import com.example.crypto.model.MarketQuote;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MarketQuoteRepository {

    public void upsert(MarketQuote quote) {
        String sql = """
                INSERT INTO market_quotes (
                    asset_id,
                    exchange_id,
                    trading_pair_id,
                    timestamp,
                    open_price,
                    close_price,
                    high_price,
                    low_price,
                    volume,
                    market_cap,
                    source
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (asset_id, exchange_id, trading_pair_id, timestamp, source)
                DO UPDATE SET
                    open_price = EXCLUDED.open_price,
                    close_price = EXCLUDED.close_price,
                    high_price = EXCLUDED.high_price,
                    low_price = EXCLUDED.low_price,
                    volume = EXCLUDED.volume,
                    market_cap = EXCLUDED.market_cap
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, quote.getAssetId());
            statement.setInt(2, quote.getExchangeId());
            statement.setInt(3, quote.getTradingPairId());
            statement.setTimestamp(4, Timestamp.valueOf(quote.getTimestamp()));
            statement.setBigDecimal(5, quote.getOpenPrice());
            statement.setBigDecimal(6, quote.getClosePrice());
            statement.setBigDecimal(7, quote.getHighPrice());
            statement.setBigDecimal(8, quote.getLowPrice());
            statement.setBigDecimal(9, quote.getVolume());
            statement.setBigDecimal(10, quote.getMarketCap());
            statement.setString(11, quote.getSource());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to upsert market quote", e);
        }
    }

    public List<MarketQuote> findByAssetSymbolAndSource(
            String symbol,
            String source,
            LocalDate from,
            LocalDate to
    ) {
        String sql = """
            SELECT 
                mq.id,
                mq.asset_id,
                mq.exchange_id,
                mq.trading_pair_id,
                mq.timestamp,
                mq.open_price,
                mq.close_price,
                mq.high_price,
                mq.low_price,
                mq.volume,
                mq.market_cap,
                mq.source
            FROM market_quotes mq
            JOIN crypto_assets ca ON mq.asset_id = ca.id
            WHERE ca.symbol = ?
              AND mq.source = ?
              AND mq.timestamp >= ?
              AND mq.timestamp < ?
            ORDER BY mq.timestamp
            """;

        List<MarketQuote> quotes = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, symbol.toUpperCase());
            statement.setString(2, source);
            statement.setTimestamp(3, Timestamp.valueOf(from.atStartOfDay()));
            statement.setTimestamp(4, Timestamp.valueOf(to.plusDays(1).atStartOfDay()));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    quotes.add(mapRow(resultSet));
                }
            }

            return quotes;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load market quotes for " + symbol + " from " + source, e);
        }
    }

    public List<MarketQuote> findBestQuotesForPriceAnalysis(
            String symbol,
            LocalDate from,
            LocalDate to
    ) {
        List<MarketQuote> binanceQuotes = findByAssetSymbolAndSource(symbol, "Binance", from, to);

        if (!binanceQuotes.isEmpty()) {
            return binanceQuotes;
        }

        return findByAssetSymbolAndSource(symbol, "CoinGecko", from, to);
    }

    private MarketQuote mapRow(ResultSet resultSet) throws SQLException {
        MarketQuote quote = new MarketQuote();

        quote.setId(resultSet.getInt("id"));
        quote.setAssetId(resultSet.getInt("asset_id"));
        quote.setExchangeId(resultSet.getInt("exchange_id"));
        quote.setTradingPairId(resultSet.getInt("trading_pair_id"));
        quote.setTimestamp(resultSet.getTimestamp("timestamp").toLocalDateTime());

        BigDecimal openPrice = resultSet.getBigDecimal("open_price");
        BigDecimal closePrice = resultSet.getBigDecimal("close_price");
        BigDecimal highPrice = resultSet.getBigDecimal("high_price");
        BigDecimal lowPrice = resultSet.getBigDecimal("low_price");
        BigDecimal volume = resultSet.getBigDecimal("volume");
        BigDecimal marketCap = resultSet.getBigDecimal("market_cap");

        quote.setOpenPrice(openPrice);
        quote.setClosePrice(closePrice);
        quote.setHighPrice(highPrice);
        quote.setLowPrice(lowPrice);
        quote.setVolume(volume);
        quote.setMarketCap(marketCap);
        quote.setSource(resultSet.getString("source"));

        return quote;
    }
}