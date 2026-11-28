package com.skillloop.assessment_service.dto;

import com.skillloop.assessment_service.entity.AttemptStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AttemptResultResponse(
        UUID attemptId,
        UUID assessmentId,
        UUID studentId,
        AttemptStatus status,
        Integer score,
        Integer earnedPoints,
        Integer totalPoints,
        Boolean passed,
        int passingScore,
        Instant startedAt,
        Instant submittedAt,
        List<QuestionResultResponse> details,
        CertificationResponse certification
) {}