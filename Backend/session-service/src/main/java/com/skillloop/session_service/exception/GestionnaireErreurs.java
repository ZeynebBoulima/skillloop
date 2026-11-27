package com.skillloop.session_service.exception;

import com.skillloop.session_service.dto.ErreurResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GestionnaireErreurs {

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<ErreurResponse> introuvable(RessourceIntrouvableException e) {
        return reponse(HttpStatus.NOT_FOUND, e.getMessage(), List.of());
    }

    // Transition d'état interdite : 409
    @ExceptionHandler(TransitionInvalideException.class)
    public ResponseEntity<ErreurResponse> transition(TransitionInvalideException e) {
        return reponse(HttpStatus.CONFLICT, e.getMessage(), List.of());
    }

    // Créneau déjà pris : 409
    @ExceptionHandler(ConflitCreneauException.class)
    public ResponseEntity<ErreurResponse> conflit(ConflitCreneauException e) {
        return reponse(HttpStatus.CONFLICT, e.getMessage(), List.of());
    }

    // Autre règle métier non respectée : 422
    @ExceptionHandler(RegleMetierException.class)
    public ResponseEntity<ErreurResponse> regle(RegleMetierException e) {
        return reponse(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), List.of());
    }

    // Validation des champs (@Valid) : 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurResponse> validation(MethodArgumentNotValidException e) {
        List<String> details = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " : " + f.getDefaultMessage())
                .toList();
        return reponse(HttpStatus.BAD_REQUEST, "Données invalides", details);
    }

    // JSON mal formé ou date illisible : 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErreurResponse> jsonInvalide(HttpMessageNotReadableException e) {
        return reponse(HttpStatus.BAD_REQUEST, "Corps de la requête illisible", List.of());
    }

    private ResponseEntity<ErreurResponse> reponse(HttpStatus statut, String message, List<String> details) {
        return ResponseEntity.status(statut)
                .body(ErreurResponse.de(statut.value(), statut.getReasonPhrase(), message, details));
    }
}
