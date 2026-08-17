import api from './api';

export const bookApi = {
    async getAllBooks(page = 1, size = 10) {
        const response = await api.get('/books', {
            params: { page, size }
        });
        return response.data;
    },

    async getBook(id) {
        const response = await api.get(`/books/${id}`);
        return response.data;
    },

    async searchBooks(query, genre) {
        const response = await api.get('/books/search', {
            params: { query, genre }
        });
        return response.data;
    },

    async addBook(book) {
        const response = await api.post('/books', book);
        return response.data;
    },

    async updateBook(id, book) {
        const response = await api.put(`/books/${id}`, book);
        return response.data;
    },

    async deleteBook(id) {
        const response = await api.delete(`/books/${id}`);
        return response.data;
    },

    async importBooks(genre, limit) {
        const response = await api.post('/books/import-openlibrary', {
            genre, limit
        });
        return response.data;
    }
};