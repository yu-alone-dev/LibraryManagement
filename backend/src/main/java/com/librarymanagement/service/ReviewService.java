package com.librarymanagement.service;

import com.librarymanagement.dao.BookDAO;
import com.librarymanagement.dao.LoanDAO;
import com.librarymanagement.dao.ReviewDAO;
import com.librarymanagement.exception.BusinessException;
import com.librarymanagement.exception.NotFoundException;
import com.librarymanagement.exception.ValidationException;
import com.librarymanagement.model.Loan;
import com.librarymanagement.model.Review;
import com.librarymanagement.util.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class ReviewService {
    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final LoanDAO loanDAO = new LoanDAO();
    private final BookDAO bookDAO = new BookDAO();

    public Review addReview(Long userId, Long bookId, int rating, String comment)
            throws SQLException {
        // Валидация
        if (rating < 1 || rating > 5) {
            throw new ValidationException("Rating must be between 1 and 5");
        }

        return TransactionManager.executeInTransaction(connection -> {
            // Проверка книги
            if (bookDAO.findById(connection, bookId).isEmpty()) {
                throw new NotFoundException("Book not found: " + bookId);
            }

            // Проверка, что пользователь прочитал книгу
            boolean hasRead = loanDAO.findByUserId(connection, userId).stream()
                    .anyMatch(loan -> loan.getBookId().equals(bookId)
                            && loan.getStatus() != Loan.LoanStatus.ACTIVE);

            if (!hasRead) {
                throw new BusinessException("You must read the book before reviewing");
            }

            // Проверка на дубликат
            if (reviewDAO.findByUserAndBook(connection, userId, bookId).isPresent()) {
                throw new BusinessException("You have already reviewed this book");
            }

            Review review = new Review(userId, bookId, rating, comment);
            Long reviewId = reviewDAO.create(connection, review);
            review.setId(reviewId);

            logger.info("Review added: user={}, book={}, rating={}", userId, bookId, rating);
            return review;
        });
    }

    public List<Review> getBookReviews(Long bookId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            return reviewDAO.findByBookId(connection, bookId);
        });
    }

    public List<Review> getUnmoderatedReviews() throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            return reviewDAO.findUnmoderated(connection);
        });
    }

    public void moderateReview(Long reviewId, boolean moderated) throws SQLException {
        TransactionManager.executeInTransactionVoid(connection -> {
            if (reviewDAO.findById(connection, reviewId).isEmpty()) {
                throw new NotFoundException("Review not found: " + reviewId);
            }
            reviewDAO.moderate(connection, reviewId, moderated);
            logger.info("Review moderated: id={}, status={}", reviewId, moderated);
        });
    }

    public Double getAverageRating(Long bookId) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            return reviewDAO.getAverageRating(connection, bookId);
        });
    }
}