package com.librarymanagement.model;

import java.time.LocalDateTime;

public class Book {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String genre;
    private String description;
    private String coverImagePath;
    private int totalCopies;
    private int availableCopies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Конструкторы
    public Book() {}

    public Book(Long id, String title, String author, String isbn) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.totalCopies = 1;
        this.availableCopies = 1;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverImagePath() { return coverImagePath; }
    public void setCoverImagePath(String coverImagePath) { this.coverImagePath = coverImagePath; }

    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }

    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Бизнес-методы
    public void decreaseAvailableCopies() {
        if (availableCopies > 0) {
            availableCopies--;
        } else {
            throw new IllegalStateException("No available copies");
        }
    }

    public void increaseAvailableCopies() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
    }

    public void addCopies(int count) {
        this.totalCopies += count;
        this.availableCopies += count;
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", isbn='" + isbn + '\'' +
                ", availableCopies=" + availableCopies +
                '}';
    }
}