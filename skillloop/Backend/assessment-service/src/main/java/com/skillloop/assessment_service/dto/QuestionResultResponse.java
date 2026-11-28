package com.skillloop.assessment_service.dto;

import java.util.UUID;

public record QuestionResultResponse(
        UUID questionId,
        int orderIndex,
        boolean correct,
        int pointsAwarded,
        int maxPoints
) {}