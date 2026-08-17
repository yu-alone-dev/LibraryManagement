<script>
    import { Router, Route } from 'svelte-routing';
    import Navbar from './lib/components/Navbar.svelte';
    import BookList from './lib/components/BookList.svelte';
    import Login from './lib/components/Login.svelte';
    import Register from './lib/components/Register.svelte';
</script>

<Navbar />

<Router>
    <Route path="/" component={BookList} />
    <Route path="/login" component={Login} />
    <Route path="/register" component={Register} />
</Router>