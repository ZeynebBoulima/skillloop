package com.skillloop.assessment_service.dto;

import com.skillloop.assessment_service.entity.Difficulty;
import com.skillloop.assessment_service.entity.QuestionType;
import jakarta.validation.constraints.*;

public record QuestionRequest(
        @NotBlank @Size(max = 2000) String statement,
        @NotNull QuestionType type,
        @NotNull Difficulty difficulty,
        @NotNull @Min(1) Integer orderIndex,
        @Min(1) Integer points
) {}