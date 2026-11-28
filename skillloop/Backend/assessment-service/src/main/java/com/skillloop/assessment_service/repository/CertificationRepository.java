package com.skillloop.assessment_service.repository;

import com.skillloop.assessment_service.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CertificationRepository extends JpaRepository<Certification, UUID> {
    Optional<Certification> findByCode(String code);
    Optional<Certification> findByAttemptId(UUID attemptId);
    List<Certification> findByStudentIdOrderByIssuedAtDesc(UUID studentId);
    boolean existsByAssessmentIdAndStudentId(UUID assessmentId, UUID studentId);
}