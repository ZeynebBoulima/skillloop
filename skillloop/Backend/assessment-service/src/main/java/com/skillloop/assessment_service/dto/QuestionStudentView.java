package com.skillloop.assessment_service.dto;

import com.skillloop.assessment_service.entity.QuestionType;

import java.util.List;
import java.util.UUID;

public record QuestionStudentView(
        UUID id,
        String statement,
        QuestionType type,
        int orderIndex,
        int points,
        List<OptionStudentView> options
) {}