package com.librarymanagement.model;

import java.time.LocalDateTime;

public class Review {
    private Long id;
    private Long userId;
    private Long bookId;
    private int rating;
    private String comment;
    private boolean isModerated;
    private LocalDateTime createdAt;

    // Конструкторы
    public Review() {}

    public Review(Long userId, Long bookId, int rating, String comment) {
        this.userId = userId;
        this.bookId = bookId;
        setRating(rating);
        this.comment = comment;
        this.isModerated = false;
        this.createdAt = LocalDateTime.now();
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public int getRating() { return rating; }
    public void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        this.rating = rating;
    }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public boolean isModerated() { return isModerated; }
    public void setModerated(boolean moderated) { isModerated = moderated; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", userId=" + userId +
                ", bookId=" + bookId +
                ", rating=" + rating +
                ", isModerated=" + isModerated +
                '}';
    }
}
