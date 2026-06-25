package com.interviewprep.dto;

/**
 * One chunk of a streamed interviewer reply. Wrapping the text in an object (rather than
 * streaming bare strings) means each NDJSON line is a JSON object with the newlines inside
 * {@code text} escaped — so code blocks survive line-based parsing on the client.
 */
public record StreamChunk(String text) {}
