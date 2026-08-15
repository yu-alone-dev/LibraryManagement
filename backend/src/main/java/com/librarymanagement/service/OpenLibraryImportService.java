package com.librarymanagement.service;

import com.librarymanagement.dao.BookDAO;
import com.librarymanagement.integration.BookMetadata;
import com.librarymanagement.integration.OpenLibraryClient;
import com.librarymanagement.model.Book;
import com.librarymanagement.util.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OpenLibraryImportService {
    private static final Logger logger = LoggerFactory.getLogger(OpenLibraryImportService.class);

    private final OpenLibraryClient openLibraryClient;
    private final BookDAO bookDAO;

    public OpenLibraryImportService() {
        this.openLibraryClient = new OpenLibraryClient();
        this.bookDAO = new BookDAO();
    }

    /**
     * Импорт случайных книг по жанру
     */
    public Map<String, Integer> importRandomBooks(String genre, int limit)
            throws IOException, SQLException {

        logger.info("Importing {} random books of genre: {}", limit, genre);

        List<BookMetadata> metadataList = openLibraryClient.importRandomByGenre(genre, limit);

        int newBooks = 0;
        int existingBooks = 0;

        for (BookMetadata metadata : metadataList) {
            boolean isNew = importBook(metadata);
            if (isNew) {
                newBooks++;
            } else {
                existingBooks++;
            }
        }

        Map<String, Integer> result = new HashMap<>();
        result.put("requested", limit);
        result.put("imported", metadataList.size());
        result.put("new_books", newBooks);
        result.put("existing_books", existingBooks);

        logger.info("Import completed: {} new, {} existing", newBooks, existingBooks);
        return result;
    }

    /**
     * Импорт книги по ISBN
     */
    public Book importByIsbn(String isbn) throws IOException, SQLException {
        logger.info("Importing book by ISBN: {}", isbn);

        BookMetadata metadata = openLibraryClient.findByIsbn(isbn);
        importBook(metadata);

        return TransactionManager.executeInTransaction(connection -> {
            return bookDAO.findByIsbn(connection, isbn).orElse(null);
        });
    }

    /**
     * Импорт одной книги (создание или увеличение копий)
     */
    private boolean importBook(BookMetadata metadata) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            Optional<Book> existingBook = bookDAO.findByIsbn(connection, metadata.getIsbn());

            if (existingBook.isPresent()) {
                // Книга существует - увеличиваем количество копий
                bookDAO.addCopies(connection, existingBook.get().getId(), 1);
                logger.debug("Increased copies for: {}", metadata.getTitle());
                return false;
            } else {
                // Новая книга
                Book book = new Book();
                book.setTitle(metadata.getTitle());
                book.setAuthor(metadata.getAuthor());
                book.setIsbn(metadata.getIsbn());
                book.setGenre(metadata.getGenre());
                book.setDescription(metadata.getDescription());
                book.setCoverImagePath(metadata.getCoverUrl());
                book.setTotalCopies(1);
                book.setAvailableCopies(1);

                bookDAO.create(connection, book);
                logger.debug("Created new book: {}", metadata.getTitle());
                return true;
            }
        });
    }
}