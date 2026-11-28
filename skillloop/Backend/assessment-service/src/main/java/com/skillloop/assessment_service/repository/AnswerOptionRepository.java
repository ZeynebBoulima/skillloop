package com.skillloop.assessment_service.repository;

import com.skillloop.assessment_service.entity.AnswerOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnswerOptionRepository extends JpaRepository<AnswerOption, UUID> {
}