package com.skillloop.identity_service.dto;

import com.skillloop.identity_service.entity.CommunityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCommunityRequest(@NotBlank String name, @NotNull CommunityType type) {}