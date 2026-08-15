package com.librarymanagement.service;

import com.librarymanagement.dao.UserDAO;
import com.librarymanagement.exception.BusinessException;
import com.librarymanagement.exception.ValidationException;
import com.librarymanagement.model.User;
import com.librarymanagement.util.TransactionManager;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserDAO userDAO = new UserDAO();

    public User register(String username, String email, String password, String fullName)
            throws SQLException {
        // Валидация
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username is required");
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("Invalid email format");
        }
        if (password == null || password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters");
        }

        return TransactionManager.executeInTransaction(connection -> {
            // Проверка уникальности
            if (userDAO.findByUsername(connection, username).isPresent()) {
                throw new BusinessException("Username already exists");
            }
            if (userDAO.findByEmail(connection, email).isPresent()) {
                throw new BusinessException("Email already exists");
            }

            // Создание пользователя
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
            user.setFullName(fullName);
            user.setRole(User.Role.READER); // По умолчанию READER

            Long userId = userDAO.create(connection, user);
            user.setId(userId);

            logger.info("User registered: {}", username);
            return user;
        });
    }

    public Optional<User> login(String username, String password) throws SQLException {
        return TransactionManager.executeInTransaction(connection -> {
            Optional<User> userOpt = userDAO.findByUsername(connection, username);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (BCrypt.checkpw(password, user.getPasswordHash())) {
                    logger.info("User logged in: {}", username);
                    return Optional.of(user);
                }
            }

            logger.warn("Failed login attempt for user: {}", username);
            return Optional.empty();
        });
    }

    public User createLibrarian(String username, String email, String password, String fullName)
            throws SQLException {
        User user = register(username, email, password, fullName);
        user.setRole(User.Role.LIBRARIAN);

        return TransactionManager.executeInTransaction(connection -> {
            userDAO.update(connection, user);
            return user;
        });
    }
}