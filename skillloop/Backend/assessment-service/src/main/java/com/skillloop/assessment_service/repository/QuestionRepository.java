package com.skillloop.assessment_service.repository;

import com.skillloop.assessment_service.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
}