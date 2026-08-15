package com.librarymanagement.controller;

import com.google.gson.Gson;
import com.librarymanagement.model.User;
import com.librarymanagement.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@WebServlet("/api/v1/auth/*")
public class AuthServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AuthServlet.class);
    private final AuthService authService = new AuthService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        if ("/register".equals(pathInfo)) {
            register(request, response);
        } else if ("/login".equals(pathInfo)) {
            login(request, response);
        } else if ("/logout".equals(pathInfo)) {
            logout(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void register(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Map<String, String> body = readJsonBody(request);

            User user = authService.register(
                    body.get("username"),
                    body.get("email"),
                    body.get("password"),
                    body.get("fullName")
            );

            // Не возвращаем пароль
            user.setPasswordHash(null);

            response.setStatus(HttpServletResponse.SC_CREATED);
            writeJson(response, user);

        } catch (Exception e) {
            logger.error("Registration failed", e);
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private void login(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Map<String, String> body = readJsonBody(request);

            Optional<User> userOpt = authService.login(
                    body.get("username"),
                    body.get("password")
            );

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // Создание сессии
                HttpSession session = request.getSession(true);
                session.setAttribute("userId", user.getId());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("role", user.getRole().name());
                session.setMaxInactiveInterval(30 * 60); // 30 минут

                user.setPasswordHash(null);
                writeJson(response, user);
            } else {
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials");
            }

        } catch (Exception e) {
            logger.error("Login failed", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void logout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        response.setContentType("application/json");
        Map<String, String> result = new HashMap<>();
        result.put("message", "Logged out successfully");
        writeJson(response, result);
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