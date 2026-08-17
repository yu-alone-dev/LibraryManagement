import { writable } from 'svelte/store';
import { authApi } from '../api/auth';

function createAuthStore() {
    const { subscribe, set, update } = writable({
        user: null,
        loading: false,
        error: null
    });

    return {
        subscribe,

        register: async (username, email, password, fullName) => {
            update(state => ({ ...state, loading: true, error: null }));
            try {
                const user = await authApi.register(username, email, password, fullName);
                set({ user, loading: false, error: null });
                return user;
            } catch (error) {
                update(state => ({ ...state, loading: false, error: error.response?.data?.error || 'Registration failed' }));
                throw error;
            }
        },

        login: async (username, password) => {
            update(state => ({ ...state, loading: true, error: null }));
            try {
                const user = await authApi.login(username, password);
                set({ user, loading: false, error: null });
                return user;
            } catch (error) {
                update(state => ({ ...state, loading: false, error: error.response?.data?.error || 'Login failed' }));
                throw error;
            }
        },

        logout: async () => {
            try {
                await authApi.logout();
            } finally {
                set({ user: null, loading: false, error: null });
            }
        }
    };
}

export const authStore = createAuthStore();