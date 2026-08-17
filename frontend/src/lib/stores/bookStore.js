import { writable } from 'svelte/store';
import { bookApi } from '../api/books';

function createBookStore() {
    const { subscribe, set, update } = writable({
        books: [],
        loading: false,
        error: null,
        page: 1,
        size: 10,
        totalPages: 1
    });

    return {
        subscribe,

        loadBooks: async (page = 1, size = 10) => {
            update(state => ({ ...state, loading: true, error: null }));
            try {
                const books = await bookApi.getAllBooks(page, size);
                update(state => ({
                    ...state,
                    books,
                    loading: false,
                    page,
                    size
                }));
            } catch (error) {
                update(state => ({ ...state, loading: false, error: error.response?.data?.error || 'Failed to load books' }));
            }
        },

        searchBooks: async (query, genre) => {
            update(state => ({ ...state, loading: true, error: null }));
            try {
                const books = await bookApi.searchBooks(query, genre);
                update(state => ({ ...state, books, loading: false }));
            } catch (error) {
                update(state => ({ ...state, loading: false, error: error.response?.data?.error || 'Search failed' }));
            }
        },

        addBook: async (book) => {
            update(state => ({ ...state, loading: true, error: null }));
            try {
                const newBook = await bookApi.addBook(book);
                update(state => ({
                    ...state,
                    books: [...state.books, newBook],
                    loading: false
                }));
                return newBook;
            } catch (error) {
                update(state => ({ ...state, loading: false, error: error.response?.data?.error || 'Failed to add book' }));
                throw error;
            }
        },

        importBooks: async (genre, limit) => {
            update(state => ({ ...state, loading: true, error: null }));
            try {
                const result = await bookApi.importBooks(genre, limit);
                update(state => ({ ...state, loading: false }));
                return result;
            } catch (error) {
                update(state => ({ ...state, loading: false, error: error.response?.data?.error || 'Import failed' }));
                throw error;
            }
        }
    };
}

export const bookStore = createBookStore();