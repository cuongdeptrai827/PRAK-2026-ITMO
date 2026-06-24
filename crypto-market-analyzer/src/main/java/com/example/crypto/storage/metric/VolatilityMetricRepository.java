package com.example.crypto.storage.metric;

import com.example.crypto.storage.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;

public class VolatilityMetricRepository {

    public void save(
            int assetId,
            String periodType,
            LocalDate from,
            LocalDate to,
            BigDecimal volatilityValue,
            BigDecimal rmsdReturn
    ) {
        deleteExisting(assetId, periodType, from, to);
        insert(assetId, periodType, from, to, volatilityValue, rmsdReturn);
    }

    private void deleteExisting(
            int assetId,
            String periodType,
            LocalDate from,
            LocalDate to
    ) {
        String sql = """
                DELETE FROM volatility_metrics
                WHERE asset_id = ?
                  AND period_type = ?
                  AND period_start = ?
                  AND period_end = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, assetId);
            statement.setString(2, periodType);
            statement.setTimestamp(3, Timestamp.valueOf(from.atStartOfDay()));
            statement.setTimestamp(4, Timestamp.valueOf(to.atStartOfDay()));

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete existing volatility metric", e);
        }
    }

    private void insert(
            int assetId,
            String periodType,
            LocalDate from,
            LocalDate to,
            BigDecimal volatilityValue,
            BigDecimal rmsdReturn
    ) {
        String sql = """
                INSERT INTO volatility_metrics (
                    asset_id,
                    period_type,
                    period_start,
                    period_end,
                    volatility_value,
                    rmsd_return
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, assetId);
            statement.setString(2, periodType);
            statement.setTimestamp(3, Timestamp.valueOf(from.atStartOfDay()));
            statement.setTimestamp(4, Timestamp.valueOf(to.atStartOfDay()));
            setBigDecimalOrNull(statement, 5, volatilityValue);
            setBigDecimalOrNull(statement, 6, rmsdReturn);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert volatility metric", e);
        }
    }

    private void setBigDecimalOrNull(
            PreparedStatement statement,
            int index,
            BigDecimal value
    ) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.NUMERIC);
        } else {
            statement.setBigDecimal(index, value);
        }
    }
}