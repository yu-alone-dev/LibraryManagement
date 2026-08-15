package com.librarymanagement.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {
    private static final Logger logger = LoggerFactory.getLogger(TransactionManager.class);

    @FunctionalInterface
    public interface TransactionCallback<T> {
        T execute(Connection connection) throws SQLException;
    }

    @FunctionalInterface
    public interface TransactionVoidCallback {
        void execute(Connection connection) throws SQLException;
    }

    public static <T> T executeInTransaction(TransactionCallback<T> callback)
            throws SQLException {
        Connection connection = null;
        try {
            connection = ConnectionPool.getInstance().getConnection();
            connection.setAutoCommit(false);

            logger.debug("Transaction started");

            T result = callback.execute(connection);

            connection.commit();
            logger.debug("Transaction committed");

            return result;

        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.debug("Transaction rolled back");
                } catch (SQLException rollbackException) {
                    logger.error("Failed to rollback transaction", rollbackException);
                }
            }
            throw new SQLException("Transaction failed: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                ConnectionPool.getInstance().releaseConnection(connection);
            }
        }
    }

    public static void executeInTransactionVoid(TransactionVoidCallback callback)
            throws SQLException {
        executeInTransaction(connection -> {
            callback.execute(connection);
            return null;
        });
    }
}
