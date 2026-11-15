package com.skillloop.assessment_service.repository;

import com.skillloop.assessment_service.entity.Assessment;
import com.skillloop.assessment_service.entity.AssessmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {
    List<Assessment> findBySkillIdAndStatus(UUID skillId, AssessmentStatus status);
}