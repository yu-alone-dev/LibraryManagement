<script>
    import { authStore } from '../stores/authStore';
    import { goto } from 'svelte-routing';

    let username = '';
    let password = '';
    let error = '';
    let loading = false;

    async function handleSubmit() {
        loading = true;
        error = '';

        try {
            await authStore.login(username, password);
            goto('/');
        } catch (err) {
            error = err.response?.data?.error || 'Ошибка входа';
        } finally {
            loading = false;
        }
    }
</script>

<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card">
                <div class="card-header">
                    <h4>Вход в систему</h4>
                </div>
                <div class="card-body">
                    {#if error}
                        <div class="alert alert-danger">{error}</div>
                    {/if}

                    <form on:submit|preventDefault={handleSubmit}>
                        <div class="mb-3">
                            <label class="form-label">Имя пользователя</label>
                            <input
                                type="text"
                                class="form-control"
                                bind:value={username}
                                required
                            />
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Пароль</label>
                            <input
                                type="password"
                                class="form-control"
                                bind:value={password}
                                required
                            />
                        </div>

                        <button type="submit" class="btn btn-primary" disabled={loading}>
                            {loading ? 'Вход...' : 'Войти'}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>