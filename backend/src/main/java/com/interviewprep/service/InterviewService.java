package com.interviewprep.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.interviewprep.ai.AiInterviewer;
import com.interviewprep.dto.MessageResponse;
import com.interviewprep.dto.StartInterviewRequest;
import com.interviewprep.dto.StartInterviewResponse;
import com.interviewprep.model.InterviewFeedback;
import com.interviewprep.model.InterviewSession;
import com.interviewprep.model.InterviewType;

import reactor.core.publisher.Flux;

/**
 * Coordinates interview sessions and delegates the actual conversation to an {@link AiInterviewer}.
 * Sessions are kept in memory for the prototype; replace with a database in Phase 2.
 */
@Service
public class InterviewService {

    private final AiInterviewer interviewer;
    private final Map<String, InterviewSession> sessions = new ConcurrentHashMap<>();

    public InterviewService(AiInterviewer interviewer) {
        this.interviewer = interviewer;
    }

    public StartInterviewResponse start(StartInterviewRequest request) {
        InterviewType type = request.type();
        InterviewSession session = new InterviewSession(
                UUID.randomUUID().toString(), type, request.targetRole(), request.level(), request.language());
        sessions.put(session.getId(), session);

        // The opening message is streamed separately via openingStream(), so the UI can render
        // it token-by-token instead of blocking the start request on the full LLM reply.
        return new StartInterviewResponse(session.getId(), type, null);
    }

    /**
     * Streams the interviewer's opening message and records it on the transcript once complete.
     * Idempotent on reconnect: if the opening was already produced, it is replayed as one chunk.
     */
    public Flux<String> openingStream(String sessionId) {
        InterviewSession session = require(sessionId);
        if (!session.getTurns().isEmpty()) {
            return Flux.just(session.getTurns().get(0).content());
        }
        StringBuilder full = new StringBuilder();
        return interviewer.openingMessageStream(session)
                .doOnNext(full::append)
                .doOnComplete(() -> session.addTurn("interviewer", full.toString()));
    }

    public MessageResponse message(String sessionId, String content) {
        InterviewSession session = require(sessionId);
        session.addTurn("candidate", content);
        String reply = interviewer.respond(session, content);
        session.addTurn("interviewer", reply);
        return new MessageResponse(reply, false);
    }

    /**
     * Streams the interviewer's reply to one candidate turn, recording both turns on the
     * transcript. The candidate turn is added up front; the interviewer turn once the stream ends.
     */
    public Flux<String> messageStream(String sessionId, String content) {
        InterviewSession session = require(sessionId);
        session.addTurn("candidate", content);
        StringBuilder full = new StringBuilder();
        return interviewer.respondStream(session, content)
                .doOnNext(full::append)
                .doOnComplete(() -> session.addTurn("interviewer", full.toString()));
    }

    private InterviewSession require(String sessionId) {
        InterviewSession session = sessions.get(sessionId);
        if (session == null) {
            throw new SessionNotFoundException(sessionId);
        }
        return session;
    }

    /**
     * Ends the interview and returns structured feedback. Feedback is generated once and
     * cached on the session, so calling finish again returns the same report.
     */
    public InterviewFeedback finish(String sessionId) {
        InterviewSession session = sessions.get(sessionId);
        if (session == null) {
            throw new SessionNotFoundException(sessionId);
        }
        if (session.isFinished()) {
            return session.getFeedback();
        }
        InterviewFeedback feedback = interviewer.generateFeedback(session);
        session.finish(feedback);
        return feedback;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class SessionNotFoundException extends RuntimeException {
        public SessionNotFoundException(String id) {
            super("No interview session with id " + id);
        }
    }
}
