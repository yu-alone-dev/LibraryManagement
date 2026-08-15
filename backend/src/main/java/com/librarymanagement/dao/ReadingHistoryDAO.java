package com.librarymanagement.dao;

import com.librarymanagement.model.ReadingHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReadingHistoryDAO {
    private static final Logger logger = LoggerFactory.getLogger(ReadingHistoryDAO.class);

    public Optional<ReadingHistory> findById(Connection connection, Long id) throws SQLException {
        String sql = "SELECT * FROM reading_history WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapToReadingHistory(rs));
            }
            return Optional.empty();
        }
    }

    public List<ReadingHistory> findByUserId(Connection connection, Long userId)
            throws SQLException {
        String sql = "SELECT * FROM reading_history WHERE user_id = ? ORDER BY read_date DESC";
        List<ReadingHistory> history = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                history.add(mapToReadingHistory(rs));
            }
        }
        return history;
    }

    public List<ReadingHistory> findByBookId(Connection connection, Long bookId)
            throws SQLException {
        String sql = "SELECT * FROM reading_history WHERE book_id = ? ORDER BY read_date DESC";
        List<ReadingHistory> history = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, bookId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                history.add(mapToReadingHistory(rs));
            }
        }
        return history;
    }

    public Optional<ReadingHistory> findByLoanId(Connection connection, Long loanId)
            throws SQLException {
        String sql = "SELECT * FROM reading_history WHERE loan_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, loanId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapToReadingHistory(rs));
            }
            return Optional.empty();
        }
    }

    public List<ReadingHistory> findByUserAndBook(Connection connection, Long userId, Long bookId)
            throws SQLException {
        String sql = "SELECT * FROM reading_history WHERE user_id = ? AND book_id = ? ORDER BY read_date DESC";
        List<ReadingHistory> history = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, bookId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                history.add(mapToReadingHistory(rs));
            }
        }
        return history;
    }

    public Long create(Connection connection, ReadingHistory history) throws SQLException {
        String sql = """
            INSERT INTO reading_history (user_id, book_id, loan_id, read_date)
            VALUES (?, ?, ?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, history.getUserId());
            ps.setLong(2, history.getBookId());
            ps.setLong(3, history.getLoanId());
            ps.setDate(4, Date.valueOf(history.getReadDate()));

            ps.executeUpdate();

            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            }
            throw new SQLException("Failed to get generated ID");
        }
    }

    public void delete(Connection connection, Long id) throws SQLException {
        String sql = "DELETE FROM reading_history WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private ReadingHistory mapToReadingHistory(ResultSet rs) throws SQLException {
        ReadingHistory history = new ReadingHistory();
        history.setId(rs.getLong("id"));
        history.setUserId(rs.getLong("user_id"));
        history.setBookId(rs.getLong("book_id"));
        history.setLoanId(rs.getLong("loan_id"));
        history.setReadDate(rs.getDate("read_date").toLocalDate());
        history.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return history;
    }
}