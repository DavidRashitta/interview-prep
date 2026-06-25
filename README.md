# Interview Prep

An AI mock-interview tool built for how technical hiring works in 2026 — AI-assisted
coding rounds, code comprehension, and system design — plus a niche newsletter as its
marketing channel. The **AI-assisted coding round** is built end to end: a setup screen,
a live AI-led interview with a real code editor, and a structured feedback report. Set
`ANTHROPIC_API_KEY` for real interviews; without it, a stub interviewer keeps the stack running.

> New here, or starting a fresh chat session? Read **[`docs/CONTEXT.md`](docs/CONTEXT.md)** first —
> it's a self-contained brief that lets you (or an AI assistant) pick up exactly where we left off.

## Stack

- **Backend:** Java 21 · Spring Boot 4.0.6 · Gradle (wrapper pinned to 9.4.1)
- **Frontend:** Vue 3 · Vite · Vue Router · Pinia · axios
- **AI:** Spring AI `ChatClient` (provider-agnostic) — Anthropic when `ANTHROPIC_API_KEY` is set, else a stub
- **Editor:** CodeMirror 6 (Java · Python · JavaScript · Go)

## Prerequisites

- **JDK 21** installed and on your `PATH` (`java -version` should show 21).
- **Node.js 20+** and npm (`node -v`).

## Run it

Open two terminals.

**1. Backend** (http://localhost:8080)

```bash
cd backend
./gradlew bootRun          # Windows: gradlew.bat bootRun
```

The first run downloads Gradle 9.4.1 and the dependencies, so give it a minute.
Check it's alive: <http://localhost:8080/api/health>

**2. Frontend** (http://localhost:5173)

```bash
cd frontend
npm install
npm run dev
```

Open <http://localhost:5173>. The home page pings the backend and shows a green dot when
connected. "Mock interview" runs a full request round-trip against the stub interviewer.

## Project layout

```
interview-prep/
├── README.md
├── docs/
│   ├── CONTEXT.md        ← start here to continue the project
│   ├── PROJECT_PLAN.md   ← the business/build plan, phases 0–5
│   └── ARCHITECTURE.md   ← technical overview & extension points
├── backend/              ← Spring Boot + Gradle
└── frontend/             ← Vue 3 + Vite
```

## Notes

- The frontend talks to the backend through Vite's dev proxy (`/api` → `:8080`), so no
  CORS issues in development. CORS for the dev server is also configured in `WebConfig`.
- Never commit API keys. The backend reads `ANTHROPIC_API_KEY` from the environment
  (see `backend/src/main/resources/application.properties`).
- The frontend is plain JavaScript to keep the barrier low; switching to TypeScript later
  is straightforward if you want it.
