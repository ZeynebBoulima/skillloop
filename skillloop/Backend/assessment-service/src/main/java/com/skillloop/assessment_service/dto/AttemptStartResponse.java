package com.skillloop.assessment_service.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AttemptStartResponse(
        UUID attemptId,
        UUID assessmentId,
        String title,
        Instant startedAt,
        Instant expiresAt,
        int durationMinutes,
        List<QuestionStudentView> questions
) {}