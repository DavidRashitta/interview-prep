package com.interviewprep.ai;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;

import com.interviewprep.model.InterviewFeedback;
import com.interviewprep.model.InterviewSession;

import reactor.core.publisher.Flux;

/**
 * LLM-backed {@link AiInterviewer} built on Spring AI's provider-agnostic {@link ChatClient}.
 * The concrete provider (Anthropic today) is chosen by the {@code spring-ai-starter-model-*}
 * dependency and {@code spring.ai.*} properties — this class never names a provider, so
 * switching to another model is a build/config change, not a code change.
 *
 * <p>All prompt/LLM detail stays behind the {@link AiInterviewer} seam; controllers, the
 * service, and the UI never see it.
 */
public class ChatClientInterviewer implements AiInterviewer {

    private static final int FEEDBACK_MAX_TOKENS = 2048;

    private final ChatClient chat;

    public ChatClientInterviewer(ChatClient.Builder builder) {
        this.chat = builder.build();
    }

    @Override
    public String openingMessage(InterviewSession session) {
        return chat.prompt()
                .system(InterviewPrompts.interviewerSystem(session))
                .messages(new UserMessage(InterviewPrompts.KICKOFF))
                .call()
                .content();
    }

    @Override
    public String respond(InterviewSession session, String candidateMessage) {
        return chat.prompt()
                .system(InterviewPrompts.interviewerSystem(session))
                .messages(conversation(session))
                .call()
                .content();
    }

    @Override
    public Flux<String> openingMessageStream(InterviewSession session) {
        return chat.prompt()
                .system(InterviewPrompts.interviewerSystem(session))
                .messages(new UserMessage(InterviewPrompts.KICKOFF))
                .stream()
                .content();
    }

    @Override
    public Flux<String> respondStream(InterviewSession session, String candidateMessage) {
        return chat.prompt()
                .system(InterviewPrompts.interviewerSystem(session))
                .messages(conversation(session))
                .stream()
                .content();
    }

    @Override
    public InterviewFeedback generateFeedback(InterviewSession session) {
        List<Message> messages = conversation(session);
        messages.add(new UserMessage(
                "The interview is over. Review my performance and produce the feedback now."));

        // .entity() asks Spring AI to coerce the reply into the record (it injects the JSON
        // schema/format instructions for us — no manual parsing). Feedback gets a larger budget.
        return chat.prompt()
                .system(InterviewPrompts.feedbackSystem(session))
                .messages(messages)
                .options(ChatOptions.builder().maxTokens(FEEDBACK_MAX_TOKENS))
                .call()
                .entity(InterviewFeedback.class);
    }

    /**
     * Maps the stored transcript into Spring AI messages. The conversation must start with a
     * user turn, so we prepend the kickoff; the stored opening (an interviewer turn) becomes the
     * first assistant turn, and the rest alternates.
     */
    private List<Message> conversation(InterviewSession session) {
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage(InterviewPrompts.KICKOFF));
        for (InterviewSession.Turn turn : session.getTurns()) {
            messages.add("interviewer".equals(turn.speaker())
                    ? new AssistantMessage(turn.content())
                    : new UserMessage(turn.content()));
        }
        return messages;
    }
}
