package com.skillloop.assessment_service.dto;

import com.skillloop.assessment_service.entity.AssessmentStatus;
import com.skillloop.assessment_service.entity.Level;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AssessmentResponse(
        UUID id,
        String title,
        String description,
        UUID skillId,
        Level level,
        int durationMinutes,
        int passingScore,
        AssessmentStatus status,
        UUID createdBy,
        Instant createdAt,
        List<QuestionResponse> questions
) {}