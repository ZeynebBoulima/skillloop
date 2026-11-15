package com.skillloop.identity_service.dto;

public record AuthResponse(String accessToken, String refreshToken, String tokenType, long expiresInSeconds) {}