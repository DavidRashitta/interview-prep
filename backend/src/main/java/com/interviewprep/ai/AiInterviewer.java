package com.interviewprep.ai;

import com.interviewprep.model.InterviewFeedback;
import com.interviewprep.model.InterviewSession;

import reactor.core.publisher.Flux;

/**
 * Drives the conversation for a mock interview.
 *
 * <p>The current implementation ({@link StubAiInterviewer}) returns canned text so the
 * whole stack runs end to end without an API key. Phase 0's main task is to replace it
 * with a real LLM-backed implementation (e.g. the Anthropic API) that:
 * <ul>
 *   <li>opens with a question suited to the interview type, target role and level;</li>
 *   <li>responds to each candidate turn like a real interviewer would;</li>
 *   <li>eventually wraps up and (later) produces structured feedback.</li>
 * </ul>
 *
 * <p>To swap implementations, create a new {@code @Component} implementing this interface
 * and either remove {@code StubAiInterviewer} or mark the new one {@code @Primary}.
 */
public interface AiInterviewer {

    String openingMessage(InterviewSession session);

    String respond(InterviewSession session, String candidateMessage);

    /**
     * Streaming variants used for the live chat UI. The default implementations wrap the
     * blocking calls into a single-chunk stream, so a non-streaming interviewer (e.g. the
     * stub) works unchanged; LLM-backed ones should override to emit real token deltas.
     */
    default Flux<String> openingMessageStream(InterviewSession session) {
        return Flux.just(openingMessage(session));
    }

    default Flux<String> respondStream(InterviewSession session, String candidateMessage) {
        return Flux.just(respond(session, candidateMessage));
    }

    /**
     * Reviews the full transcript and produces structured end-of-interview feedback:
     * strengths, areas to improve, a rubric/score, and concrete next steps.
     */
    InterviewFeedback generateFeedback(InterviewSession session);
}
