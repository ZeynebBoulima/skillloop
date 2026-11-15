package com.skillloop.identity_service.dto;

import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        String status,
        Set<String> roles
) {}