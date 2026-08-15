package com.librarymanagement.dao;

import com.librarymanagement.model.Review;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReviewDAO {
    private static final Logger logger = LoggerFactory.getLogger(ReviewDAO.class);

    public Optional<Review> findById(Connection connection, Long id) throws SQLException {
        String sql = "SELECT * FROM reviews WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapToReview(rs));
            }
            return Optional.empty();
        }
    }

    public List<Review> findByBookId(Connection connection, Long bookId) throws SQLException {
        String sql = "SELECT * FROM reviews WHERE book_id = ? AND is_moderated = true ORDER BY created_at DESC";
        List<Review> reviews = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, bookId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                reviews.add(mapToReview(rs));
            }
        }
        return reviews;
    }

    public List<Review> findAllByBookId(Connection connection, Long bookId) throws SQLException {
        String sql = "SELECT * FROM reviews WHERE book_id = ? ORDER BY created_at DESC";
        List<Review> reviews = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, bookId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                reviews.add(mapToReview(rs));
            }
        }
        return reviews;
    }

    public List<Review> findByUserId(Connection connection, Long userId) throws SQLException {
        String sql = "SELECT * FROM reviews WHERE user_id = ? ORDER BY created_at DESC";
        List<Review> reviews = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                reviews.add(mapToReview(rs));
            }
        }
        return reviews;
    }

    public Optional<Review> findByUserAndBook(Connection connection, Long userId, Long bookId)
            throws SQLException {
        String sql = "SELECT * FROM reviews WHERE user_id = ? AND book_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, bookId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapToReview(rs));
            }
            return Optional.empty();
        }
    }

    public List<Review> findUnmoderated(Connection connection) throws SQLException {
        String sql = "SELECT * FROM reviews WHERE is_moderated = false ORDER BY created_at";
        List<Review> reviews = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                reviews.add(mapToReview(rs));
            }
        }
        return reviews;
    }

    public Double getAverageRating(Connection connection, Long bookId) throws SQLException {
        String sql = "SELECT AVG(rating) FROM reviews WHERE book_id = ? AND is_moderated = true";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, bookId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        }
    }

    public Long create(Connection connection, Review review) throws SQLException {
        String sql = """
            INSERT INTO reviews (user_id, book_id, rating, comment)
            VALUES (?, ?, ?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, review.getUserId());
            ps.setLong(2, review.getBookId());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getComment());

            ps.executeUpdate();

            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            }
            throw new SQLException("Failed to get generated ID");
        }
    }

    public void update(Connection connection, Review review) throws SQLException {
        String sql = """
            UPDATE reviews
            SET rating = ?, comment = ?, is_moderated = ?
            WHERE id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, review.getRating());
            ps.setString(2, review.getComment());
            ps.setBoolean(3, review.isModerated());
            ps.setLong(4, review.getId());

            ps.executeUpdate();
        }
    }

    public void moderate(Connection connection, Long id, boolean moderated) throws SQLException {
        String sql = "UPDATE reviews SET is_moderated = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, moderated);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public void delete(Connection connection, Long id) throws SQLException {
        String sql = "DELETE FROM reviews WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Review mapToReview(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setId(rs.getLong("id"));
        review.setUserId(rs.getLong("user_id"));
        review.setBookId(rs.getLong("book_id"));
        review.setRating(rs.getInt("rating"));
        review.setComment(rs.getString("comment"));
        review.setModerated(rs.getBoolean("is_moderated"));
        review.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return review;
    }
}