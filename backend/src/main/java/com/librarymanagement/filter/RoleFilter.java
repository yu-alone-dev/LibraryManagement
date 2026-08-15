package com.librarymanagement.filter;

import com.google.gson.Gson;
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

@WebFilter("/api/v1/admin/*")
public class RoleFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(RoleFilter.class);
    private final Gson gson = new Gson();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Role Filter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);

        if (session == null || session.getAttribute("role") == null) {
            writeForbidden(httpResponse, "Authentication required");
            return;
        }

        String role = (String) session.getAttribute("role");

        // Проверяем роль LIBRARIAN
        if (!"LIBRARIAN".equals(role)) {
            logger.warn("Access denied for role: {} to path: {}", role, httpRequest.getRequestURI());
            writeForbidden(httpResponse, "Access denied. LIBRARIAN role required.");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.info("Role Filter destroyed");
    }

    private void writeForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> error = new HashMap<>();
        error.put("error", "Forbidden");
        error.put("message", message);

        response.getWriter().write(gson.toJson(error));
    }
}