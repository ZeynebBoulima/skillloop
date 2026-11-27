package com.skillloop.session_service.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rappels")
@Getter @Setter @NoArgsConstructor
public class Rappel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateEnvoiPrevue;

    // EN_ATTENTE, ENVOYE ou ECHEC
    @Column(nullable = false, length = 20)
    private String etat = "EN_ATTENTE";
}