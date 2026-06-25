package com.interviewprep.ai;

import java.util.List;

import com.interviewprep.model.InterviewFeedback;
import com.interviewprep.model.InterviewSession;

/**
 * Placeholder interviewer that returns deterministic text. It exists so the whole stack
 * still runs end to end when no {@code ANTHROPIC_API_KEY} is configured. When a key is
 * present, {@link com.interviewprep.config.AiConfig} wires {@link AnthropicAiInterviewer}
 * instead of this.
 */
public class StubAiInterviewer implements AiInterviewer {

    @Override
    public String openingMessage(InterviewSession session) {
        String role = session.getTargetRole() == null ? "software" : session.getTargetRole();
        return ("Hi, thanks for joining. This is a mock %s interview for a %s role. "
                + "To start, tell me briefly about a recent project and one technical decision you made. "
                + "[Placeholder interviewer — replace StubAiInterviewer with a real AI implementation.]")
                .formatted(prettyType(session), role);
    }

    @Override
    public String respond(InterviewSession session, String candidateMessage) {
        long interviewerTurns = session.getTurns().stream()
                .filter(t -> "interviewer".equals(t.speaker()))
                .count();
        return ("Thanks. [Placeholder reply #%d.] Can you go deeper on the trade-offs, "
                + "and how you would handle this at scale?")
                .formatted(interviewerTurns);
    }

    @Override
    public InterviewFeedback generateFeedback(InterviewSession session) {
        return new InterviewFeedback(
                3.5,
                "Placeholder feedback — set ANTHROPIC_API_KEY for a real review.",
                "This is canned feedback from the stub interviewer. With an Anthropic API key "
                        + "configured, this becomes a real assessment of your interview.",
                List.of(
                        new InterviewFeedback.RubricItem("Problem solving", "approach & correctness", 3.5),
                        new InterviewFeedback.RubricItem("Code quality", "structure & idioms", 3.5),
                        new InterviewFeedback.RubricItem("Communication", "thinking out loud", 3.5),
                        new InterviewFeedback.RubricItem("System trade-offs", "scaling & edge cases", 3.5)),
                List.of("You completed a full round trip against the stub interviewer."),
                List.of("Configure ANTHROPIC_API_KEY to get genuine, specific feedback."),
                List.of("Set the API key, then run a real coding-round interview."));
    }

    private String prettyType(InterviewSession session) {
        return switch (session.getType()) {
            case AI_ASSISTED_CODING -> "AI-assisted coding";
            case SYSTEM_DESIGN -> "system design";
            case BEHAVIORAL -> "behavioral";
        };
    }
}
