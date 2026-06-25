# Architecture

A small, deliberately simple two-tier app: a Vue single-page app talks to a Spring Boot
REST API, which delegates the actual interview conversation to a pluggable AI component.

## Components & request flow

```
  Browser (Vue 3 SPA)
        │   axios → /api/...   (Vite dev-proxies /api to :8080)
        ▼
  Spring Boot REST API  (controllers)
        │
        ▼
  InterviewService  ── holds sessions (in-memory ConcurrentHashMap for now)
        │
        ▼
  AiInterviewer (interface)
        ├─ StubAiInterviewer   ← current: canned replies
        └─ <LlmInterviewer>    ← next: calls an LLM (e.g. Anthropic API)
```

**Start an interview**
`POST /api/interviews` `{ type, targetRole, level }`
→ `InterviewService.start()` creates an `InterviewSession`, asks the `AiInterviewer` for an
opening message, stores it, and returns `{ sessionId, type, firstMessage }`.

**Each turn**
`POST /api/interviews/{id}/messages` `{ content }`
→ `InterviewService.message()` appends the candidate turn, asks the `AiInterviewer` to
respond given the session transcript, stores the reply, and returns `{ reply, finished }`.
Unknown `id` → 404 via `SessionNotFoundException`.

## Key extension points

- **`AiInterviewer`** (`ai/`): the single seam for AI logic. Add a real implementation here;
  keep prompts, model calls, and parsing behind this interface so controllers/UI don't change.
- **`InterviewSession`** (`model/`): holds the transcript (`List<Turn>`). Add fields for
  interview phase, scoring, or code submissions as the real interviewer grows.
- **Persistence**: `InterviewService` uses an in-memory map. To persist, introduce a
  repository (e.g. Spring Data JPA + Postgres) and store sessions/transcripts there — the
  service API can stay the same.
- **Frontend state**: `stores/interview.js` (Pinia) owns one interview's state. The coding
  round will add a code-editor component and a feedback view in `views/`.

## Configuration & security

- **AI key**: read from the `ANTHROPIC_API_KEY` env var, bound to `app.ai.api-key` in
  `application.properties`. Never hard-code or commit keys.
- **CORS**: `WebConfig` allows `http://localhost:5173` for development. In production, serve
  the built frontend from the same origin (or tighten allowed origins) and you can drop this.
- **Validation**: request DTOs use Jakarta Bean Validation (`@NotNull`, `@NotBlank`) with
  `@Valid` in controllers, so malformed requests are rejected with 400.

## Planned additions (later phases)

- **Accounts/auth** (Phase 2): add Spring Security; protect interview endpoints per user.
- **Payments** (Phase 2–3): Stripe checkout + a subscription/credits model; gate sessions.
- **Database** (Phase 2): replace in-memory sessions with Postgres.
- **Deployment**: frontend on a static/edge host (e.g. Vercel), backend on a small managed
  host; start on free tiers, scale with revenue.
