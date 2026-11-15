package com.skillloop.identity_service.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record UpdateProfileRequest(
        @NotBlank String fullName,
        Set<String> languages,
        @NotBlank String timezone
) {}