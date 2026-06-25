<script setup>
import { ref, nextTick, watch, onBeforeUnmount, computed } from 'vue'
import { useInterviewStore } from '@/stores/interview'
import CodeEditor from '@/components/CodeEditor.vue'
import CodeBlock from '@/components/CodeBlock.vue'
import MarkdownText from '@/components/MarkdownText.vue'

const store = useInterviewStore()

// ---- setup form ----
const roles = [
  { label: 'Backend', value: 'Backend Engineer' },
  { label: 'Frontend', value: 'Frontend Engineer' },
  { label: 'Full-stack', value: 'Full-stack Engineer' },
  { label: 'Platform / infra', value: 'Platform Engineer' }
]
const levels = ['Junior', 'Mid', 'Senior', 'Staff+']
const languages = [
  { label: 'Java', value: 'java', file: 'Solution.java' },
  { label: 'Python', value: 'python', file: 'solution.py' },
  { label: 'JavaScript', value: 'javascript', file: 'solution.js' },
  { label: 'Go', value: 'go', file: 'solution.go' }
]
const starters = {
  java: 'class Solution {\n    // Write your solution here\n}\n',
  python: 'def solution():\n    # Write your solution here\n    pass\n',
  javascript: 'function solution() {\n  // Write your solution here\n}\n',
  go: 'package main\n\nfunc solution() {\n\t// Write your solution here\n}\n'
}

const role = ref('Backend Engineer')
const level = ref('Mid')
const language = ref('java')

// ---- live session ----
const code = ref('')
const draft = ref('')
const scroller = ref(null)
let timer = null
const elapsed = ref(0)

const currentLanguage = computed(() => languages.find((l) => l.value === store.language) || languages[0])

// ---- resizable editor/chat split ----
const ideEl = ref(null)
const split = ref(55) // % width given to the editor pane; rest goes to chat
let dragging = false
const ideStyle = computed(() => ({ gridTemplateColumns: `${split.value}% 8px minmax(0, 1fr)` }))

function startDrag(e) {
  dragging = true
  e.preventDefault()
  window.addEventListener('pointermove', onDrag)
  window.addEventListener('pointerup', stopDrag)
}
function onDrag(e) {
  if (!dragging || !ideEl.value) return
  const r = ideEl.value.getBoundingClientRect()
  const pct = ((e.clientX - r.left) / r.width) * 100
  split.value = Math.min(75, Math.max(25, pct))
}
function stopDrag() {
  dragging = false
  window.removeEventListener('pointermove', onDrag)
  window.removeEventListener('pointerup', stopDrag)
}

async function start() {
  await store.begin({
    type: 'AI_ASSISTED_CODING',
    targetRole: role.value,
    level: level.value,
    language: language.value
  })
  if (store.sessionId) {
    code.value = starters[language.value] ?? ''
    startTimer()
    scrollToEnd()
  }
}

async function send() {
  const text = draft.value
  draft.value = ''
  await store.reply(text)
  scrollToEnd()
}

async function submitCode() {
  if (!code.value.trim()) return
  const fenced = '```' + store.language + '\n' + code.value.trim() + '\n```'
  await store.reply(fenced)
  scrollToEnd()
}

async function endInterview() {
  stopTimer()
  await store.finish()
  scrollToEnd()
}

function restart() {
  stopTimer()
  elapsed.value = 0
  code.value = ''
  draft.value = ''
  store.reset()
}

// ---- timer ----
function startTimer() {
  elapsed.value = 0
  timer = setInterval(() => (elapsed.value += 1), 1000)
}
function stopTimer() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}
const clock = computed(() => {
  const m = Math.floor(elapsed.value / 60)
  const s = elapsed.value % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})
onBeforeUnmount(() => {
  stopTimer()
  stopDrag()
})

// ---- transcript rendering ----
// Split a message into plain-text and fenced-code segments so submitted code renders nicely.
function segments(content) {
  const parts = []
  const re = /```(\w*)\n?([\s\S]*?)```/g
  let last = 0
  let m
  while ((m = re.exec(content))) {
    if (m.index > last) parts.push({ type: 'text', text: content.slice(last, m.index) })
    parts.push({ type: 'code', text: m[2], lang: m[1] })
    last = re.lastIndex
  }
  if (last < content.length) parts.push({ type: 'text', text: content.slice(last) })
  return parts.length ? parts : [{ type: 'text', text: content }]
}

function scrollToEnd() {
  nextTick(() => {
    if (scroller.value) scroller.value.scrollTop = scroller.value.scrollHeight
  })
}
watch(() => store.messages.length, scrollToEnd)
// Keep pinned to the bottom as the streaming reply grows (length stays fixed, content grows).
watch(() => store.messages[store.messages.length - 1]?.content, scrollToEnd)

const busy = computed(() => store.loading || store.streaming)

// score ring geometry (0–5 → conic gradient)
const scoreDeg = computed(() => {
  const s = store.feedback?.overallScore ?? 0
  return Math.max(0, Math.min(100, (s / 5) * 100))
})
</script>

<template>
  <!-- ============ SETUP ============ -->
  <div v-if="!store.sessionId" class="setup">
    <p class="kicker">New mock interview</p>
    <h1>AI-assisted coding round</h1>
    <p class="sub">
      A ~30–40 minute conversation around one realistic problem. The interviewer probes your
      thinking and trade-offs — it won't hand you the answer.
    </p>

    <div class="field">
      <label>Target role</label>
      <div class="opts">
        <button
          v-for="r in roles"
          :key="r.value"
          class="opt"
          :class="{ on: role === r.value }"
          @click="role = r.value"
        >
          {{ r.label }}
        </button>
      </div>
    </div>

    <div class="field">
      <label>Level</label>
      <div class="opts">
        <button
          v-for="l in levels"
          :key="l"
          class="opt"
          :class="{ on: level === l }"
          @click="level = l"
        >
          {{ l }}
        </button>
      </div>
    </div>

    <div class="field">
      <label>Language <span class="hint">— the editor preloads a starter file</span></label>
      <div class="opts">
        <button
          v-for="lang in languages"
          :key="lang.value"
          class="opt"
          :class="{ on: language === lang.value }"
          @click="language = lang.value"
        >
          {{ lang.label }}
        </button>
      </div>
    </div>

    <button class="btn primary start" :disabled="store.loading" @click="start">
      {{ store.loading ? 'Starting…' : 'Start interview' }}
    </button>
    <p v-if="store.error" class="error">{{ store.error }}</p>
  </div>

  <!-- ============ FEEDBACK ============ -->
  <div v-else-if="store.finished && store.feedback" class="feedback">
    <div class="fb-top">
      <div class="fb-lead">
        <p class="kicker">Session feedback · {{ store.targetRole }} · {{ store.level }}</p>
        <h1>{{ store.feedback.headline }}</h1>
        <p class="sub">{{ store.feedback.summary }}</p>
      </div>
      <div class="score">
        <div class="ring" :style="{ background: `conic-gradient(var(--accent) ${scoreDeg}%, var(--surface-2) ${scoreDeg}%)` }">
          <b>{{ store.feedback.overallScore }}<span>/ 5</span></b>
        </div>
      </div>
    </div>

    <div class="rubric">
      <div v-for="item in store.feedback.rubric" :key="item.name" class="rgrid">
        <div class="rl">
          {{ item.name }}<span>{{ item.description }}</span>
        </div>
        <div class="bar">
          <i :style="{ width: (item.score / 5) * 100 + '%' }"></i>
        </div>
        <div class="rv">{{ item.score }}</div>
      </div>
    </div>

    <div class="cols">
      <div class="panel">
        <h4><span class="tag good"></span>Strengths</h4>
        <ul class="ilist good">
          <li v-for="(s, i) in store.feedback.strengths" :key="i">{{ s }}</li>
        </ul>
      </div>
      <div class="panel">
        <h4><span class="tag work"></span>Areas to improve</h4>
        <ul class="ilist work">
          <li v-for="(s, i) in store.feedback.improvements" :key="i">{{ s }}</li>
        </ul>
      </div>
    </div>

    <div class="next">
      <h4>Do this next</h4>
      <ol>
        <li v-for="(s, i) in store.feedback.nextSteps" :key="i">{{ s }}</li>
      </ol>
    </div>

    <button class="btn primary" @click="restart">Try another round</button>
  </div>

  <!-- ============ LIVE INTERVIEW ============ -->
  <div v-else class="session">
    <div class="session-head">
      <div>
        <h2>AI-assisted coding round</h2>
        <span class="meta">{{ store.targetRole }} · {{ store.level }}</span>
      </div>
      <div class="head-right">
        <span class="timer"><i></i>{{ clock }}</span>
        <button class="btn ghost small" :disabled="store.finishing" @click="endInterview">
          {{ store.finishing ? 'Scoring…' : 'End interview' }}
        </button>
      </div>
    </div>

    <div class="ide" ref="ideEl" :style="ideStyle">
      <!-- editor -->
      <div class="editor-pane">
        <div class="tab">{{ currentLanguage.file }}<span class="lang">{{ currentLanguage.label }}</span></div>
        <CodeEditor v-model="code" :language="store.language" class="editor" />
        <div class="editor-foot">
          <button class="btn primary small" :disabled="busy || !code.trim()" @click="submitCode">
            Submit code as my turn
          </button>
        </div>
      </div>

      <!-- drag to resize editor vs chat -->
      <div class="gutter" @pointerdown="startDrag" title="Drag to resize"></div>

      <!-- transcript -->
      <div class="talk">
        <div ref="scroller" class="thread">
          <div v-for="(m, i) in store.messages" :key="i" class="msg" :class="m.speaker">
            <div class="who">{{ m.speaker === 'interviewer' ? 'Interviewer' : 'You' }}</div>
            <div class="bubble">
              <template v-for="(seg, j) in segments(m.content)" :key="j">
                <CodeBlock v-if="seg.type === 'code'" :code="seg.text" :lang="seg.lang" />
                <MarkdownText v-else class="text" :text="seg.text" />
              </template>
              <span
                v-if="store.streaming && m.speaker === 'interviewer' && i === store.messages.length - 1"
                class="caret"
              ></span>
            </div>
          </div>
          <div v-if="store.loading" class="msg interviewer">
            <div class="who">Interviewer</div>
            <div class="bubble typing">…</div>
          </div>
        </div>

        <form class="composer" @submit.prevent="send">
          <input
            v-model="draft"
            type="text"
            placeholder="Reply, or edit code and submit…"
            :disabled="busy"
          />
          <button class="btn primary small" type="submit" :disabled="busy || !draft.trim()">
            Send
          </button>
        </form>
      </div>
    </div>
    <p v-if="store.error" class="error">{{ store.error }}</p>
  </div>
</template>

<style scoped>
.kicker {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 0.75rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--muted);
  margin: 0 0 0.6rem;
}
.sub { color: var(--muted); margin: 0 0 1.6rem; max-width: 52ch; }

/* ---- setup ---- */
.setup { max-width: 640px; }
.field { margin-bottom: 1.3rem; }
.field > label { display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 0.5rem; }
.field .hint { color: var(--muted); font-weight: 400; }
.opts { display: flex; gap: 0.5rem; flex-wrap: wrap; }
.opt {
  font: inherit; cursor: pointer;
  border: 1px solid var(--border); border-radius: 8px; background: var(--surface);
  color: var(--muted); padding: 0.5rem 0.9rem; font-size: 0.92rem;
}
.opt:hover { border-color: var(--accent); }
.opt.on { border-color: var(--accent); background: var(--accent-weak); color: var(--accent); font-weight: 600; }
.start { margin-top: 0.6rem; }

/* ---- live session ---- */
.session { display: flex; flex-direction: column; height: calc(100vh - 230px); min-height: 480px; }
.session-head { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 0.8rem; }
.session-head .meta { font-size: 0.82rem; color: var(--muted); }
.head-right { display: flex; align-items: center; gap: 0.8rem; }
.timer {
  display: inline-flex; align-items: center; gap: 0.4rem;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-size: 0.85rem; color: var(--muted);
}
.timer i { width: 7px; height: 7px; border-radius: 50%; background: var(--danger); display: block; }

.ide {
  flex: 1; display: grid; grid-template-columns: 55% 8px minmax(0, 1fr); min-height: 0;
}
.gutter {
  cursor: col-resize; border-radius: 999px; background: var(--border);
  transition: background 0.15s; touch-action: none;
}
.gutter:hover { background: var(--accent); }
@media (max-width: 720px) {
  .ide { grid-template-columns: 1fr !important; gap: 0.9rem; }
  .gutter { display: none; }
  .session { height: auto; }
}

/* editor */
.editor-pane {
  display: flex; flex-direction: column; min-height: 0;
  border: 1px solid var(--border); border-radius: var(--radius); overflow: hidden; background: #fff;
}
.tab {
  display: flex; align-items: center; padding: 0.5rem 0.8rem; border-bottom: 1px solid var(--border);
  background: var(--surface-2); font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-size: 0.8rem;
}
.tab .lang { margin-left: auto; color: var(--muted); }
.editor { flex: 1; min-height: 240px; }
.editor-foot { padding: 0.6rem 0.7rem; border-top: 1px solid var(--border); display: flex; justify-content: flex-end; }

/* transcript */
.talk {
  display: flex; flex-direction: column; min-height: 0;
  border: 1px solid var(--border); border-radius: var(--radius); overflow: hidden; background: var(--surface);
}
.thread { flex: 1; overflow-y: auto; padding: 1rem; display: flex; flex-direction: column; gap: 0.9rem; }
.msg { max-width: 92%; }
.msg.candidate { align-self: flex-end; }
.who { font-size: 0.72rem; color: var(--muted); margin-bottom: 0.2rem; }
.msg.candidate .who { text-align: right; }
.bubble {
  display: inline-block; padding: 0.6rem 0.85rem; border-radius: 12px;
  background: var(--surface-2); text-align: left;
}
.msg.candidate .bubble { background: var(--accent); color: #fff; }
.bubble.typing { letter-spacing: 0.15em; color: var(--muted); }
.caret {
  display: inline-block; width: 7px; height: 1em; margin-left: 1px;
  vertical-align: text-bottom; background: var(--accent); border-radius: 1px;
  animation: blink 1s step-start infinite;
}
@keyframes blink { 50% { opacity: 0; } }
.composer { display: flex; gap: 0.5rem; padding: 0.7rem; border-top: 1px solid var(--border); }
.composer input {
  flex: 1; padding: 0.55rem 0.75rem; font: inherit;
  border: 1px solid var(--border); border-radius: 8px; background: var(--surface); color: var(--text);
}
.composer input:focus { outline: 2px solid var(--accent-weak); border-color: var(--accent); }

/* ---- feedback ---- */
.feedback { max-width: 760px; }
.fb-top { display: flex; gap: 1.8rem; align-items: flex-start; flex-wrap: wrap; margin-bottom: 1.6rem; }
.fb-lead { flex: 1; min-width: 260px; }
.fb-lead h1 { font-size: 1.55rem; }
.score { flex: 0 0 auto; }
.ring { width: 92px; height: 92px; border-radius: 50%; display: grid; place-items: center; }
.ring b {
  width: 70px; height: 70px; border-radius: 50%; background: var(--surface);
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  font-size: 1.4rem; font-weight: 700;
}
.ring b span { font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-size: 0.62rem; color: var(--muted); font-weight: 500; }

.rubric { display: grid; gap: 1rem; margin-bottom: 2rem; }
.rgrid { display: grid; grid-template-columns: 170px 1fr 40px; gap: 0.9rem; align-items: center; }
@media (max-width: 560px) { .rgrid { grid-template-columns: 120px 1fr 34px; } }
.rl { font-size: 0.88rem; font-weight: 600; }
.rl span { display: block; font-weight: 400; color: var(--muted); font-size: 0.74rem; }
.bar { height: 8px; border-radius: 999px; background: var(--surface-2); overflow: hidden; }
.bar i { display: block; height: 100%; border-radius: 999px; background: var(--accent); }
.rv { font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-size: 0.85rem; text-align: right; font-weight: 600; }

.cols { display: grid; grid-template-columns: 1fr 1fr; gap: 1.8rem; margin-bottom: 1.8rem; }
@media (max-width: 560px) { .cols { grid-template-columns: 1fr; } }
.panel h4 {
  font-size: 0.74rem; font-family: ui-monospace, SFMono-Regular, Menlo, monospace; letter-spacing: 0.06em;
  text-transform: uppercase; margin: 0 0 0.9rem; display: flex; align-items: center; gap: 0.5rem; color: var(--text);
}
.tag { width: 9px; height: 9px; border-radius: 2px; }
.tag.good { background: var(--ok); }
.tag.work { background: #c98326; }
.ilist { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 0.7rem; }
.ilist li { font-size: 0.9rem; color: var(--text); line-height: 1.5; padding-left: 1.1rem; position: relative; }
.ilist li::before { content: ''; position: absolute; left: 0; top: 0.45rem; width: 7px; height: 7px; border-radius: 2px; }
.ilist.good li::before { background: var(--ok); }
.ilist.work li::before { background: #c98326; }

.next {
  background: var(--accent-weak); border: 1px solid #d5e0fb; border-radius: var(--radius);
  padding: 1.2rem 1.4rem; margin-bottom: 1.6rem;
}
.next h4 {
  font-size: 0.74rem; font-family: ui-monospace, SFMono-Regular, Menlo, monospace; letter-spacing: 0.06em;
  text-transform: uppercase; margin: 0 0 0.8rem; color: var(--accent);
}
.next ol { margin: 0; padding-left: 1.2rem; display: flex; flex-direction: column; gap: 0.5rem; }
.next li { font-size: 0.9rem; line-height: 1.5; }
</style>
