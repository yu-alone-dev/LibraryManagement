package com.librarymanagement.service;

import com.librarymanagement.dao.BookDAO;
import com.librarymanagement.exception.NotFoundException;
import com.librarymanagement.exception.ValidationException;
import com.librarymanagement.model.Book;
import com.librarymanagement.util.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class BookService {
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);
    private final BookDAO bookDAO = new BookDAO();

    public Book addBook(Book book) throws SQLException {
        validateBook(book);

        return TransactionManager.executeInTransaction(connection -> {
            // Проверка ISBN
            if (book.getIsbn() != null && !book.getIsbn().isEmpty()) {
                if (bookDAO.findByIsbn(connection, book.getIsbn()).isPresent()) {
                    // Книга существует - увеличиваем количество
                    Book existing = bookDAO.findByIsbn(connection, book.getIsbn()).get();
                    bookDAO.addCopies(connection, existing.getId(), book.getTotalCopies());
                    return bookDAO.findById(connection, existing.getId()).get();
                }
            }

            // Новая книга
            Long bookId = bookDAO.create(connection, book);
            book.setId(bookId);
            logger.info("Book added: {}", book.getTitle());
            return book;
        });
    }

    public Book getBook(Long id) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            return bookDAO.findById(connection, id)
                    .orElseThrow(() -> new NotFoundException("Book not found: " + id));
        });
    }

    public List<Book> getAllBooks(int page, int size) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            return bookDAO.findAll(connection, page, size);
        });
    }

    public List<Book> searchBooks(String query, String genre) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            return bookDAO.search(connection, query, genre);
        });
    }

    public Book updateBook(Long id, Book book) throws SQLException {
        validateBook(book);

        return TransactionManager.executeInTransaction(connection -> {
            if (bookDAO.findById(connection, id).isEmpty()) {
                throw new NotFoundException("Book not found: " + id);
            }

            book.setId(id);
            bookDAO.update(connection, book);
            logger.info("Book updated: {}", book.getTitle());
            return book;
        });
    }

    public void deleteBook(Long id) throws SQLException {
        TransactionManager.executeInTransactionVoid(connection -> {
            if (bookDAO.findById(connection, id).isEmpty()) {
                throw new NotFoundException("Book not found: " + id);
            }
            bookDAO.delete(connection, id);
            logger.info("Book deleted: {}", id);
        });
    }

    public void addCopies(Long bookId, int count) throws SQLException {
        if (count <= 0) {
            throw new ValidationException("Count must be positive");
        }

        TransactionManager.executeInTransactionVoid(connection -> {
            if (bookDAO.findById(connection, bookId).isEmpty()) {
                throw new NotFoundException("Book not found: " + bookId);
            }
            bookDAO.addCopies(connection, bookId, count);
            logger.info("Added {} copies to book {}", count, bookId);
        });
    }

    private void validateBook(Book book) {
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new ValidationException("Title is required");
        }
        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            throw new ValidationException("Author is required");
        }
        if (book.getTotalCopies() < 0) {
            throw new ValidationException("Total copies cannot be negative");
        }
    }
}