import api from './api';

export const reviewApi = {
    async getBookReviews(bookId) {
        const response = await api.get('/reviews', {
            params: { bookId }
        });
        return response.data;
    },

    async addReview(userId, bookId, rating, comment) {
        const response = await api.post('/reviews', {
            userId, bookId, rating, comment
        });
        return response.data;
    },

    async moderateReview(reviewId, moderated) {
        const response = await api.put(`/reviews/${reviewId}/moderate`, {
            moderated
        });
        return response.data;
    }
};