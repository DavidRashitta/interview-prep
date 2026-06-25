<script setup>
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div class="layout">
    <header class="topbar">
      <RouterLink to="/" class="brand">
        <span class="brand-mark">IP</span>
        <span class="brand-name">Interview Prep</span>
      </RouterLink>
      <nav class="topnav">
        <RouterLink to="/">Home</RouterLink>
        <RouterLink to="/interview">Mock interview</RouterLink>
      </nav>
    </header>

    <main class="content">
      <RouterView />
    </main>

    <footer class="footnote">
      Prototype scaffold · see <code>docs/CONTEXT.md</code> to continue in a new session
    </footer>
  </div>
</template>

<style scoped>
.layout { min-height: 100vh; display: flex; flex-direction: column; }
.topbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 0.9rem 1.4rem; border-bottom: 1px solid var(--border); background: var(--surface);
}
.brand { display: flex; align-items: center; gap: 0.6rem; text-decoration: none; color: var(--text); font-weight: 650; }
.brand-mark {
  display: grid; place-items: center; width: 28px; height: 28px; border-radius: 7px;
  background: var(--accent); color: #fff; font-size: 0.78rem; font-weight: 700;
}
.brand-name { font-size: 1.02rem; }
.topnav { display: flex; gap: 1.1rem; }
.topnav a { text-decoration: none; color: var(--muted); font-size: 0.94rem; padding-bottom: 2px; border-bottom: 2px solid transparent; }
.topnav a:hover { color: var(--text); }
.topnav a.router-link-active { color: var(--text); border-bottom-color: var(--accent); }
.content { flex: 1; width: 100%; max-width: 920px; margin: 0 auto; padding: 2rem 1.4rem; }
.footnote { text-align: center; color: var(--muted); font-size: 0.8rem; padding: 1.2rem; border-top: 1px solid var(--border); }
.footnote code { background: var(--surface-2); padding: 0.1rem 0.35rem; border-radius: 4px; }
</style>
