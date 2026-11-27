package com.skillloop.session_service.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "creneaux")
@Getter @Setter @NoArgsConstructor
public class Creneau {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime debut;

    @Column(nullable = false)
    private LocalDateTime fin;

    // RESERVE ou LIBERE
    @Column(nullable = false, length = 20)
    private String etat = "RESERVE";
}