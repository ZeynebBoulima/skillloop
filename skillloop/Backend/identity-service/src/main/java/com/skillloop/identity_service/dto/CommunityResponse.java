package com.skillloop.identity_service.dto;

import java.util.UUID;

public record CommunityResponse(UUID id, String name, String type, int memberCount) {}