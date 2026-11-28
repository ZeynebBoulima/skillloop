package com.skillloop.assessment_service.dto;

import java.util.UUID;

public record OptionResponse(UUID id, String text, boolean correct, int position) {}