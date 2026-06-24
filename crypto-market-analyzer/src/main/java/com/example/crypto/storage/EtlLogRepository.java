package com.example.crypto.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class EtlLogRepository {

    public void save(String source, String status, LocalDateTime startedAt, LocalDateTime finishedAt, String message) {
        String sql = """
                INSERT INTO etl_logs (source, status, started_at, finished_at, message)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, source);
            statement.setString(2, status);
            statement.setTimestamp(3, Timestamp.valueOf(startedAt));
            statement.setTimestamp(4, Timestamp.valueOf(finishedAt));
            statement.setString(5, message);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save ETL log", e);
        }
    }
}