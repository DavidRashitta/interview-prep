<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { EditorView, basicSetup } from 'codemirror'
import { java } from '@codemirror/lang-java'
import { python } from '@codemirror/lang-python'
import { javascript } from '@codemirror/lang-javascript'
import { go } from '@codemirror/lang-go'

// A thin Vue wrapper around CodeMirror 6. v-model carries the code text.
// The language is fixed for the editor's lifetime (it's chosen on the setup
// screen before this mounts), so no dynamic reconfiguration is needed.
const props = defineProps({
  modelValue: { type: String, default: '' },
  language: { type: String, default: 'java' }
})
const emit = defineEmits(['update:modelValue'])

const host = ref(null)
let view = null

function languageExtension(lang) {
  switch (lang) {
    case 'python':
      return python()
    case 'javascript':
      return javascript()
    case 'go':
      return go()
    case 'java':
    default:
      return java()
  }
}

onMounted(() => {
  view = new EditorView({
    doc: props.modelValue,
    extensions: [
      basicSetup,
      languageExtension(props.language),
      EditorView.updateListener.of((update) => {
        if (update.docChanged) {
          emit('update:modelValue', update.state.doc.toString())
        }
      })
    ],
    parent: host.value
  })
})

onBeforeUnmount(() => {
  if (view) view.destroy()
})

// Keep the editor in sync if the parent swaps the text programmatically.
watch(
  () => props.modelValue,
  (val) => {
    if (view && val !== view.state.doc.toString()) {
      view.dispatch({ changes: { from: 0, to: view.state.doc.length, insert: val ?? '' } })
    }
  }
)
</script>

<template>
  <div ref="host" class="cm-host"></div>
</template>

<style scoped>
.cm-host {
  height: 100%;
  overflow: hidden;
}
/* Make the editor fill the host and scroll internally. */
.cm-host :deep(.cm-editor) {
  height: 100%;
}
.cm-host :deep(.cm-scroller) {
  overflow: auto;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 13px;
}
</style>
