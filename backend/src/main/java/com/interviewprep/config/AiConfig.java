package com.interviewprep.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.interviewprep.ai.AiInterviewer;
import com.interviewprep.ai.ChatClientInterviewer;
import com.interviewprep.ai.StubAiInterviewer;

/**
 * Chooses which {@link AiInterviewer} the app runs with: the Spring AI {@link ChatClient}-backed
 * one when an API key is configured, otherwise the stub so the stack still boots and runs without
 * a key. Keeping this decision in one bean keeps the {@code AiInterviewer} seam clean.
 *
 * <p>Spring AI auto-configures the {@code ChatClient.Builder} for whichever model starter is on
 * the classpath. The key check below is the only Anthropic-specific line; when switching providers,
 * point it at that provider's key env var.
 */
@Configuration
public class AiConfig {

    private static final Logger log = LoggerFactory.getLogger(AiConfig.class);

    @Bean
    AiInterviewer aiInterviewer(
            @Value("${OPENROUTER_API_KEY:}") String apiKey,
            ObjectProvider<ChatClient.Builder> chatClientBuilder) {

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("No OPENROUTER_API_KEY set — using the stub interviewer (canned replies). "
                    + "Set the env var to run real interviews.");
            return new StubAiInterviewer();
        }

        ChatClient.Builder builder = chatClientBuilder.getIfAvailable();
        if (builder == null) {
            log.warn("API key set but no ChatClient.Builder is available — falling back to the stub. "
                    + "Check that a Spring AI model starter is on the classpath.");
            return new StubAiInterviewer();
        }

        log.info("Spring AI interviewer enabled.");
        return new ChatClientInterviewer(builder);
    }
}
