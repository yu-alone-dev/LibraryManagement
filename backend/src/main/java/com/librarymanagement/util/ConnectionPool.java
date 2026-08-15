package com.librarymanagement.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConnectionPool {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);

    private static ConnectionPool instance;
    private final BlockingQueue<Connection> availableConnections;
    private final int poolSize;

    private ConnectionPool() {
        this.poolSize = 10;
        this.availableConnections = new LinkedBlockingQueue<>(poolSize);

        for (int i = 0; i < poolSize; i++) {
            try {
                availableConnections.add(DatabaseConnection.getConnection());
            } catch (SQLException e) {
                logger.error("Failed to create connection", e);
            }
        }

        logger.info("Connection pool initialized with {} connections", poolSize);
    }

    public static synchronized ConnectionPool getInstance() {
        if (instance == null) {
            instance = new ConnectionPool();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        try {
            Connection connection = availableConnections.poll(5, TimeUnit.SECONDS);
            if (connection == null) {
                throw new SQLException("Connection pool exhausted");
            }
            return connection;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for connection", e);
        }
    }

    public void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    availableConnections.offer(connection);
                }
            } catch (SQLException e) {
                logger.error("Failed to release connection", e);
            }
        }
    }

    public void closeAll() {
        for (Connection connection : availableConnections) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("Failed to close connection", e);
            }
        }
        availableConnections.clear();
        logger.info("Connection pool closed");
    }

    public int getAvailableCount() {
        return availableConnections.size();
    }
}