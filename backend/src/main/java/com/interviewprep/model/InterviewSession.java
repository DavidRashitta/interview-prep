package com.interviewprep.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the state of a single mock interview, including the full transcript.
 * Stored in memory for the prototype (see InterviewService); move to a database in Phase 2.
 */
public class InterviewSession {

    /** One line of the transcript. {@code speaker} is "interviewer" or "candidate". */
    public record Turn(String speaker, String content, Instant at) {}

    private final String id;
    private final InterviewType type;
    private final String targetRole;   // e.g. "Backend Engineer"
    private final String level;        // e.g. "Mid", "Senior"
    private final String language;     // e.g. "java", "python" — the language the candidate codes in
    private final Instant createdAt;
    private final List<Turn> turns = new ArrayList<>();

    private boolean finished;
    private InterviewFeedback feedback;   // populated once the interview is finished

    public InterviewSession(String id, InterviewType type, String targetRole, String level, String language) {
        this.id = id;
        this.type = type;
        this.targetRole = targetRole;
        this.level = level;
        this.language = language;
        this.createdAt = Instant.now();
    }

    public void addTurn(String speaker, String content) {
        turns.add(new Turn(speaker, content, Instant.now()));
    }

    public String getId() { return id; }
    public InterviewType getType() { return type; }
    public String getTargetRole() { return targetRole; }
    public String getLevel() { return level; }
    public String getLanguage() { return language; }
    public Instant getCreatedAt() { return createdAt; }
    public List<Turn> getTurns() { return Collections.unmodifiableList(turns); }

    public boolean isFinished() { return finished; }

    public InterviewFeedback getFeedback() { return feedback; }

    /** Marks the interview complete and stores the feedback that was generated for it. */
    public void finish(InterviewFeedback feedback) {
        this.finished = true;
        this.feedback = feedback;
    }
}
