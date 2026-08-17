<script>
    import { authStore } from '../stores/authStore';

    let username = '';
    let email = '';
    let password = '';
    let fullName = '';
    let error = '';
    let loading = false;

    async function handleSubmit() {
        loading = true;
        error = '';

        try {
            await authStore.register(username, email, password, fullName);
            alert('Регистрация успешна! Теперь войдите в систему.');
            username = '';
            email = '';
            password = '';
            fullName = '';
        } catch (err) {
            error = err.response?.data?.error || 'Ошибка регистрации';
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
                    <h4>Регистрация</h4>
                </div>
                <div class="card-body">
                    {#if error}
                        <div class="alert alert-danger">{error}</div>
                    {/if}

                    <form on:submit|preventDefault={handleSubmit}>
                        <div class="mb-3">
                            <label class="form-label">Имя пользователя</label>
                            <input type="text" class="form-control" bind:value={username} required />
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Email</label>
                            <input type="email" class="form-control" bind:value={email} required />
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Полное имя</label>
                            <input type="text" class="form-control" bind:value={fullName} />
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Пароль</label>
                            <input type="password" class="form-control" bind:value={password} required minlength="6" />
                        </div>

                        <button type="submit" class="btn btn-primary" disabled={loading}>
                            {loading ? 'Регистрация...' : 'Зарегистрироваться'}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>