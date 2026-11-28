package com.skillloop.assessment_service.repository;

import com.skillloop.assessment_service.entity.Attempt;
import com.skillloop.assessment_service.entity.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AttemptRepository extends JpaRepository<Attempt, UUID> {
    List<Attempt> findByStudentIdOrderByStartedAtDesc(UUID studentId);
    List<Attempt> findByAssessmentIdAndStudentIdAndStatus(UUID assessmentId, UUID studentId, AttemptStatus status);
    long countByAssessmentIdAndStudentId(UUID assessmentId, UUID studentId);
}