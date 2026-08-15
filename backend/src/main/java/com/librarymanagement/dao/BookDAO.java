package com.librarymanagement.dao;

import com.librarymanagement.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookDAO {
    private static final Logger logger = LoggerFactory.getLogger(BookDAO.class);

    public Optional<Book> findById(Connection connection, Long id) throws SQLException {
        String sql = "SELECT * FROM books WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapToBook(rs));
            }
            return Optional.empty();
        }
    }

    public Optional<Book> findByIdForUpdate(Connection connection, Long id)
            throws SQLException {
        String sql = "SELECT * FROM books WHERE id = ? FOR UPDATE";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapToBook(rs));
            }
            return Optional.empty();
        }
    }

    public Optional<Book> findByIsbn(Connection connection, String isbn)
            throws SQLException {
        String sql = "SELECT * FROM books WHERE isbn = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, isbn);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapToBook(rs));
            }
            return Optional.empty();
        }
    }

    public List<Book> findAll(Connection connection, int page, int size)
            throws SQLException {
        String sql = "SELECT * FROM books ORDER BY id LIMIT ? OFFSET ?";
        List<Book> books = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, size);
            ps.setInt(2, (page - 1) * size);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                books.add(mapToBook(rs));
            }
        }
        return books;
    }

    public List<Book> search(Connection connection, String query, String genre)
            throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM books WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.isEmpty()) {
            sql.append(" AND (title ILIKE ? OR author ILIKE ? OR isbn ILIKE ?)");
            String searchPattern = "%" + query + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }

        if (genre != null && !genre.isEmpty()) {
            sql.append(" AND genre = ?");
            params.add(genre);
        }

        sql.append(" ORDER BY title");

        List<Book> books = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                books.add(mapToBook(rs));
            }
        }
        return books;
    }

    public Long create(Connection connection, Book book) throws SQLException {
        String sql = """
            INSERT INTO books (title, author, isbn, genre, description, 
                              cover_image_path, total_copies, available_copies)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getIsbn());
            ps.setString(4, book.getGenre());
            ps.setString(5, book.getDescription());
            ps.setString(6, book.getCoverImagePath());
            ps.setInt(7, book.getTotalCopies());
            ps.setInt(8, book.getAvailableCopies());

            ps.executeUpdate();

            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            }
            throw new SQLException("Failed to get generated ID");
        }
    }

    public void update(Connection connection, Book book) throws SQLException {
        String sql = """
            UPDATE books 
            SET title = ?, author = ?, isbn = ?, genre = ?, description = ?,
                cover_image_path = ?, total_copies = ?, available_copies = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getIsbn());
            ps.setString(4, book.getGenre());
            ps.setString(5, book.getDescription());
            ps.setString(6, book.getCoverImagePath());
            ps.setInt(7, book.getTotalCopies());
            ps.setInt(8, book.getAvailableCopies());
            ps.setLong(9, book.getId());

            ps.executeUpdate();
        }
    }

    public void decreaseAvailableCopies(Connection connection, Long id)
            throws SQLException {
        String sql = """
            UPDATE books 
            SET available_copies = available_copies - 1,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND available_copies > 0
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new SQLException("No available copies");
            }
        }
    }

    public void increaseAvailableCopies(Connection connection, Long id)
            throws SQLException {
        String sql = """
            UPDATE books 
            SET available_copies = available_copies + 1,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public void addCopies(Connection connection, Long id, int count)
            throws SQLException {
        String sql = """
            UPDATE books 
            SET total_copies = total_copies + ?,
                available_copies = available_copies + ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, count);
            ps.setInt(2, count);
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    public void delete(Connection connection, Long id) throws SQLException {
        String sql = "DELETE FROM books WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Book mapToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getLong("id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setIsbn(rs.getString("isbn"));
        book.setGenre(rs.getString("genre"));
        book.setDescription(rs.getString("description"));
        book.setCoverImagePath(rs.getString("cover_image_path"));
        book.setTotalCopies(rs.getInt("total_copies"));
        book.setAvailableCopies(rs.getInt("available_copies"));
        book.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        book.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return book;
    }
}
