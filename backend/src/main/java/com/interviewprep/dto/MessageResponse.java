package com.interviewprep.dto;

/** The interviewer's reply. {@code finished} signals the interview has wrapped up. */
public record MessageResponse(String reply, boolean finished) {}
