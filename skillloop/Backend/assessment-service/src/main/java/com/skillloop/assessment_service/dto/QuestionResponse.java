package com.skillloop.assessment_service.dto;

import com.skillloop.assessment_service.entity.Difficulty;
import com.skillloop.assessment_service.entity.QuestionType;

import java.util.UUID;

public record QuestionResponse(
        UUID id,
        String statement,
        QuestionType type,
        Difficulty difficulty,
        int orderIndex,
        int points
) {}