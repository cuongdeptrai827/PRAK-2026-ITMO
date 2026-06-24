package com.example.crypto.storage.metric;

import com.example.crypto.storage.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;

public class CorrelationMetricRepository {

    public void save(
            int asset1Id,
            int asset2Id,
            String method,
            LocalDate from,
            LocalDate to,
            BigDecimal correlationValue
    ) {
        deleteExisting(asset1Id, asset2Id, method, from, to);
        insert(asset1Id, asset2Id, method, from, to, correlationValue);
    }

    private void deleteExisting(
            int asset1Id,
            int asset2Id,
            String method,
            LocalDate from,
            LocalDate to
    ) {
        String sql = """
                DELETE FROM correlation_metrics
                WHERE asset_1_id = ?
                  AND asset_2_id = ?
                  AND method = ?
                  AND period_start = ?
                  AND period_end = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, asset1Id);
            statement.setInt(2, asset2Id);
            statement.setString(3, method);
            statement.setTimestamp(4, Timestamp.valueOf(from.atStartOfDay()));
            statement.setTimestamp(5, Timestamp.valueOf(to.atStartOfDay()));

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete existing correlation metric", e);
        }
    }

    private void insert(
            int asset1Id,
            int asset2Id,
            String method,
            LocalDate from,
            LocalDate to,
            BigDecimal correlationValue
    ) {
        String sql = """
                INSERT INTO correlation_metrics (
                    asset_1_id,
                    asset_2_id,
                    method,
                    period_start,
                    period_end,
                    correlation_value
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, asset1Id);
            statement.setInt(2, asset2Id);
            statement.setString(3, method);
            statement.setTimestamp(4, Timestamp.valueOf(from.atStartOfDay()));
            statement.setTimestamp(5, Timestamp.valueOf(to.atStartOfDay()));
            statement.setBigDecimal(6, correlationValue);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert correlation metric", e);
        }
    }
}