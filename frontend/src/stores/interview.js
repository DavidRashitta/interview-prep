import { defineStore } from 'pinia'
import { startInterview, streamOpening, streamMessage, finishInterview } from '@/api/client'

// State for one mock coding-round interview: the live transcript plus the structured
// feedback produced when the candidate ends the session. Sessions live in the backend's
// memory for the prototype; this store just mirrors the current one.
export const useInterviewStore = defineStore('interview', {
  state: () => ({
    sessionId: null,
    type: null,
    targetRole: null,
    level: null,
    language: 'java',
    messages: [], // { speaker: 'interviewer' | 'candidate', content: string }
    loading: false, // waiting on the first chunk of an interviewer reply
    streaming: false, // interviewer reply is actively streaming in
    finishing: false, // generating feedback
    finished: false,
    feedback: null, // structured feedback once finished
    error: null
  }),
  actions: {
    async begin({ type, targetRole, level, language }) {
      this.loading = true
      this.error = null
      try {
        const data = await startInterview({ type, targetRole, level, language })
        this.sessionId = data.sessionId
        this.type = data.type
        this.targetRole = targetRole
        this.level = level
        this.language = language
        this.finished = false
        this.feedback = null
        this.messages = []
        // Opening is streamed token-by-token rather than returned by start().
        await this._streamInto((onChunk) => streamOpening(this.sessionId, onChunk))
      } catch (e) {
        this.error = 'Could not start the interview. Is the backend running on port 8080?'
      } finally {
        this.loading = false
        this.streaming = false
      }
    },
    async reply(content) {
      if (!this.sessionId || !content.trim() || this.finished || this.streaming) return
      this.messages.push({ speaker: 'candidate', content })
      this.loading = true
      this.error = null
      try {
        await this._streamInto((onChunk) => streamMessage(this.sessionId, content, onChunk))
      } catch (e) {
        this.error = 'Message failed to send.'
      } finally {
        this.loading = false
        this.streaming = false
      }
    },
    // Runs a streaming call, lazily creating one interviewer message and appending each
    // chunk to it. The "loading" (typing dots) state holds until the first chunk lands,
    // then "streaming" takes over while text flows in.
    async _streamInto(run) {
      let idx = -1
      this.streaming = true
      await run((chunk) => {
        if (idx === -1) {
          this.messages.push({ speaker: 'interviewer', content: '' })
          idx = this.messages.length - 1
          this.loading = false
        }
        this.messages[idx].content += chunk
      })
    },
    async finish() {
      if (!this.sessionId || this.finishing || this.finished) return
      this.finishing = true
      this.error = null
      try {
        this.feedback = await finishInterview(this.sessionId)
        this.finished = true
      } catch (e) {
        this.error = 'Could not generate feedback. Please try again.'
      } finally {
        this.finishing = false
      }
    },
    reset() {
      this.sessionId = null
      this.type = null
      this.targetRole = null
      this.level = null
      this.messages = []
      this.loading = false
      this.streaming = false
      this.finished = false
      this.finishing = false
      this.feedback = null
      this.error = null
    }
  }
})
