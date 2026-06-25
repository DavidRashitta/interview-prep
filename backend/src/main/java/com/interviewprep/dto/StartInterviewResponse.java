package com.interviewprep.dto;

import com.interviewprep.model.InterviewType;

public record StartInterviewResponse(
        String sessionId,
        InterviewType type,
        String firstMessage
) {}
