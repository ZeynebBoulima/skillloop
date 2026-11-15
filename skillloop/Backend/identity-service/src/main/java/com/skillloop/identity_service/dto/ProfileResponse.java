package com.skillloop.identity_service.dto;

import java.util.Set;
import java.util.UUID;

public record ProfileResponse(UUID userId, String fullName, Set<String> languages, String timezone) {}