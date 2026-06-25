package com.interviewprep.dto;

import jakarta.validation.constraints.NotBlank;

/** Body for POST /api/interviews/{id}/messages. */
public record MessageRequest(@NotBlank String content) {}
