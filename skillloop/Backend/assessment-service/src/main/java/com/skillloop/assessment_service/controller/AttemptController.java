package com.skillloop.assessment_service.controller;

import com.skillloop.assessment_service.dto.*;
import com.skillloop.assessment_service.service.AttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AttemptController {

    private final AttemptService service;

    @PostMapping("/assessments/{assessmentId}/attempts")
    @ResponseStatus(HttpStatus.CREATED)
    public AttemptStartResponse start(@PathVariable UUID assessmentId,
                                      @Valid @RequestBody StartAttemptRequest request) {
        return service.start(assessmentId, request);
    }

    @PostMapping("/attempts/{attemptId}/submit")
    public AttemptResultResponse submit(@PathVariable UUID attemptId,
                                        @Valid @RequestBody SubmitAttemptRequest request) {
        return service.submit(attemptId, request);
    }

    @GetMapping("/attempts/{attemptId}")
    public AttemptResultResponse get(@PathVariable UUID attemptId) {
        return service.getAttempt(attemptId);
    }

    @GetMapping("/students/{studentId}/attempts")
    public List<AttemptResultResponse> attempts(@PathVariable UUID studentId) {
        return service.listAttempts(studentId);
    }

    @GetMapping("/students/{studentId}/certifications")
    public List<CertificationResponse> certifications(@PathVariable UUID studentId) {
        return service.listCertifications(studentId);
    }

    @GetMapping("/certifications/{code}")
    public CertificationResponse certification(@PathVariable String code) {
        return service.getCertification(code);
    }
}