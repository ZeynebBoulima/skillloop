package com.skillloop.session_service.repository;

import com.skillloop.session_service.entity.EtatSeance;
import com.skillloop.session_service.entity.Seance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface SeanceRepository extends JpaRepository<Seance, Long> {

    List<Seance> findByDemandeurIdOrMentorId(Long demandeurId, Long mentorId);

    List<Seance> findByEtat(EtatSeance etat);

    // Le mentor a-t-il déjà une séance qui chevauche ce créneau ?
    @Query("""
        select case when count(s) > 0 then true else false end
        from Seance s
        where s.mentorId = :mentorId
          and s.etat in :etats
          and s.creneau.debut < :fin
          and s.creneau.fin > :debut
        """)
    boolean existeConflitPourMentor(@Param("mentorId") Long mentorId,
                                    @Param("debut") LocalDateTime debut,
                                    @Param("fin") LocalDateTime fin,
                                    @Param("etats") Collection<EtatSeance> etats);
}