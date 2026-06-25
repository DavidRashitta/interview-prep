package com.interviewprep.controller;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

import com.interviewprep.dto.MessageRequest;
import com.interviewprep.dto.MessageResponse;
import com.interviewprep.dto.StartInterviewRequest;
import com.interviewprep.dto.StartInterviewResponse;
import com.interviewprep.dto.StreamChunk;
import com.interviewprep.model.InterviewFeedback;
import com.interviewprep.service.InterviewService;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewService service;

    public InterviewController(InterviewService service) {
        this.service = service;
    }

    /** Start a new mock interview. */
    @PostMapping
    public StartInterviewResponse start(@Valid @RequestBody StartInterviewRequest request) {
        return service.start(request);
    }

    /** Send one candidate turn and get the interviewer's reply. */
    @PostMapping("/{id}/messages")
    public MessageResponse message(@PathVariable String id, @Valid @RequestBody MessageRequest request) {
        return service.message(id, request.content());
    }

    /**
     * Streams the interviewer's opening message as newline-delimited JSON chunks
     * (one JSON-encoded string per token batch), so the UI can render it as it arrives.
     */
    @PostMapping(value = "/{id}/opening/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<StreamChunk> openingStream(@PathVariable String id) {
        return service.openingStream(id).map(StreamChunk::new);
    }

    /** Streams the interviewer's reply to one candidate turn as newline-delimited JSON chunks. */
    @PostMapping(value = "/{id}/messages/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<StreamChunk> messageStream(@PathVariable String id, @Valid @RequestBody MessageRequest request) {
        return service.messageStream(id, request.content()).map(StreamChunk::new);
    }

    /** End the interview and get structured feedback. */
    @PostMapping("/{id}/finish")
    public InterviewFeedback finish(@PathVariable String id) {
        return service.finish(id);
    }
}
