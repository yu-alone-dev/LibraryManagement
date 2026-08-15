package com.librarymanagement.dao;

import com.librarymanagement.model.Loan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoanDAO {
    private static final Logger logger = LoggerFactory.getLogger(LoanDAO.class);

    public Optional<Loan> findById(Connection connection, Long id) throws SQLException {
        String sql = "SELECT * FROM loans WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapToLoan(rs));
            }
            return Optional.empty();
        }
    }

    public List<Loan> findByUserId(Connection connection, Long userId)
            throws SQLException {
        String sql = "SELECT * FROM loans WHERE user_id = ? ORDER BY loan_date DESC";
        List<Loan> loans = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                loans.add(mapToLoan(rs));
            }
        }
        return loans;
    }

    public List<Loan> findActiveByUserId(Connection connection, Long userId)
            throws SQLException {
        String sql = "SELECT * FROM loans WHERE user_id = ? AND status = 'ACTIVE'";
        List<Loan> loans = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                loans.add(mapToLoan(rs));
            }
        }
        return loans;
    }

    public int countActiveByUser(Connection connection, Long userId)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM loans WHERE user_id = ? AND status = 'ACTIVE'";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    public boolean hasActiveLoan(Connection connection, Long userId, Long bookId)
            throws SQLException {
        String sql = "SELECT 1 FROM loans WHERE user_id = ? AND book_id = ? AND status = 'ACTIVE'";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, bookId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    public List<Loan> findOverdue(Connection connection) throws SQLException {
        String sql = """
            SELECT * FROM loans 
            WHERE status = 'ACTIVE' AND due_date < CURRENT_DATE
            ORDER BY due_date
        """;
        List<Loan> loans = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                loans.add(mapToLoan(rs));
            }
        }
        return loans;
    }

    public Long create(Connection connection, Loan loan) throws SQLException {
        String sql = """
            INSERT INTO loans (user_id, book_id, loan_date, due_date, status)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, loan.getUserId());
            ps.setLong(2, loan.getBookId());
            ps.setDate(3, Date.valueOf(loan.getLoanDate()));
            ps.setDate(4, Date.valueOf(loan.getDueDate()));
            ps.setString(5, loan.getStatus().name());

            ps.executeUpdate();

            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            }
            throw new SQLException("Failed to get generated ID");
        }
    }

    public void updateStatus(Connection connection, Long id, Loan.LoanStatus status)
            throws SQLException {
        String sql = "UPDATE loans SET status = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public void returnBook(Connection connection, Long id) throws SQLException {
        String sql = """
            UPDATE loans 
            SET return_date = ?, status = ?
            WHERE id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(LocalDate.now()));
            ps.setString(2, "RETURNED");
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    private Loan mapToLoan(ResultSet rs) throws SQLException {
        Loan loan = new Loan();
        loan.setId(rs.getLong("id"));
        loan.setUserId(rs.getLong("user_id"));
        loan.setBookId(rs.getLong("book_id"));
        loan.setLoanDate(rs.getDate("loan_date").toLocalDate());
        loan.setDueDate(rs.getDate("due_date").toLocalDate());

        Date returnDate = rs.getDate("return_date");
        if (returnDate != null) {
            loan.setReturnDate(returnDate.toLocalDate());
        }

        loan.setStatus(Loan.LoanStatus.valueOf(rs.getString("status")));
        loan.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return loan;
    }
}
