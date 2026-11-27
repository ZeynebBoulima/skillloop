package com.skillloop.session_service.entity;

import com.skillloop.session_service.exception.RegleMetierException;
import com.skillloop.session_service.exception.TransitionInvalideException;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "seances")
@Getter @Setter @NoArgsConstructor
public class Seance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long demandeurId;

    @Column(nullable = false)
    private Long mentorId;

    @Column(nullable = false, length = 150)
    private String titre;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Integer capacite = 2;

    private String lienVisio;

    @Setter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EtatSeance etat = EtatSeance.DEMANDEE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    // Compositions : supprimés avec la séance
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "creneau_id", nullable = false)
    private Creneau creneau;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "seance_id", nullable = false)
    private List<Participant> participants = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "seance_id", nullable = false)
    private List<PlaceEnAttente> listeAttente = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "seance_id", nullable = false)
    private List<Rappel> rappels = new ArrayList<>();

    // 0..1 : peut ne pas exister
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "suivi_reservation_id")
    private SuiviReservation suiviReservation;

    @PrePersist
    void aLaCreation() {
        this.dateCreation = LocalDateTime.now();
    }
    // Création d'une séance avec ses participants et son créneau
    public static Seance demander(Long demandeurId, Long mentorId, String titre,
                                  String description, LocalDateTime debut, LocalDateTime fin) {
        if (demandeurId.equals(mentorId)) {
            throw new RegleMetierException("Le demandeur et le mentor doivent être différents");
        }
        if (!fin.isAfter(debut)) {
            throw new RegleMetierException("La fin doit être après le début");
        }
        Seance s = new Seance();
        s.setDemandeurId(demandeurId);
        s.setMentorId(mentorId);
        s.setTitre(titre);
        s.setDescription(description);

        Creneau c = new Creneau();
        c.setDebut(debut);
        c.setFin(fin);
        s.setCreneau(c);

        s.getParticipants().add(new Participant(demandeurId, "DEMANDEUR"));
        s.getParticipants().add(new Participant(mentorId, "MENTOR"));
        return s;
    }

    private void changerEtat(EtatSeance cible) {
        if (!this.etat.peutPasserA(cible)) {
            throw new TransitionInvalideException(this.etat, cible);
        }
        this.etat = cible;
    }

    public void accepter()      { changerEtat(EtatSeance.ACCEPTEE); }
    public void confirmer()     { changerEtat(EtatSeance.CONFIRMEE); }
    public void demarrer()      { changerEtat(EtatSeance.EN_COURS); }
    public void marquerAbsence(){ changerEtat(EtatSeance.ABSENCE); }
    public void ouvrirLitige()  { changerEtat(EtatSeance.LITIGE); }

    public void annuler() {
        changerEtat(EtatSeance.ANNULEE);
        this.creneau.setEtat("LIBERE");   // le créneau redevient libre
    }

    // Double check-in : chaque participant confirme sa présence
    public void enregistrerPresence(Long utilisateurId) {
        Participant p = participants.stream()
                .filter(x -> x.getUtilisateurId().equals(utilisateurId))
                .findFirst()
                .orElseThrow(() -> new RegleMetierException("Cet utilisateur ne participe pas à la séance"));
        p.setPresent(true);
    }

    // La séance n'est terminée que si tous les participants ont confirmé
    public void terminer() {
        if (this.etat == EtatSeance.EN_COURS
                && !participants.stream().allMatch(Participant::getPresent)) {
            throw new RegleMetierException("Tous les participants doivent confirmer leur présence");
        }
        changerEtat(EtatSeance.TERMINEE);
    }
}