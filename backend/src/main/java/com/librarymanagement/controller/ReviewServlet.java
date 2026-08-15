package com.librarymanagement.controller;

import com.google.gson.Gson;
import com.librarymanagement.model.Review;
import com.librarymanagement.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/v1/reviews/*")
public class ReviewServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ReviewServlet.class);
    private final ReviewService reviewService = new ReviewService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/v1/reviews?bookId=...
                String bookIdParam = request.getParameter("bookId");
                if (bookIdParam != null) {
                    Long bookId = Long.parseLong(bookIdParam);
                    List<Review> reviews = reviewService.getBookReviews(bookId);
                    writeJson(response, reviews);
                } else {
                    writeError(response, HttpServletResponse.SC_BAD_REQUEST, "bookId is required");
                }

            } else if (pathInfo.equals("/unmoderated")) {
                // GET /api/v1/reviews/unmoderated
                List<Review> reviews = reviewService.getUnmoderatedReviews();
                writeJson(response, reviews);

            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            logger.error("Failed to get reviews", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Map<String, String> body = readJsonBody(request);

            Long userId = Long.parseLong(body.get("userId"));
            Long bookId = Long.parseLong(body.get("bookId"));
            int rating = Integer.parseInt(body.get("rating"));
            String comment = body.get("comment");

            Review review = reviewService.addReview(userId, bookId, rating, comment);

            response.setStatus(HttpServletResponse.SC_CREATED);
            writeJson(response, review);

        } catch (Exception e) {
            logger.error("Failed to add review", e);
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
            if (pathInfo != null && pathInfo.endsWith("/moderate")) {
                // PUT /api/v1/reviews/{id}/moderate
                Long reviewId = Long.parseLong(pathInfo.split("/")[1]);
                Map<String, String> body = readJsonBody(request);
                boolean moderated = Boolean.parseBoolean(body.get("moderated"));

                reviewService.moderateReview(reviewId, moderated);

                Map<String, String> result = new HashMap<>();
                result.put("message", "Review moderated successfully");
                writeJson(response, result);

            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (Exception e) {
            logger.error("Failed to moderate review", e);
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
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