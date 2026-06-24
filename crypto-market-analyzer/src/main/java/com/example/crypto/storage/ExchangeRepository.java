package com.example.crypto.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExchangeRepository {

    public int saveAndGetId(String name) {
        saveIfNotExists(name);
        return findIdByName(name);
    }

    public void saveIfNotExists(String name) {
        String sql = """
                INSERT INTO exchanges (name)
                VALUES (?)
                ON CONFLICT (name) DO NOTHING
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save exchange: " + name, e);
        }
    }

    public int findIdByName(String name) {
        String sql = """
                SELECT id
                FROM exchanges
                WHERE name = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }

            throw new RuntimeException("Exchange not found: " + name);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find exchange: " + name, e);
        }
    }
}