package com.skillloop.assessment_service.service;

import com.skillloop.assessment_service.dto.*;
import com.skillloop.assessment_service.entity.*;
import com.skillloop.assessment_service.repository.AssessmentRepository;
import com.skillloop.assessment_service.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final QuestionRepository questionRepository;

    // ---------- Évaluations ----------

    @Transactional
    public AssessmentResponse create(CreateAssessmentRequest request) {
        Assessment assessment = Assessment.builder()
                .title(request.title().trim())
                .description(request.description())
                .skillId(request.skillId())
                .level(request.level())
                .durationMinutes(request.durationMinutes())
                .passingScore(request.passingScore())
                .createdBy(request.createdBy())
                .build();
        return toResponse(assessmentRepository.save(assessment));
    }

    @Transactional(readOnly = true)
    public List<AssessmentSummaryResponse> list(UUID skillId, AssessmentStatus status) {
        return assessmentRepository.findAll().stream()
                .filter(a -> skillId == null || a.getSkillId().equals(skillId))
                .filter(a -> status == null || a.getStatus() == status)
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssessmentResponse get(UUID id) {
        return toResponse(find(id));
    }

    @Transactional
    public AssessmentResponse update(UUID id, UpdateAssessmentRequest request) {
        Assessment assessment = find(id);
        requireDraft(assessment);
        assessment.setTitle(request.title().trim());
        assessment.setDescription(request.description());
        assessment.setSkillId(request.skillId());
        assessment.setLevel(request.level());
        assessment.setDurationMinutes(request.durationMinutes());
        assessment.setPassingScore(request.passingScore());
        return toResponse(assessment);
    }

    @Transactional
    public void delete(UUID id) {
        Assessment assessment = find(id);
        requireDraft(assessment);
        assessmentRepository.delete(assessment);
    }

    @Transactional
    public AssessmentResponse publish(UUID id) {
        Assessment assessment = find(id);
        requireDraft(assessment);
        if (assessment.getQuestions().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Impossible de publier une évaluation sans question");
        }
        assessment.setStatus(AssessmentStatus.PUBLIE);
        return toResponse(assessment);
    }

    // ---------- Questions ----------

    @Transactional
    public QuestionResponse addQuestion(UUID assessmentId, QuestionRequest request) {
        Assessment assessment = find(assessmentId);
        requireDraft(assessment);
        checkOrderFree(assessment, request.orderIndex(), null);

        Question question = Question.builder()
                .statement(request.statement().trim())
                .type(request.type())
                .difficulty(request.difficulty())
                .orderIndex(request.orderIndex())
                .points(request.points() != null ? request.points() : 1)
                .build();
        assessment.addQuestion(question);
        return toResponse(questionRepository.save(question));
    }

    @Transactional
    public QuestionResponse updateQuestion(UUID assessmentId, UUID questionId, QuestionRequest request) {
        Assessment assessment = find(assessmentId);
        requireDraft(assessment);
        Question question = findQuestion(assessment, questionId);
        checkOrderFree(assessment, request.orderIndex(), questionId);

        question.setStatement(request.statement().trim());
        question.setType(request.type());
        question.setDifficulty(request.difficulty());
        question.setOrderIndex(request.orderIndex());
        question.setPoints(request.points() != null ? request.points() : 1);
        return toResponse(question);
    }

    @Transactional
    public void deleteQuestion(UUID assessmentId, UUID questionId) {
        Assessment assessment = find(assessmentId);
        requireDraft(assessment);
        assessment.removeQuestion(findQuestion(assessment, questionId));
    }

    // ---------- Outils internes ----------

    private Assessment find(UUID id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Évaluation introuvable"));
    }

    private Question findQuestion(Assessment assessment, UUID questionId) {
        return assessment.getQuestions().stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question introuvable"));
    }

    private void requireDraft(Assessment assessment) {
        if (assessment.getStatus() != AssessmentStatus.BROUILLON) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Seule une évaluation en brouillon peut être modifiée ou supprimée");
        }
    }

    private void checkOrderFree(Assessment assessment, int orderIndex, UUID excludedQuestionId) {
        boolean taken = assessment.getQuestions().stream()
                .anyMatch(q -> q.getOrderIndex() == orderIndex && !q.getId().equals(excludedQuestionId));
        if (taken) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Une question occupe déjà la position " + orderIndex);
        }
    }

    private QuestionResponse toResponse(Question q) {
        return new QuestionResponse(q.getId(), q.getStatement(), q.getType(),
                q.getDifficulty(), q.getOrderIndex(), q.getPoints());
    }

    private AssessmentResponse toResponse(Assessment a) {
        return new AssessmentResponse(a.getId(), a.getTitle(), a.getDescription(), a.getSkillId(),
                a.getLevel(), a.getDurationMinutes(), a.getPassingScore(), a.getStatus(),
                a.getCreatedBy(), a.getCreatedAt(),
                a.getQuestions().stream().map(this::toResponse).toList());
    }

    private AssessmentSummaryResponse toSummary(Assessment a) {
        return new AssessmentSummaryResponse(a.getId(), a.getTitle(), a.getSkillId(), a.getLevel(),
                a.getStatus(), a.getDurationMinutes(), a.getPassingScore(), a.getQuestions().size());
    }
}