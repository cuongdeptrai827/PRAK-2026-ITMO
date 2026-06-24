package com.example.crypto.storage.forecast;

import com.example.crypto.model.ForecastResult;
import com.example.crypto.storage.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ForecastResultRepository {

    public void save(
            int assetId,
            ForecastResult forecastResult
    ) {
        deleteExisting(assetId, forecastResult);
        insert(assetId, forecastResult);
    }

    private void deleteExisting(
            int assetId,
            ForecastResult forecastResult
    ) {
        String sql = """
                DELETE FROM forecast_results
                WHERE asset_id = ?
                  AND target_type = ?
                  AND forecast_date = ?
                  AND model_name = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, assetId);
            statement.setString(2, forecastResult.targetType());
            statement.setTimestamp(3, Timestamp.valueOf(forecastResult.forecastDate().atStartOfDay()));
            statement.setString(4, forecastResult.modelName());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete existing forecast result", e);
        }
    }

    private void insert(
            int assetId,
            ForecastResult forecastResult
    ) {
        String sql = """
                INSERT INTO forecast_results (
                    asset_id,
                    target_type,
                    forecast_date,
                    predicted_value,
                    model_name
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, assetId);
            statement.setString(2, forecastResult.targetType());
            statement.setTimestamp(3, Timestamp.valueOf(forecastResult.forecastDate().atStartOfDay()));
            statement.setBigDecimal(4, forecastResult.predictedValue());
            statement.setString(5, forecastResult.modelName());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert forecast result", e);
        }
    }
}