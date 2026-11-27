package com.skillloop.session_service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErreurResponse(
        int statut,
        String erreur,
        String message,
        List<String> details,
        LocalDateTime horodatage
) {
    public static ErreurResponse de(int statut, String erreur, String message, List<String> details) {
        return new ErreurResponse(statut, erreur, message, details, LocalDateTime.now());
    }
}
