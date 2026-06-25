# Project Context — read this first to continue

This file is a self-contained brief. If you're opening a **new chat session** (to save
tokens) or handing this to an AI assistant, attach/paste this file and the repo and say
"continue from here." It captures what the project is, why, and exactly what to do next.

---

## 1. What this is

An **AI mock-interview tool** for the way technical hiring works in 2026: AI-assisted
coding rounds, code comprehension, debugging, and system design. The user runs a realistic
mock interview with an AI interviewer and gets useful feedback. A **niche newsletter**
("navigating AI-era interviews") is the free **marketing channel** that feeds users to the
tool — it is not a separate business.

## 2. Why this approach

- AI has upended technical interviews; the format is shifting fast while most candidates
  still prep with outdated, algorithm-grinding material. That **gap** is the opportunity.
- The model is a **tool, not coaching**. Credibility comes from the product working, not
  from the founder's interview résumé.
- Validate demand (landing page + waitlist) **before** building the full product.

## 3. Founder profile & constraints (shapes every decision)

- Solo builder; **basic Vue.js** experience; comfortable with **Java/Spring Boot + Gradle**.
- **~10–20 hours/week**, **~€1,000** budget, goal is **steady income over months**, not a quick flip.
- Therefore: keep the stack familiar, costs low, scope tight; ship one thing well.

## 4. Decisions locked so far

- Product = AI interview-prep **tool**; newsletter = **channel**.
- First interview type to build well: **AI-assisted coding round** (recommended) — newest
  format, least existing prep, natural fit for an AI tool. *(See open decision in §9.)*
- Backend: **Java 21 + Spring Boot 4.0.6 + Gradle**. Frontend: **Vue 3 + Vite** (plain JS),
  chosen to match the founder's existing experience.
- Sessions stored **in memory** for the prototype; move to a database in Phase 2.

## 5. Tech stack & rationale

| Layer | Choice | Why |
| --- | --- | --- |
| Backend | Java 21, Spring Boot 4.0.6, Gradle (wrapper 9.4.1) | Founder's stack; 4.0.x is the current supported line (3.5 hits OSS EOL 30 Jun 2026). |
| Frontend | Vue 3, Vite, Vue Router, Pinia, axios | Founder knows Vue; Vite is fast; plain JS lowers the barrier. |
| AI | Pluggable `AiInterviewer` interface | Swap the stub for a real LLM without touching controllers/UI. |

## 6. Repo layout

```
interview-prep/
├── README.md                 how to run
├── docs/
│   ├── CONTEXT.md            (this file)
│   ├── PROJECT_PLAN.md       business/build plan, phases 0–5 + budget
│   └── ARCHITECTURE.md       technical overview & extension points
├── backend/                  Spring Boot + Gradle
│   └── src/main/java/com/interviewprep/
│       ├── controller/  HealthController, InterviewController
│       ├── dto/         request/response records
│       ├── model/       InterviewType, InterviewSession
│       ├── service/     InterviewService (in-memory sessions)
│       ├── ai/          AiInterviewer (interface) + StubAiInterviewer  ← replace this
│       └── config/      WebConfig (CORS for the dev server)
└── frontend/                 Vue 3 + Vite
    └── src/
        ├── api/client.js     axios calls to the backend
        ├── stores/interview.js  Pinia store for one interview
        ├── views/            HomeView, InterviewView
        └── router/, assets/
```

## 7. How to run (quick)

- Backend: `cd backend && ./gradlew bootRun` → http://localhost:8080 (health at `/api/health`).
- Frontend: `cd frontend && npm install && npm run dev` → http://localhost:5173.
- Vite proxies `/api` to `:8080`, so the two talk to each other with no CORS friction in dev.

## 8. Current state (the coding-round loop is built)

- ✅ Full stack runs end to end: setup → live AI-led interview with a code editor → structured feedback.
- ✅ Endpoints: `GET /api/health`, `POST /api/interviews`, `POST /api/interviews/{id}/messages`,
  `POST /api/interviews/{id}/finish` (returns structured feedback).
- ✅ **Real interviewer** (`ChatClientInterviewer`) backed by **Spring AI** (`ChatClient`), behind the
  `AiInterviewer` seam. `AiConfig` selects it when `ANTHROPIC_API_KEY` is set, else falls back to
  `StubAiInterviewer` (canned replies) so the stack still boots without a key.
- ✅ Frontend: CodeMirror 6 editor (`components/CodeEditor.vue`) + chat transcript in `InterviewView`,
  plus an end-of-interview feedback report. Language choice seeds a starter file (Java/Python/JS/Go).
- ⛔ No code execution (out of scope for this build), no persistence beyond memory, no accounts, no payments.

## 9. How the AI layer is wired

- `ai/AiInterviewer` — the seam: `openingMessage`, `respond`, `generateFeedback`.
- `ai/ChatClientInterviewer` — built on **Spring AI's `ChatClient`** (provider-agnostic). Maps the
  transcript to `ChatClient` messages for chat turns, and uses `.entity(InterviewFeedback.class)` for
  the structured feedback (Spring AI generates the JSON schema and parses it — no manual JSON handling).
- `ai/InterviewPrompts` — the interviewer persona and the feedback rubric prompt.
- `config/AiConfig` — picks `ChatClientInterviewer` when `ANTHROPIC_API_KEY` is set, else the stub.
- `model/InterviewFeedback` — the structured feedback record (overall score, rubric, strengths,
  improvements, next steps); also the `/finish` response shape the UI renders.

**Switching LLM provider:** replace `spring-ai-starter-model-anthropic` in `build.gradle` with another
Spring AI model starter (e.g. `spring-ai-starter-model-openai`) and the `spring.ai.anthropic.*`
properties with that provider's (`spring.ai.openai.*`). `ChatClientInterviewer` does not change; update
the `ANTHROPIC_API_KEY` check in `AiConfig` to the new provider's key.

**Possible next steps:** add code execution in the browser, then Phase 2 (accounts, Postgres, Stripe).

## 10. Conventions & gotchas

- **Never commit API keys.** Use env vars; `.env` is gitignored, `.env.example` is committed.
- The `AiInterviewer` interface is the seam — keep AI/LLM logic behind it; don't leak it into controllers.
- In-memory `ConcurrentHashMap` session store is intentional for the prototype; swap for Postgres in Phase 2.
- Frontend is plain JS by choice; TypeScript can come later.
- Gradle wrapper is committed (`gradle/wrapper/gradle-wrapper.jar`) and pinned to 9.4.1 — don't delete it.

## 11. How to use this file in a new session

Start a fresh chat (optionally inside a Claude **Project** so the repo/docs stay attached),
paste or upload this `CONTEXT.md` (and the repo), and say something like:
*"Here's my project context. Let's do the Phase 0 task: build the AI interviewer for the
AI-assisted coding round."* That gives the assistant everything it needs without replaying
the whole previous conversation.
