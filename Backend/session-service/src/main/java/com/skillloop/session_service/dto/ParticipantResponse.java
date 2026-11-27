package com.skillloop.session_service.dto;

import com.skillloop.session_service.entity.Participant;

public record ParticipantResponse(Long utilisateurId, String role, boolean present) {

    public static ParticipantResponse de(Participant p) {
        return new ParticipantResponse(p.getUtilisateurId(), p.getRole(), Boolean.TRUE.equals(p.getPresent()));
    }
}
