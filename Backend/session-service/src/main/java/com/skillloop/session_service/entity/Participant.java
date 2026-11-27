package com.skillloop.session_service.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "participants")
@Getter
@Setter
@NoArgsConstructor
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long utilisateurId;

    // DEMANDEUR, MENTOR ou INVITE
    @Column(nullable = false, length = 20)
    private String role;

    @Column(nullable = false)
    private Boolean present = false;

    public Participant(Long utilisateurId, String role) {
        this.utilisateurId = utilisateurId;
        this.role = role;
    }
}