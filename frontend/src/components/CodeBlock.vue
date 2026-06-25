<script setup>
import { ref } from 'vue'

const props = defineProps({
  code: { type: String, default: '' },
  lang: { type: String, default: '' }
})

const copied = ref(false)
let timer = null

async function copy() {
  try {
    await navigator.clipboard.writeText(props.code)
    copied.value = true
    clearTimeout(timer)
    timer = setTimeout(() => (copied.value = false), 1500)
  } catch (e) {
    // clipboard blocked (e.g. insecure context) — silently ignore
  }
}
</script>

<template>
  <div class="cb">
    <div class="cb-head">
      <span class="cb-lang">{{ lang || 'code' }}</span>
      <button class="cb-copy" type="button" @click="copy">
        {{ copied ? '✓ Copied' : 'Copy' }}
      </button>
    </div>
    <pre class="cb-body"><code>{{ code }}</code></pre>
  </div>
</template>

<style scoped>
.cb {
  margin: 0.5rem 0; border-radius: 8px; overflow: hidden;
  background: #1b1e26; border: 1px solid #2a2e38;
}
.cb-head {
  display: flex; align-items: center; justify-content: space-between;
  padding: 0.35rem 0.6rem; background: #11131a; border-bottom: 1px solid #2a2e38;
}
.cb-lang {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 0.72rem; color: #8a90a0; letter-spacing: 0.04em;
}
.cb-copy {
  font: inherit; font-size: 0.72rem; cursor: pointer;
  color: #c7ccd6; background: #232732; border: 1px solid #343a47;
  padding: 0.2rem 0.55rem; border-radius: 5px;
}
.cb-copy:hover { background: #2c3140; }
.cb-body {
  margin: 0; padding: 0.7rem 0.8rem; overflow-x: auto;
  color: #c7ccd6; font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 0.8rem; line-height: 1.55;
}
.cb-body code { font: inherit; }
</style>
