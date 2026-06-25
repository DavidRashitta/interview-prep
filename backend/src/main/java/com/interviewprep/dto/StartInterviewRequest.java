package com.interviewprep.dto;

import com.interviewprep.model.InterviewType;
import jakarta.validation.constraints.NotNull;

/** Body for POST /api/interviews. targetRole, level and language are optional hints for the interviewer. */
public record StartInterviewRequest(
        @NotNull InterviewType type,
        String targetRole,
        String level,
        String language
) {}
