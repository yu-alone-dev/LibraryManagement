package com.librarymanagement.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Loan {
    private Long id;
    private Long userId;
    private Long bookId;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private LoanStatus status;
    private LocalDateTime createdAt;

    public enum LoanStatus {
        ACTIVE, RETURNED, OVERDUE
    }

    // Конструкторы
    public Loan() {}

    public Loan(Long userId, Long bookId) {
        this.userId = userId;
        this.bookId = bookId;
        this.loanDate = LocalDate.now();
        this.dueDate = loanDate.plusDays(14);
        this.status = LoanStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public LocalDate getLoanDate() { return loanDate; }
    public void setLoanDate(LocalDate loanDate) { this.loanDate = loanDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Бизнес-методы
    public boolean isOverdue() {
        return status == LoanStatus.ACTIVE && LocalDate.now().isAfter(dueDate);
    }

    public void returnBook() {
        this.returnDate = LocalDate.now();
        this.status = isOverdue() ? LoanStatus.OVERDUE : LoanStatus.RETURNED;
    }

    @Override
    public String toString() {
        return "Loan{" +
                "id=" + id +
                ", userId=" + userId +
                ", bookId=" + bookId +
                ", status=" + status +
                '}';
    }
}