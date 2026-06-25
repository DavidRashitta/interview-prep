package com.interviewprep.model;

import java.util.List;

/**
 * Structured feedback produced at the end of an interview. Shape is the API contract for
 * the feedback view: an overall score, a short narrative, a per-dimension rubric, and
 * concrete lists the UI renders directly.
 *
 * <p>Scores are on a 0–5 scale (one decimal place is fine).
 */
public record InterviewFeedback(
        double overallScore,
        String headline,
        String summary,
        List<RubricItem> rubric,
        List<String> strengths,
        List<String> improvements,
        List<String> nextSteps
) {
    /** One row of the rubric: a named dimension with a short description and a 0–5 score. */
    public record RubricItem(String name, String description, double score) {}
}
