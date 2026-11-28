package com.skillloop.assessment_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SubmitAttemptRequest(@NotNull @Valid List<@Valid AnswerRequest> answers) {}