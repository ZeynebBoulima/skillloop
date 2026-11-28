package com.skillloop.assessment_service.controller;

import com.skillloop.assessment_service.dto.*;
import com.skillloop.assessment_service.entity.AssessmentStatus;
import com.skillloop.assessment_service.service.AssessmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/assessments")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssessmentResponse create(@Valid @RequestBody CreateAssessmentRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<AssessmentSummaryResponse> list(@RequestParam(required = false) UUID skillId,
                                                @RequestParam(required = false) AssessmentStatus status) {
        return service.list(skillId, status);
    }

    @GetMapping("/{id}")
    public AssessmentResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public AssessmentResponse update(@PathVariable UUID id,
                                     @Valid @RequestBody UpdateAssessmentRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @PostMapping("/{id}/publish")
    public AssessmentResponse publish(@PathVariable UUID id) {
        return service.publish(id);
    }

    @PostMapping("/{id}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionResponse addQuestion(@PathVariable UUID id,
                                        @Valid @RequestBody QuestionRequest request) {
        return service.addQuestion(id, request);
    }

    @PutMapping("/{id}/questions/{questionId}")
    public QuestionResponse updateQuestion(@PathVariable UUID id,
                                           @PathVariable UUID questionId,
                                           @Valid @RequestBody QuestionRequest request) {
        return service.updateQuestion(id, questionId, request);
    }

    @DeleteMapping("/{id}/questions/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuestion(@PathVariable UUID id, @PathVariable UUID questionId) {
        service.deleteQuestion(id, questionId);
    }
    @PostMapping("/{id}/questions/{questionId}/options")
    @ResponseStatus(HttpStatus.CREATED)
    public OptionResponse addOption(@PathVariable UUID id,
                                    @PathVariable UUID questionId,
                                    @Valid @RequestBody OptionRequest request) {
        return service.addOption(id, questionId, request);
    }

    @PutMapping("/{id}/questions/{questionId}/options/{optionId}")
    public OptionResponse updateOption(@PathVariable UUID id,
                                       @PathVariable UUID questionId,
                                       @PathVariable UUID optionId,
                                       @Valid @RequestBody OptionRequest request) {
        return service.updateOption(id, questionId, optionId, request);
    }

    @DeleteMapping("/{id}/questions/{questionId}/options/{optionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOption(@PathVariable UUID id,
                             @PathVariable UUID questionId,
                             @PathVariable UUID optionId) {
        service.deleteOption(id, questionId, optionId);
    }
}