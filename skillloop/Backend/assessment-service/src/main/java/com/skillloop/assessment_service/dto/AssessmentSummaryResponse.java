package com.skillloop.assessment_service.dto;

import com.skillloop.assessment_service.entity.AssessmentStatus;
import com.skillloop.assessment_service.entity.Level;

import java.util.UUID;

public record AssessmentSummaryResponse(
        UUID id,
        String title,
        UUID skillId,
        Level level,
        AssessmentStatus status,
        int durationMinutes,
        int passingScore,
        int questionCount
) {}