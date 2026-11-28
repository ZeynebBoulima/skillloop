package com.skillloop.assessment_service.service;

import com.skillloop.assessment_service.dto.*;
import com.skillloop.assessment_service.entity.*;
import com.skillloop.assessment_service.repository.AssessmentRepository;
import com.skillloop.assessment_service.repository.AttemptRepository;
import com.skillloop.assessment_service.repository.CertificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttemptService {

    private static final int MAX_ATTEMPTS = 3;
    private static final long GRACE_SECONDS = 30;

    private final AssessmentRepository assessmentRepository;
    private final AttemptRepository attemptRepository;
    private final CertificationRepository certificationRepository;

    // ---------- Démarrer une tentative ----------

    @Transactional
    public AttemptStartResponse start(UUID assessmentId, StartAttemptRequest request) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Évaluation introuvable"));
        if (assessment.getStatus() != AssessmentStatus.PUBLIE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cette évaluation n'est pas ouverte aux candidats");
        }

        UUID studentId = request.studentId();
        if (certificationRepository.existsByAssessmentIdAndStudentId(assessmentId, studentId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Vous êtes déjà certifié pour cette évaluation");
        }

        Instant now = Instant.now();
        List<Attempt> running = attemptRepository
                .findByAssessmentIdAndStudentIdAndStatus(assessmentId, studentId, AttemptStatus.EN_COURS);
        for (Attempt old : running) {
            if (isExpired(old, now)) {
                old.setStatus(AttemptStatus.EXPIREE);
            }
        }
        boolean stillRunning = running.stream().anyMatch(a -> a.getStatus() == AttemptStatus.EN_COURS);
        if (stillRunning) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Une tentative est déjà en cours");
        }

        if (attemptRepository.countByAssessmentIdAndStudentId(assessmentId, studentId) >= MAX_ATTEMPTS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Nombre maximal de tentatives atteint (" + MAX_ATTEMPTS + ")");
        }

        Attempt attempt = Attempt.builder()
                .assessment(assessment)
                .studentId(studentId)
                .startedAt(now)
                .expiresAt(now.plus(assessment.getDurationMinutes(), ChronoUnit.MINUTES))
                .build();
        attemptRepository.save(attempt);
        return toStartResponse(attempt);
    }

    // ---------- Soumettre et corriger ----------

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public AttemptResultResponse submit(UUID attemptId, SubmitAttemptRequest request) {
        Attempt attempt = findAttempt(attemptId);
        if (attempt.getStatus() != AttemptStatus.EN_COURS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette tentative est déjà terminée");
        }

        Instant now = Instant.now();
        if (isExpired(attempt, now)) {
            attempt.setStatus(AttemptStatus.EXPIREE);
            attempt.setSubmittedAt(now);
            throw new ResponseStatusException(HttpStatus.GONE, "Temps écoulé : la tentative a expiré");
        }

        Assessment assessment = attempt.getAssessment();
        Map<UUID, AnswerRequest> answers = validateAnswers(assessment, request);

        int earned = 0;
        int total = 0;
        for (Question question : assessment.getQuestions()) {
            total += question.getPoints();

            AnswerRequest answer = answers.get(question.getId());
            Set<UUID> selected = answer != null ? answer.selectedOptionIds() : Set.of();
            Set<UUID> correctIds = question.getOptions().stream()
                    .filter(AnswerOption::isCorrect)
                    .map(AnswerOption::getId)
                    .collect(Collectors.toSet());

            boolean correct = !selected.isEmpty() && selected.equals(correctIds);
            int points = correct ? question.getPoints() : 0;
            earned += points;

            attempt.addResponse(StudentResponse.builder()
                    .question(question)
                    .selectedOptionIds(new HashSet<>(selected))
                    .correct(correct)
                    .pointsAwarded(points)
                    .build());
        }

        int score = total == 0 ? 0 : (int) Math.round(earned * 100.0 / total);
        boolean passed = score >= assessment.getPassingScore();

        attempt.setStatus(AttemptStatus.SOUMISE);
        attempt.setSubmittedAt(now);
        attempt.setEarnedPoints(earned);
        attempt.setTotalPoints(total);
        attempt.setScore(score);
        attempt.setPassed(passed);

        Certification certification = null;
        if (passed) {
            certification = certificationRepository.save(Certification.builder()
                    .attempt(attempt)
                    .assessment(assessment)
                    .studentId(attempt.getStudentId())
                    .skillId(assessment.getSkillId())
                    .level(assessment.getLevel())
                    .score(score)
                    .code(newCode())
                    .build());
        }
        return toResult(attempt, certification, true);
    }

    // ---------- Consultation ----------

    @Transactional(readOnly = true)
    public AttemptResultResponse getAttempt(UUID attemptId) {
        Attempt attempt = findAttempt(attemptId);
        Certification certification = certificationRepository.findByAttemptId(attemptId).orElse(null);
        return toResult(attempt, certification, true);
    }

    @Transactional(readOnly = true)
    public List<AttemptResultResponse> listAttempts(UUID studentId) {
        return attemptRepository.findByStudentIdOrderByStartedAtDesc(studentId).stream()
                .map(a -> toResult(a, certificationRepository.findByAttemptId(a.getId()).orElse(null), false))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CertificationResponse> listCertifications(UUID studentId) {
        return certificationRepository.findByStudentIdOrderByIssuedAtDesc(studentId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CertificationResponse getCertification(String code) {
        return certificationRepository.findByCode(code)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certification introuvable"));
    }

    // ---------- Outils internes ----------

    private Attempt findAttempt(UUID id) {
        return attemptRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tentative introuvable"));
    }

    private boolean isExpired(Attempt attempt, Instant now) {
        return now.isAfter(attempt.getExpiresAt().plusSeconds(GRACE_SECONDS));
    }

    private String newCode() {
        return "CERT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    private Map<UUID, AnswerRequest> validateAnswers(Assessment assessment, SubmitAttemptRequest request) {
        Map<UUID, Question> questions = assessment.getQuestions().stream()
                .collect(Collectors.toMap(Question::getId, q -> q));
        Map<UUID, AnswerRequest> answers = new HashMap<>();

        for (AnswerRequest answer : request.answers()) {
            Question question = questions.get(answer.questionId());
            if (question == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Question inconnue pour cette évaluation : " + answer.questionId());
            }
            if (answers.put(question.getId(), answer) != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Plusieurs réponses pour la question n°" + question.getOrderIndex());
            }
            Set<UUID> validIds = question.getOptions().stream()
                    .map(AnswerOption::getId)
                    .collect(Collectors.toSet());
            if (!validIds.containsAll(answer.selectedOptionIds())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Choix invalide pour la question n°" + question.getOrderIndex());
            }
            if (question.getType() != QuestionType.QCM_MULTIPLE && answer.selectedOptionIds().size() > 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Un seul choix est autorisé pour la question n°" + question.getOrderIndex());
            }
        }
        return answers;
    }

    // ---------- Conversions vers les DTO ----------

    private AttemptStartResponse toStartResponse(Attempt attempt) {
        Assessment a = attempt.getAssessment();
        List<QuestionStudentView> questions = a.getQuestions().stream()
                .map(q -> new QuestionStudentView(
                        q.getId(), q.getStatement(), q.getType(), q.getOrderIndex(), q.getPoints(),
                        q.getOptions().stream()
                                .map(o -> new OptionStudentView(o.getId(), o.getText(), o.getPosition()))
                                .toList()))
                .toList();
        return new AttemptStartResponse(attempt.getId(), a.getId(), a.getTitle(),
                attempt.getStartedAt(), attempt.getExpiresAt(), a.getDurationMinutes(), questions);
    }

    private AttemptResultResponse toResult(Attempt attempt, Certification certification, boolean withDetails) {
        List<QuestionResultResponse> details = withDetails
                ? attempt.getResponses().stream()
                .sorted(Comparator.comparingInt(r -> r.getQuestion().getOrderIndex()))
                .map(r -> new QuestionResultResponse(
                        r.getQuestion().getId(), r.getQuestion().getOrderIndex(),
                        r.isCorrect(), r.getPointsAwarded(), r.getQuestion().getPoints()))
                .toList()
                : List.of();
        return new AttemptResultResponse(
                attempt.getId(), attempt.getAssessment().getId(), attempt.getStudentId(),
                attempt.getStatus(), attempt.getScore(), attempt.getEarnedPoints(),
                attempt.getTotalPoints(), attempt.getPassed(),
                attempt.getAssessment().getPassingScore(),
                attempt.getStartedAt(), attempt.getSubmittedAt(), details,
                certification != null ? toResponse(certification) : null);
    }

    private CertificationResponse toResponse(Certification c) {
        return new CertificationResponse(c.getId(), c.getCode(), c.getAssessment().getId(),
                c.getStudentId(), c.getSkillId(), c.getLevel(), c.getScore(),
                c.getIssuedAt(), c.getStatus());
    }
}