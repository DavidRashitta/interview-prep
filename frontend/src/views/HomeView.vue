<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { getHealth } from '@/api/client'

const status = ref('checking') // 'checking' | 'ok' | 'down'
const detail = ref('')

onMounted(async () => {
  try {
    const data = await getHealth()
    status.value = 'ok'
    detail.value = data.service || 'backend'
  } catch (e) {
    status.value = 'down'
    detail.value = ''
  }
})
</script>

<template>
  <section class="hero">
    <p class="eyebrow">AI-assisted coding round</p>
    <h1>Practice interviews built for how hiring works now.</h1>
    <p class="lede">
      A mock-interview tool focused on the 2026 format. Run a realistic AI-led coding
      interview in a real editor, then get a structured feedback report — strengths,
      gaps, a rubric, and concrete next steps.
    </p>
    <div class="actions">
      <RouterLink class="btn primary" to="/interview">Start a mock interview</RouterLink>
    </div>
  </section>

  <section class="status">
    <span class="dot" :class="status"></span>
    <span v-if="status === 'checking'">Checking backend…</span>
    <span v-else-if="status === 'ok'">Backend connected ({{ detail }})</span>
    <span v-else>Backend not reachable — start it with <code>./gradlew bootRun</code></span>
  </section>
</template>

<style scoped>
.hero { padding: 1.5rem 0 2rem; max-width: 640px; }
.eyebrow { text-transform: uppercase; letter-spacing: 0.08em; font-size: 0.74rem; font-weight: 650; color: var(--accent); margin: 0 0 0.7rem; }
.lede { color: var(--muted); font-size: 1.05rem; margin: 0 0 1.4rem; }
.actions { display: flex; gap: 0.7rem; }
.status {
  display: inline-flex; align-items: center; gap: 0.55rem;
  font-size: 0.9rem; color: var(--muted);
  background: var(--surface); border: 1px solid var(--border);
  padding: 0.55rem 0.85rem; border-radius: 999px;
}
.status code { background: var(--surface-2); padding: 0.05rem 0.3rem; border-radius: 4px; color: var(--text); }
.dot { width: 9px; height: 9px; border-radius: 50%; background: var(--muted); }
.dot.ok { background: var(--ok); }
.dot.down { background: var(--danger); }
.dot.checking { background: #d9a521; }
</style>
