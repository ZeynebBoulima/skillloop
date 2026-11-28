package com.skillloop.assessment_service.service;

import com.skillloop.assessment_service.dto.*;
import com.skillloop.assessment_service.entity.*;
import com.skillloop.assessment_service.repository.AnswerOptionRepository;
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
    private final AnswerOptionRepository answerOptionRepository;

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
        assessment.getQuestions().forEach(this::requireReadyToPublish);
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

    // ---------- Choix de réponse ----------

    @Transactional
    public OptionResponse addOption(UUID assessmentId, UUID questionId, OptionRequest request) {
        Assessment assessment = find(assessmentId);
        requireDraft(assessment);
        Question question = findQuestion(assessment, questionId);

        int max = maxOptions(question);
        if (question.getOptions().size() >= max) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Nombre maximal de choix atteint (" + max + ")");
        }
        checkSingleCorrect(question, request.correct(), null);

        AnswerOption option = AnswerOption.builder()
                .text(request.text().trim())
                .correct(request.correct())
                .position(question.getOptions().size() + 1)
                .build();
        question.addOption(option);
        return toResponse(answerOptionRepository.save(option));
    }

    @Transactional
    public OptionResponse updateOption(UUID assessmentId, UUID questionId, UUID optionId, OptionRequest request) {
        Assessment assessment = find(assessmentId);
        requireDraft(assessment);
        Question question = findQuestion(assessment, questionId);
        AnswerOption option = findOption(question, optionId);
        checkSingleCorrect(question, request.correct(), optionId);

        option.setText(request.text().trim());
        option.setCorrect(request.correct());
        return toResponse(option);
    }

    @Transactional
    public void deleteOption(UUID assessmentId, UUID questionId, UUID optionId) {
        Assessment assessment = find(assessmentId);
        requireDraft(assessment);
        Question question = findQuestion(assessment, questionId);
        question.removeOption(findOption(question, optionId));

        int position = 1;
        for (AnswerOption o : question.getOptions()) {
            o.setPosition(position++);
        }
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

    private AnswerOption findOption(Question question, UUID optionId) {
        return question.getOptions().stream()
                .filter(o -> o.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Choix introuvable"));
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

    private int maxOptions(Question question) {
        return question.getType() == QuestionType.VRAI_FAUX ? 2 : 6;
    }

    private void checkSingleCorrect(Question question, boolean isCorrect, UUID excludedOptionId) {
        if (!isCorrect || question.getType() == QuestionType.QCM_MULTIPLE) {
            return;
        }
        boolean alreadyCorrect = question.getOptions().stream()
                .anyMatch(o -> o.isCorrect() && !o.getId().equals(excludedOptionId));
        if (alreadyCorrect) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cette question n'accepte qu'une seule bonne réponse");
        }
    }

    private void requireReadyToPublish(Question question) {
        int n = question.getOrderIndex();
        long correct = question.getOptions().stream().filter(AnswerOption::isCorrect).count();

        if (question.getOptions().size() < 2) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La question n°" + n + " doit avoir au moins 2 choix");
        }
        if (question.getType() == QuestionType.VRAI_FAUX && question.getOptions().size() != 2) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La question n°" + n + " (vrai/faux) doit avoir exactement 2 choix");
        }
        if (question.getType() != QuestionType.QCM_MULTIPLE && correct != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La question n°" + n + " doit avoir exactement une bonne réponse");
        }
        if (question.getType() == QuestionType.QCM_MULTIPLE && correct < 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La question n°" + n + " doit avoir au moins une bonne réponse");
        }
    }

    // ---------- Conversions vers les DTO ----------

    private OptionResponse toResponse(AnswerOption o) {
        return new OptionResponse(o.getId(), o.getText(), o.isCorrect(), o.getPosition());
    }

    private QuestionResponse toResponse(Question q) {
        return new QuestionResponse(q.getId(), q.getStatement(), q.getType(),
                q.getDifficulty(), q.getOrderIndex(), q.getPoints(),
                q.getOptions().stream().map(this::toResponse).toList());
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