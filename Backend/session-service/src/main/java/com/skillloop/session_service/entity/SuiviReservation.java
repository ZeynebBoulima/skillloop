package com.skillloop.session_service.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "suivis_reservation")
@Getter @Setter @NoArgsConstructor
public class SuiviReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String identifiantSuivi = UUID.randomUUID().toString();

    // CRENEAU_VERROUILLE, CREDITS_GELES, SALLE_CREEE...
    @Column(nullable = false, length = 50)
    private String etapeActuelle = "DEMARRE";

    // DEMARRE, TERMINE, COMPENSATION ou ECHEC
    @Column(nullable = false, length = 20)
    private String etat = "DEMARRE";

    @Column(name = "date_mise_a_jour", nullable = false)
    private LocalDateTime dateMiseAJour = LocalDateTime.now();
}