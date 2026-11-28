package com.skillloop.assessment_service.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StartAttemptRequest(@NotNull UUID studentId) {}