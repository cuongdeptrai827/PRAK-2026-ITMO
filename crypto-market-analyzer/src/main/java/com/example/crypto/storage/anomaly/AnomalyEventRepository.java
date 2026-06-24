package com.example.crypto.storage.anomaly;

import com.example.crypto.model.AnomalyEvent;
import com.example.crypto.storage.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;

public class AnomalyEventRepository {

    public void deleteByAssetAndPeriod(
            int assetId,
            LocalDate from,
            LocalDate to
    ) {
        String sql = """
                DELETE FROM anomaly_events
                WHERE asset_id = ?
                  AND timestamp >= ?
                  AND timestamp < ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, assetId);
            statement.setTimestamp(2, Timestamp.valueOf(from.atStartOfDay()));
            statement.setTimestamp(3, Timestamp.valueOf(to.plusDays(1).atStartOfDay()));

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete old anomaly events", e);
        }
    }

    public void save(
            int assetId,
            AnomalyEvent anomalyEvent
    ) {
        String sql = """
                INSERT INTO anomaly_events (
                    asset_id,
                    event_type,
                    timestamp,
                    value,
                    description,
                    severity
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, assetId);
            statement.setString(2, anomalyEvent.eventType());
            statement.setTimestamp(3, Timestamp.valueOf(anomalyEvent.timestamp()));
            statement.setBigDecimal(4, anomalyEvent.value());
            statement.setString(5, anomalyEvent.description());
            statement.setString(6, anomalyEvent.severity());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save anomaly event", e);
        }
    }
}