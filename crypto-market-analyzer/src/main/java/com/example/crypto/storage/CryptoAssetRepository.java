package com.example.crypto.storage;

import com.example.crypto.model.CryptoAsset;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CryptoAssetRepository {

    public void saveIfNotExists(CryptoAsset asset) {
        String sql = """
                INSERT INTO crypto_assets (symbol, name)
                VALUES (?, ?)
                ON CONFLICT (symbol) DO NOTHING
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, asset.getSymbol());
            statement.setString(2, asset.getName());
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save crypto asset: " + asset.getSymbol(), e);
        }
    }

    public int saveAndGetId(CryptoAsset asset) {
        saveIfNotExists(asset);

        return findBySymbol(asset.getSymbol())
                .orElseThrow(() -> new RuntimeException("Asset not found: " + asset.getSymbol()))
                .getId();
    }

    public Optional<CryptoAsset> findBySymbol(String symbol) {
        String sql = """
                SELECT id, symbol, name, added_date
                FROM crypto_assets
                WHERE symbol = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, symbol);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    CryptoAsset asset = mapRow(resultSet);
                    return Optional.of(asset);
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find crypto asset by symbol: " + symbol, e);
        }
    }

    public List<CryptoAsset> findAll() {
        String sql = """
                SELECT id, symbol, name, added_date
                FROM crypto_assets
                ORDER BY symbol
                """;

        List<CryptoAsset> assets = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                assets.add(mapRow(resultSet));
            }

            return assets;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all crypto assets", e);
        }
    }

    private CryptoAsset mapRow(ResultSet resultSet) throws SQLException {
        return new CryptoAsset(
                resultSet.getInt("id"),
                resultSet.getString("symbol"),
                resultSet.getString("name"),
                resultSet.getTimestamp("added_date").toLocalDateTime()
        );
    }
}