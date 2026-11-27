package com.skillloop.session_service.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "places_attente")
@Getter @Setter @NoArgsConstructor
public class PlaceEnAttente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long utilisateurId;

    @Column(nullable = false)
    private LocalDateTime dateAjout = LocalDateTime.now();

    @Column(nullable = false)
    private Integer rang;
}