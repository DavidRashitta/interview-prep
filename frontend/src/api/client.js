import axios from 'axios'

const BASE = import.meta.env.VITE_API_BASE || '/api'

const api = axios.create({
  baseURL: BASE,
  headers: { 'Content-Type': 'application/json' }
})

/**
 * Reads a newline-delimited-JSON stream from the backend, invoking onChunk(string)
 * for each decoded chunk as it arrives. Each line is a JSON-encoded string, so
 * embedded newlines (code blocks) survive intact. Uses fetch (not axios) because we
 * need the streaming response body reader.
 */
async function streamNdjson(path, body, onChunk) {
  const res = await fetch(`${BASE}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: body ? JSON.stringify(body) : undefined
  })
  if (!res.ok || !res.body) throw new Error(`stream failed: ${res.status}`)

  const reader = res.body.getReader()
  const decoder = new TextDecoder()
  let buf = ''
  const flushLine = (line) => {
    const t = line.trim()
    if (t) onChunk(JSON.parse(t))
  }
  for (;;) {
    const { value, done } = await reader.read()
    if (done) break
    buf += decoder.decode(value, { stream: true })
    let nl
    while ((nl = buf.indexOf('\n')) >= 0) {
      flushLine(buf.slice(0, nl))
      buf = buf.slice(nl + 1)
    }
  }
  buf += decoder.decode()
  flushLine(buf)
}

export function getHealth() {
  return api.get('/health').then((r) => r.data)
}

export function startInterview(payload) {
  // payload: { type, targetRole, level, language }
  return api.post('/interviews', payload).then((r) => r.data)
}

export function sendMessage(sessionId, content) {
  return api.post(`/interviews/${sessionId}/messages`, { content }).then((r) => r.data)
}

// Each NDJSON line is { text: "<chunk>" }; hand the bare text to the caller.
export function streamOpening(sessionId, onChunk) {
  return streamNdjson(`/interviews/${sessionId}/opening/stream`, null, (o) => onChunk(o.text))
}

export function streamMessage(sessionId, content, onChunk) {
  return streamNdjson(`/interviews/${sessionId}/messages/stream`, { content }, (o) => onChunk(o.text))
}

export function finishInterview(sessionId) {
  // Returns structured feedback: { overallScore, headline, summary, rubric, strengths, improvements, nextSteps }
  return api.post(`/interviews/${sessionId}/finish`).then((r) => r.data)
}

export default api
