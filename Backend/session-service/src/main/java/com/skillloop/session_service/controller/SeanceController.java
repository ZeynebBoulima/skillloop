package com.skillloop.session_service.controller;

import com.skillloop.session_service.dto.DemandeSeanceRequest;
import com.skillloop.session_service.dto.PresenceRequest;
import com.skillloop.session_service.dto.SeanceResponse;
import com.skillloop.session_service.service.SeanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/seances")
@RequiredArgsConstructor
public class SeanceController {

    private final SeanceService service;

    @PostMapping
    public ResponseEntity<SeanceResponse> demander(@Valid @RequestBody DemandeSeanceRequest requete) {
        SeanceResponse creee = service.demander(requete);
        return ResponseEntity.created(URI.create("/api/seances/" + creee.id())).body(creee);
    }

    @GetMapping("/{id}")
    public SeanceResponse consulter(@PathVariable Long id) {
        return service.consulter(id);
    }

    @GetMapping
    public List<SeanceResponse> lister(@RequestParam Long utilisateurId) {
        return service.lister(utilisateurId);
    }

    @PostMapping("/{id}/accepter")
    public SeanceResponse accepter(@PathVariable Long id) {
        return service.accepter(id);
    }

    @PostMapping("/{id}/confirmer")
    public SeanceResponse confirmer(@PathVariable Long id) {
        return service.confirmer(id);
    }

    @PostMapping("/{id}/demarrer")
    public SeanceResponse demarrer(@PathVariable Long id) {
        return service.demarrer(id);
    }

    @PostMapping("/{id}/presence")
    public SeanceResponse presence(@PathVariable Long id, @Valid @RequestBody PresenceRequest requete) {
        return service.enregistrerPresence(id, requete.utilisateurId());
    }

    @PostMapping("/{id}/terminer")
    public SeanceResponse terminer(@PathVariable Long id) {
        return service.terminer(id);
    }

    @PostMapping("/{id}/annuler")
    public SeanceResponse annuler(@PathVariable Long id) {
        return service.annuler(id);
    }
}