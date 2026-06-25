<script setup>
import { computed } from 'vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

// Renders a markdown text segment to safe HTML. Code fences are handled
// upstream by CodeBlock — this covers bold, lists, headings, inline code, etc.
// LLM output is untrusted, so always sanitize before v-html.
const props = defineProps({
  text: { type: String, default: '' }
})

const html = computed(() =>
  DOMPurify.sanitize(marked.parse(props.text.trim(), { breaks: true }))
)
</script>

<template>
  <div class="md" v-html="html"></div>
</template>

<style scoped>
.md { line-height: 1.55; }
.md :deep(p) { margin: 0 0 0.6rem; }
.md :deep(p:last-child) { margin-bottom: 0; }
.md :deep(strong) { font-weight: 650; }
.md :deep(ul),
.md :deep(ol) { margin: 0.3rem 0 0.6rem; padding-left: 1.2rem; }
.md :deep(li) { margin: 0.15rem 0; }
.md :deep(h1),
.md :deep(h2),
.md :deep(h3),
.md :deep(h4) { font-size: 0.98rem; margin: 0.6rem 0 0.4rem; }
.md :deep(code) {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 0.85em; padding: 0.1rem 0.35rem; border-radius: 4px;
  background: var(--surface-2);
}
.md :deep(a) { color: var(--accent); }
.md :deep(p:first-child) { margin-top: 0; }
</style>
