Build brief: AI mock-interview tool — first build target
Product. A web app where someone runs a realistic AI mock interview and gets useful feedback, focused on the 2026 technical-interview format (AI-assisted coding, code comprehension, debugging, system design). A niche newsletter is the marketing channel — not part of this codebase; don't build it.
This build target. The core working product: one interview type, done well, end to end — start → realistic AI-led interview → structured feedback. No accounts or payments yet; those come next, once this loop is good.
First interview type: AI-assisted coding round.
Already built — extend it, don't rebuild
The repo is a working Spring Boot + Vue scaffold:

Backend (Java 21, Spring Boot 4.0.6, Gradle): REST endpoints for health, start-interview, and send-message. Interview logic sits behind an AiInterviewer interface with a StubAiInterviewer that returns canned text. In-memory session store in InterviewService. CORS configured for the Vite dev server.
Frontend (Vue 3, Vite, Pinia, axios): home page pings health; InterviewView already does a full start→message round-trip; state in stores/interview.js; API calls in api/client.js.
The whole stack runs today — the only gap is the stubbed interviewer.

In scope (build this)

Real AI interviewer. Replace StubAiInterviewer with an AiInterviewer implementation backed by the Anthropic API. It should open with a coding task suited to targetRole + level, respond to each candidate turn using the full transcript, ask realistic follow-ups and probe trade-offs, and behave like an interviewer — not a tutor that hands over the answer.
Code editor in the UI. Add a code editor to InterviewView so the candidate writes/edits code and submits it as their turn. CodeMirror 6 is light and Vite-friendly; Monaco is fine too.
End + structured feedback. Add a way to finish the interview and get feedback: strengths, areas to improve, a rough rubric/score, and concrete next steps. Suggest a new endpoint (e.g. POST /api/interviews/{id}/finish) returning structured JSON, rendered in a feedback view.
Keep sessions in memory for now.

Out of scope (don't build yet)
User accounts/auth, payments/Stripe, a database, additional interview types, the newsletter, analytics. These are later phases.
Anthropic API integration

The key comes from the ANTHROPIC_API_KEY env var (already bound to app.ai.api-key; model in app.ai.model). Never hard-code or commit it.
Call POST https://api.anthropic.com/v1/messages with headers x-api-key, anthropic-version: 2023-06-01, and content-type: application/json. Body: model, max_tokens, a system prompt (the interviewer persona + rules), and a messages array mapped from the transcript (interviewer turns → assistant, candidate turns → user). Read the reply from content[0].text. Use Spring's RestClient or WebClient.
Keep all LLM logic behind the AiInterviewer interface — controllers and UI must not know about the LLM.

Constraints

Stack is fixed: Java 21 + Spring Boot + Gradle backend; Vue 3 + Vite (plain JS) frontend. Solo builder with basic Vue and solid Java — favor clear, conventional code over cleverness.
Don't commit secrets; keep the AiInterviewer seam intact.

Done when
A user can pick the coding round, get a realistic AI-led interview with a working code editor, end it, and receive useful structured feedback — running locally against a real Anthropic key.
