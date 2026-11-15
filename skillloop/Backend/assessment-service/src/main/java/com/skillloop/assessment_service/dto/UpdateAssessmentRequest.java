package com.skillloop.assessment_service.dto;

import com.skillloop.assessment_service.entity.Level;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record UpdateAssessmentRequest(
        @NotBlank String title,
        @Size(max = 2000) String description,
        @NotNull UUID skillId,
        @NotNull Level level,
        @NotNull @Min(1) Integer durationMinutes,
        @NotNull @Min(0) @Max(100) Integer passingScore
) {}