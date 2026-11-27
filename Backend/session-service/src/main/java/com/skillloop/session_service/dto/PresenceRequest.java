package com.skillloop.session_service.dto;

import jakarta.validation.constraints.NotNull;

public record PresenceRequest(@NotNull Long utilisateurId) {}
