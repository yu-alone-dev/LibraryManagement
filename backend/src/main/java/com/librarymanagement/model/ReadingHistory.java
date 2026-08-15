package com.librarymanagement.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReadingHistory {
    private Long id;
    private Long userId;
    private Long bookId;
    private Long loanId;
    private LocalDate readDate;
    private LocalDateTime createdAt;

    // Конструкторы
    public ReadingHistory() {}

    public ReadingHistory(Long userId, Long bookId, Long loanId) {
        this.userId = userId;
        this.bookId = bookId;
        this.loanId = loanId;
        this.readDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public Long getLoanId() { return loanId; }
    public void setLoanId(Long loanId) { this.loanId = loanId; }

    public LocalDate getReadDate() { return readDate; }
    public void setReadDate(LocalDate readDate) { this.readDate = readDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "ReadingHistory{" +
                "id=" + id +
                ", userId=" + userId +
                ", bookId=" + bookId +
                ", loanId=" + loanId +
                ", readDate=" + readDate +
                '}';
    }
}
