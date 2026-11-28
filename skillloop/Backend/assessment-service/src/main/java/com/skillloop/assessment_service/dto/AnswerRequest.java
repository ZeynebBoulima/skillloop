package com.skillloop.assessment_service.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public record AnswerRequest(@NotNull UUID questionId, @NotNull Set<UUID> selectedOptionIds) {}