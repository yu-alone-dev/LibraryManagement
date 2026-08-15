package com.librarymanagement.controller;

import com.google.gson.Gson;
import com.librarymanagement.service.OpenLibraryImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/v1/books/import-openlibrary")
public class OpenLibraryImportServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(OpenLibraryImportServlet.class);
    private final OpenLibraryImportService importService = new OpenLibraryImportService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Map<String, String> body = readJsonBody(request);

            String genre = body.get("genre");
            int limit = parseIntOrDefault(body.get("limit"), 10);

            if (genre == null || genre.isEmpty()) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Genre is required");
                return;
            }

            Map<String, Integer> result = importService.importRandomBooks(genre, limit);
            writeJson(response, result);

        } catch (Exception e) {
            logger.error("Failed to import books", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private Map<String, String> readJsonBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return gson.fromJson(sb.toString(), Map.class);
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