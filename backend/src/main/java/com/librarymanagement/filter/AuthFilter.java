package com.librarymanagement.filter;

import com.google.gson.Gson;
import com.librarymanagement.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebFilter("/api/v1/*")
public class AuthFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);
    private final Gson gson = new Gson();

    // Публичные эндпоинты (без аутентификации)
    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/register",
            "/api/v1/auth/login"
    };

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Auth Filter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        // Пропускаем OPTIONS запросы
        if ("OPTIONS".equalsIgnoreCase(method)) {
            chain.doFilter(request, response);
            return;
        }

        // Проверяем публичные пути
        if (isPublicPath(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // Проверяем сессию
        HttpSession session = httpRequest.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            logger.warn("Unauthorized access attempt to: {}", requestURI);
            writeUnauthorized(httpResponse);
            return;
        }

        // Логируем пользователя
        Long userId = (Long) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");
        logger.debug("Authenticated request: user={}, path={}", username, requestURI);

        // Добавляем атрибуты в request
        httpRequest.setAttribute("userId", userId);
        httpRequest.setAttribute("username", username);
        httpRequest.setAttribute("role", session.getAttribute("role"));

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.info("Auth Filter destroyed");
    }

    private boolean isPublicPath(String requestURI) {
        for (String publicPath : PUBLIC_PATHS) {
            if (requestURI.equals(publicPath)) {
                return true;
            }
        }
        return false;
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> error = new HashMap<>();
        error.put("error", "Unauthorized");
        error.put("message", "Authentication required");

        response.getWriter().write(gson.toJson(error));
    }
}