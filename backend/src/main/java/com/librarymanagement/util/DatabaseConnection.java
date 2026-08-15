package com.librarymanagement.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static Properties properties = new Properties();

    static {
        try (InputStream input = DatabaseConnection.class
                .getClassLoader()
                .getResourceAsStream("database.properties")) {

            if (input == null) {
                logger.error("database.properties not found");
                throw new RuntimeException("database.properties not found");
            }

            properties.load(input);
            Class.forName(properties.getProperty("db.driver"));
            logger.info("Database driver loaded: {}", properties.getProperty("db.driver"));

        } catch (IOException | ClassNotFoundException e) {
            logger.error("Failed to initialize database connection", e);
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.username"),
                properties.getProperty("db.password")
        );
    }
}
