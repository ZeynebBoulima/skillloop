package com.skillloop.assessment_service.dto;

import java.util.UUID;

public record OptionStudentView(UUID id, String text, int position) {}