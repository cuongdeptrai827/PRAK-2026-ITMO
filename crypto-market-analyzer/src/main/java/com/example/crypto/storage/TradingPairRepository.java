package com.example.crypto.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TradingPairRepository {

    public int saveAndGetId(int baseAssetId, String quoteAsset, int exchangeId, String symbol) {
        saveIfNotExists(baseAssetId, quoteAsset, exchangeId, symbol);
        return findId(baseAssetId, quoteAsset, exchangeId, symbol);
    }

    private void saveIfNotExists(int baseAssetId, String quoteAsset, int exchangeId, String symbol) {
        String sql = """
                INSERT INTO trading_pairs (base_asset_id, quote_asset, exchange_id, symbol)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (base_asset_id, quote_asset, exchange_id, symbol) DO NOTHING
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, baseAssetId);
            statement.setString(2, quoteAsset);
            statement.setInt(3, exchangeId);
            statement.setString(4, symbol);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save trading pair: " + symbol, e);
        }
    }

    private int findId(int baseAssetId, String quoteAsset, int exchangeId, String symbol) {
        String sql = """
                SELECT id
                FROM trading_pairs
                WHERE base_asset_id = ?
                  AND quote_asset = ?
                  AND exchange_id = ?
                  AND symbol = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, baseAssetId);
            statement.setString(2, quoteAsset);
            statement.setInt(3, exchangeId);
            statement.setString(4, symbol);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }

            throw new RuntimeException("Trading pair not found: " + symbol);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find trading pair: " + symbol, e);
        }
    }
}