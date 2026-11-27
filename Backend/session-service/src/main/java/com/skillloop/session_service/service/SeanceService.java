package com.skillloop.session_service.service;

import com.skillloop.session_service.dto.DemandeSeanceRequest;
import com.skillloop.session_service.dto.SeanceResponse;
import com.skillloop.session_service.entity.EtatSeance;
import com.skillloop.session_service.entity.Seance;
import com.skillloop.session_service.exception.ConflitCreneauException;
import com.skillloop.session_service.exception.RessourceIntrouvableException;
import com.skillloop.session_service.repository.SeanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class SeanceService {

    // Les séances dans ces états bloquent réellement le créneau du mentor
    private static final Set<EtatSeance> ETATS_BLOQUANTS =
            EnumSet.of(EtatSeance.ACCEPTEE, EtatSeance.CONFIRMEE, EtatSeance.EN_COURS);

    private final SeanceRepository seanceRepository;

    public SeanceResponse demander(DemandeSeanceRequest r) {
        // La création valide déjà : demandeur différent du mentor, fin après début
        Seance seance = Seance.demander(r.demandeurId(), r.mentorId(), r.titre(),
                r.description(), r.debut(), r.fin());
        verifierCreneauLibre(seance);
        return SeanceResponse.de(seanceRepository.save(seance));
    }

    @Transactional(readOnly = true)
    public SeanceResponse consulter(Long id) {
        return SeanceResponse.de(trouver(id));
    }

    @Transactional(readOnly = true)
    public List<SeanceResponse> lister(Long utilisateurId) {
        return seanceRepository.findByDemandeurIdOrMentorId(utilisateurId, utilisateurId)
                .stream().map(SeanceResponse::de).toList();
    }

    public SeanceResponse accepter(Long id) {
        Seance seance = trouver(id);
        verifierCreneauLibre(seance);   // protège contre deux acceptations qui se chevauchent
        seance.accepter();
        return SeanceResponse.de(seance);
    }

    public SeanceResponse confirmer(Long id) {
        Seance seance = trouver(id);
        seance.confirmer();             // la Saga (crédits, visio) sera ajoutée plus tard
        return SeanceResponse.de(seance);
    }

    public SeanceResponse demarrer(Long id) {
        Seance seance = trouver(id);
        seance.demarrer();
        return SeanceResponse.de(seance);
    }

    public SeanceResponse annuler(Long id) {
        Seance seance = trouver(id);
        seance.annuler();
        return SeanceResponse.de(seance);
    }

    public SeanceResponse enregistrerPresence(Long id, Long utilisateurId) {
        Seance seance = trouver(id);
        seance.enregistrerPresence(utilisateurId);
        return SeanceResponse.de(seance);
    }

    public SeanceResponse terminer(Long id) {
        Seance seance = trouver(id);
        seance.terminer();
        return SeanceResponse.de(seance);
    }

    private Seance trouver(Long id) {
        return seanceRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Séance introuvable : " + id));
    }

    private void verifierCreneauLibre(Seance seance) {
        boolean conflit = seanceRepository.existeConflitPourMentor(
                seance.getMentorId(),
                seance.getCreneau().getDebut(),
                seance.getCreneau().getFin(),
                ETATS_BLOQUANTS);
        if (conflit) {
            throw new ConflitCreneauException("Le mentor a déjà une séance sur ce créneau");
        }
    }
}
