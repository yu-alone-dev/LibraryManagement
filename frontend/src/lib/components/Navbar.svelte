<script>
    import { authStore } from '../stores/authStore';

    let isMenuOpen = false;

    function toggleMenu() {
        isMenuOpen = !isMenuOpen;
    }

    async function handleLogout() {
        await authStore.logout();
    }
</script>

<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container">
        <a class="navbar-brand" href="/">LibraryManagement</a>

        <button class="navbar-toggler" on:click={toggleMenu}>
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" class:show={isMenuOpen}>
            <ul class="navbar-nav me-auto">
                <li class="nav-item">
                    <a class="nav-link" href="/">Книги</a>
                </li>
                {#if $authStore.user}
                    <li class="nav-item">
                        <a class="nav-link" href="/loans">Мои выдачи</a>
                    </li>
                {/if}
            </ul>

            <ul class="navbar-nav">
                {#if $authStore.user}
                    <li class="nav-item">
                        <span class="navbar-text me-3">
                            {$authStore.user.username}
                        </span>
                    </li>
                    <li class="nav-item">
                        <button class="btn btn-outline-light btn-sm" on:click={handleLogout}>
                            Выйти
                        </button>
                    </li>
                {:else}
                    <li class="nav-item">
                        <a class="nav-link" href="/login">Войти</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/register">Регистрация</a>
                    </li>
                {/if}
            </ul>
        </div>
    </div>
</nav>