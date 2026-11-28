package com.skillloop.assessment_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OptionRequest(
        @NotBlank @Size(max = 1000) String text,
        @NotNull Boolean correct
) {}