package com.skillloop.session_service.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record DemandeSeanceRequest(
        @NotNull Long demandeurId,
        @NotNull Long mentorId,
        @NotBlank @Size(max = 150) String titre,
        @Size(max = 1000) String description,
        @NotNull @Future LocalDateTime debut,
        @NotNull LocalDateTime fin
) {}
