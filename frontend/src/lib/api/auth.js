import api from './api';

export const authApi = {
    async register(username, email, password, fullName) {
        const response = await api.post('/auth/register', {
            username, email, password, fullName
        });
        return response.data;
    },

    async login(username, password) {
        const response = await api.post('/auth/login', {
            username, password
        });
        return response.data;
    },

    async logout() {
        const response = await api.post('/auth/logout');
        return response.data;
    }
};