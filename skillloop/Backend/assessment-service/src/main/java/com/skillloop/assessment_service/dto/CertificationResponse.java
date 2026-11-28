package com.skillloop.assessment_service.dto;

import com.skillloop.assessment_service.entity.CertificationStatus;
import com.skillloop.assessment_service.entity.Level;

import java.time.Instant;
import java.util.UUID;

public record CertificationResponse(
        UUID id,
        String code,
        UUID assessmentId,
        UUID studentId,
        UUID skillId,
        Level level,
        int score,
        Instant issuedAt,
        CertificationStatus status
) {}