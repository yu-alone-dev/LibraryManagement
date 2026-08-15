package com.librarymanagement.service;

import com.librarymanagement.dao.BookDAO;
import com.librarymanagement.dao.LoanDAO;
import com.librarymanagement.dao.ReadingHistoryDAO;
import com.librarymanagement.dao.UserDAO;
import com.librarymanagement.exception.BusinessException;
import com.librarymanagement.exception.NotFoundException;
import com.librarymanagement.model.Book;
import com.librarymanagement.model.Loan;
import com.librarymanagement.model.ReadingHistory;
import com.librarymanagement.model.User;
import com.librarymanagement.util.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class LoanService {
    private static final Logger logger = LoggerFactory.getLogger(LoanService.class);
    private final LoanDAO loanDAO = new LoanDAO();
    private final BookDAO bookDAO = new BookDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ReadingHistoryDAO readingHistoryDAO = new ReadingHistoryDAO();

    public Loan issueBook(Long userId, Long bookId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            // Проверка пользователя
            User user = userDAO.findById(connection, userId)
                    .orElseThrow(() -> new NotFoundException("User not found: " + userId));

            if (!user.isActive()) {
                throw new BusinessException("User is not active");
            }

            // Проверка лимита книг
            int activeLoans = loanDAO.countActiveByUser(connection, userId);
            if (activeLoans >= 5) {
                throw new BusinessException("User has reached maximum loan limit (5)");
            }

            // Блокировка книги
            Book book = bookDAO.findByIdForUpdate(connection, bookId)
                    .orElseThrow(() -> new NotFoundException("Book not found: " + bookId));

            if (!book.isAvailable()) {
                throw new BusinessException("Book is not available");
            }

            // Проверка на дубликат
            if (loanDAO.hasActiveLoan(connection, userId, bookId)) {
                throw new BusinessException("User already has this book");
            }

            // Создание выдачи
            Loan loan = new Loan(userId, bookId);
            Long loanId = loanDAO.create(connection, loan);
            loan.setId(loanId);

            // Уменьшение доступных копий
            bookDAO.decreaseAvailableCopies(connection, bookId);

            logger.info("Book issued: user={}, book={}", userId, bookId);
            return loan;
        });
    }

    public void returnBook(Long loanId) throws SQLException {
        TransactionManager.executeInTransactionVoid(connection -> {
            Loan loan = loanDAO.findById(connection, loanId)
                    .orElseThrow(() -> new NotFoundException("Loan not found: " + loanId));

            if (loan.getStatus() != Loan.LoanStatus.ACTIVE) {
                throw new BusinessException("Loan is not active");
            }

            // Возврат книги
            loanDAO.returnBook(connection, loanId);
            bookDAO.increaseAvailableCopies(connection, loan.getBookId());

            // Добавление в историю чтения
            ReadingHistory history = new ReadingHistory(
                    loan.getUserId(), loan.getBookId(), loanId);
            readingHistoryDAO.create(connection, history);

            logger.info("Book returned: loan={}", loanId);
        });
    }

    public List<Loan> getUserLoans(Long userId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            return loanDAO.findByUserId(connection, userId);
        });
    }

    public List<Loan> getActiveLoans(Long userId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            return loanDAO.findActiveByUserId(connection, userId);
        });
    }

    public List<Loan> getOverdueLoans() throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            return loanDAO.findOverdue(connection);
        });
    }
}