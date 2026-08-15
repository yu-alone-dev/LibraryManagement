package com.librarymanagement.dao;

import com.librarymanagement.model.Genre;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenreDAO {
    private static final Logger logger = LoggerFactory.getLogger(GenreDAO.class);

    public Optional<Genre> findById(Connection connection, Long id) throws SQLException {
        String sql = "SELECT * FROM genres WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapToGenre(rs));
            }
            return Optional.empty();
        }
    }

    public Optional<Genre> findByName(Connection connection, String name) throws SQLException {
        String sql = "SELECT * FROM genres WHERE name = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapToGenre(rs));
            }
            return Optional.empty();
        }
    }

    public List<Genre> search(Connection connection, String keyword) throws SQLException {
        String sql = "SELECT * FROM genres WHERE name ILIKE ? OR description ILIKE ?";
        List<Genre> genres = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                genres.add(mapToGenre(rs));
            }
        }
        return genres;
    }

    public List<Genre> findAll(Connection connection) throws SQLException {
        String sql = "SELECT * FROM genres ORDER BY name";
        List<Genre> genres = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                genres.add(mapToGenre(rs));
            }
        }
        return genres;
    }

    public Long create(Connection connection, Genre genre) throws SQLException {
        String sql = """
            INSERT INTO genres (name, description)
            VALUES (?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, genre.getName());
            ps.setString(2, genre.getDescription());

            ps.executeUpdate();

            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            }
            throw new SQLException("Failed to get generated ID");
        }
    }

    public void update(Connection connection, Genre genre) throws SQLException {
        String sql = """
            UPDATE genres
            SET name = ?, description = ?
            WHERE id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, genre.getName());
            ps.setString(2, genre.getDescription());
            ps.setLong(3, genre.getId());

            ps.executeUpdate();
        }
    }

    public void delete(Connection connection, Long id) throws SQLException {
        String sql = "DELETE FROM genres WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Genre mapToGenre(ResultSet rs) throws SQLException {
        Genre genre = new Genre();
        genre.setId(rs.getLong("id"));
        genre.setName(rs.getString("name"));
        genre.setDescription(rs.getString("description"));
        return genre;
    }
}