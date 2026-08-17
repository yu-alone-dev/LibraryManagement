import api from './api';

export const loanApi = {
    async getUserLoans(userId) {
        const response = await api.get('/loans', {
            params: { userId }
        });
        return response.data;
    },

    async issueBook(userId, bookId) {
        const response = await api.post('/loans', {
            userId, bookId
        });
        return response.data;
    },

    async returnBook(loanId) {
        const response = await api.put(`/loans/${loanId}/return`);
        return response.data;
    },

    async getOverdueLoans() {
        const response = await api.get('/loans/overdue');
        return response.data;
    }
};