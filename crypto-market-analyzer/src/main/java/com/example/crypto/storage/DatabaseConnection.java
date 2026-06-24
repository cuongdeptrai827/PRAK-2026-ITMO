package com.example.crypto.storage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final String CONFIG_FILE = "application.properties";

    public static Connection getConnection() throws SQLException {
        Properties properties = loadProperties();

        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");

        return DriverManager.getConnection(url, username, password);
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();

        try (InputStream inputStream = DatabaseConnection.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {

            if (inputStream == null) {
                throw new RuntimeException("Cannot find " + CONFIG_FILE);
            }

            properties.load(inputStream);
            return properties;

        } catch (IOException e) {
            throw new RuntimeException("Failed to load database configuration", e);
        }
    }
}
