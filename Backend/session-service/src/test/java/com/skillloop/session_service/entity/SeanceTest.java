package com.skillloop.session_service.entity;

import com.skillloop.session_service.exception.RegleMetierException;
import com.skillloop.session_service.exception.TransitionInvalideException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

class SeanceTest {

    private Seance nouvelleSeance() {
        LocalDateTime debut = LocalDateTime.now().plusDays(1);
        return Seance.demander(1L, 2L, "Aide en React", "Les hooks", debut, debut.plusHours(1));
    }

    @Test
    void cycleNormalJusquaTerminee() {
        Seance s = nouvelleSeance();
        s.accepter();
        s.confirmer();
        s.demarrer();
        s.enregistrerPresence(1L);
        s.enregistrerPresence(2L);
        s.terminer();
        assertEquals(EtatSeance.TERMINEE, s.getEtat());
    }

    @Test
    void impossibleDePasserDeDemandeeATerminee() {
        Seance s = nouvelleSeance();
        assertThrows(TransitionInvalideException.class, s::terminer);
    }

    @Test
    void impossibleDeTerminerSiUnParticipantEstAbsent() {
        Seance s = nouvelleSeance();
        s.accepter();
        s.confirmer();
        s.demarrer();
        s.enregistrerPresence(1L);   // le mentor n'a pas confirmé
        assertThrows(RegleMetierException.class, s::terminer);
    }

    @Test
    void annulerLibereLeCreneau() {
        Seance s = nouvelleSeance();
        s.annuler();
        assertEquals(EtatSeance.ANNULEE, s.getEtat());
        assertEquals("LIBERE", s.getCreneau().getEtat());
    }

    @Test
    void demandeurEtMentorDoiventEtreDifferents() {
        LocalDateTime debut = LocalDateTime.now().plusDays(1);
        assertThrows(RegleMetierException.class,
                () -> Seance.demander(1L, 1L, "Test", "", debut, debut.plusHours(1)));
    }
}