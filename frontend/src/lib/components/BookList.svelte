<script>
    import { bookStore } from '../stores/bookStore';
    import { authStore } from '../stores/authStore';

    let searchQuery = '';
    let selectedGenre = '';

    $: filteredBooks = $bookStore.books;

    bookStore.loadBooks();

    async function handleSearch() {
        await bookStore.searchBooks(searchQuery, selectedGenre);
    }

    async function handleImport() {
        const genre = prompt('Введите жанр для импорта:');
        const limit = parseInt(prompt('Сколько книг импортировать?', '10'));

        if (genre && limit > 0) {
            try {
                const result = await bookStore.importBooks(genre, limit);
                alert(`Импортировано: ${result.imported} (новых: ${result.new_books}, существующих: ${result.existing_books})`);
                await bookStore.loadBooks();
            } catch (error) {
                alert('Ошибка импорта');
            }
        }
    }
</script>

<div class="container mt-4">
    <h2>Каталог книг</h2>

    <div class="row mb-3">
        <div class="col-md-4">
            <input
                type="text"
                class="form-control"
                placeholder="Поиск..."
                bind:value={searchQuery}
                on:keyup={handleSearch}
            />
        </div>
        <div class="col-md-3">
            <select class="form-control" bind:value={selectedGenre} on:change={handleSearch}>
                <option value="">Все жанры</option>
                <option value="Фантастика">Фантастика</option>
                <option value="Детектив">Детектив</option>
                <option value="Роман">Роман</option>
                <option value="Научная литература">Научная литература</option>
            </select>
        </div>
        <div class="col-md-2">
            <button class="btn btn-primary" on:click={handleSearch}>Поиск</button>
        </div>
        {#if $authStore.user?.role === 'LIBRARIAN'}
            <div class="col-md-3">
                <button class="btn btn-success" on:click={handleImport}>
                    Импорт из OpenLibrary
                </button>
            </div>
        {/if}
    </div>

    {#if $bookStore.loading}
        <div class="text-center">
            <div class="spinner-border" role="status">
                <span class="visually-hidden">Загрузка...</span>
            </div>
        </div>
    {:else if $bookStore.error}
        <div class="alert alert-danger">
            {$bookStore.error}
        </div>
    {:else}
        <div class="row">
            {#each filteredBooks as book}
                <div class="col-md-3 mb-3">
                    <div class="card h-100">
                        {#if book.coverImagePath}
                            <img src={book.coverImagePath} class="card-img-top" alt={book.title} />
                        {/if}
                        <div class="card-body">
                            <h5 class="card-title">{book.title}</h5>
                            <h6 class="card-subtitle mb-2 text-muted">{book.author}</h6>
                            <p class="card-text">
                                <span class="badge bg-primary">{book.genre}</span>
                                <br>
                                <small>Доступно: {book.availableCopies} из {book.totalCopies}</small>
                            </p>
                            {#if book.description}
                                <p class="card-text small">{book.description.substring(0, 100)}...</p>
                            {/if}
                        </div>
                    </div>
                </div>
            {/each}
        </div>

        {#if filteredBooks.length === 0}
            <div class="alert alert-info">
                Книги не найдены
            </div>
        {/if}
    {/if}
</div>