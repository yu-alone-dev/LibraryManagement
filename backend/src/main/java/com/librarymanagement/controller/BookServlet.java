package com.librarymanagement.controller;

import com.google.gson.Gson;
import com.librarymanagement.model.Book;
import com.librarymanagement.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet("/api/v1/books/*")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024, // 1 MB
        maxFileSize = 1024 * 1024 * 5,   // 5 MB
        maxRequestSize = 1024 * 1024 * 10 // 10 MB
)
public class BookServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(BookServlet.class);
    private final BookService bookService = new BookService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/v1/books - список книг
                int page = parseIntOrDefault(request.getParameter("page"), 1);
                int size = parseIntOrDefault(request.getParameter("size"), 10);

                List<Book> books = bookService.getAllBooks(page, size);
                writeJson(response, books);

            } else if (pathInfo.equals("/search")) {
                // GET /api/v1/books/search?query=...&genre=...
                String query = request.getParameter("query");
                String genre = request.getParameter("genre");

                List<Book> books = bookService.searchBooks(query, genre);
                writeJson(response, books);

            } else {
                // GET /api/v1/books/{id}
                Long id = Long.parseLong(pathInfo.substring(1));
                Book book = bookService.getBook(id);
                writeJson(response, book);
            }

        } catch (NumberFormatException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (Exception e) {
            logger.error("Failed to get books", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // POST /api/v1/books - добавить книгу
                Book book = readBookFromRequest(request);
                Book created = bookService.addBook(book);

                response.setStatus(HttpServletResponse.SC_CREATED);
                writeJson(response, created);

            } else if (pathInfo.endsWith("/cover")) {
                // POST /api/v1/books/{id}/cover - загрузить обложку
                Long bookId = Long.parseLong(pathInfo.split("/")[1]);
                uploadCover(request, response, bookId);

            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            logger.error("Failed to create book", e);
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        try {
            Long id = Long.parseLong(pathInfo.substring(1));
            Book book = readBookFromRequest(request);
            Book updated = bookService.updateBook(id, book);
            writeJson(response, updated);

        } catch (Exception e) {
            logger.error("Failed to update book", e);
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        try {
            Long id = Long.parseLong(pathInfo.substring(1));
            bookService.deleteBook(id);

            Map<String, String> result = new HashMap<>();
            result.put("message", "Book deleted successfully");
            writeJson(response, result);

        } catch (Exception e) {
            logger.error("Failed to delete book", e);
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private Book readBookFromRequest(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return gson.fromJson(sb.toString(), Book.class);
    }

    private void uploadCover(HttpServletRequest request, HttpServletResponse response, Long bookId)
            throws ServletException, IOException {
        Part filePart = request.getPart("cover");
        if (filePart == null) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "No file uploaded");
            return;
        }

        String fileName = UUID.randomUUID().toString() + "_" +
                Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

        String uploadDir = getServletContext().getRealPath("/uploads");
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (InputStream input = filePart.getInputStream()) {
            Files.copy(input, uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        }

        String coverPath = "/uploads/" + fileName;

        Map<String, String> result = new HashMap<>();
        result.put("coverPath", coverPath);
        writeJson(response, result);
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void writeJson(HttpServletResponse response, Object data) throws IOException {
        response.getWriter().write(gson.toJson(data));
    }

    private void writeError(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        writeJson(response, error);
    }
}