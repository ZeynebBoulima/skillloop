package com.skillloop.session_service.dto;

import com.skillloop.session_service.entity.EtatSeance;
import com.skillloop.session_service.entity.Seance;

import java.time.LocalDateTime;
import java.util.List;

public record SeanceResponse(
        Long id,
        Long demandeurId,
        Long mentorId,
        String titre,
        String description,
        Integer capacite,
        String lienVisio,
        EtatSeance etat,
        LocalDateTime dateCreation,
        LocalDateTime debut,
        LocalDateTime fin,
        List<ParticipantResponse> participants
) {
    public static SeanceResponse de(Seance s) {
        return new SeanceResponse(
                s.getId(), s.getDemandeurId(), s.getMentorId(), s.getTitre(),
                s.getDescription(), s.getCapacite(), s.getLienVisio(), s.getEtat(),
                s.getDateCreation(), s.getCreneau().getDebut(), s.getCreneau().getFin(),
                s.getParticipants().stream().map(ParticipantResponse::de).toList());
    }
}
