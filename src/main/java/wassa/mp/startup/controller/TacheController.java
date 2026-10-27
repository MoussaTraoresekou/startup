
package wassa.mp.startup.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.dto.TacheCreationDto;
import wassa.mp.startup.dto.TacheRenduDto;
import wassa.mp.startup.dto.TacheResponseDto;
import wassa.mp.startup.dto.TacheVerificationDto;
import wassa.mp.startup.service.TacheService;

@RestController
@RequestMapping("/api/taches")
@SecurityRequirement(name = "bearerAuth")

public class TacheController {

    @Autowired
    private TacheService tacheService;

    // Créer une tâche
    @PostMapping("/projets/{projetId}")
    public ResponseEntity<TacheResponseDto> creerTache(
            @PathVariable int projetId,
            @Valid @RequestBody TacheCreationDto dto,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {

        TacheResponseDto tache = tacheService.creerTache(
                projetId, dto, userConnecter
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(tache);
    }

    // Consulter les tâches d'un projet
    @GetMapping("/projets/{projetId}")
    public ResponseEntity<List<TacheResponseDto>> obtenirTachesProjet(
            @PathVariable int projetId,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {

        return ResponseEntity.ok(
                tacheService.obtenirTachesProjet(projetId, userConnecter)
        );
    }

    // Soumettre le rendu d'une tâche
    @PutMapping("/{tacheId}/soumettre")
    public ResponseEntity<TacheResponseDto> soumettreRendu(
            @PathVariable int tacheId,
            @RequestBody TacheRenduDto dto,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {

        return ResponseEntity.ok(
                tacheService.soumettreRendu(tacheId, dto, userConnecter)
        );
    }

    // Vérifier le rendu d'une tâche
    @PutMapping("/{tacheId}/verifier")
    public ResponseEntity<TacheResponseDto> verifierRendu(
            @PathVariable int tacheId,
            @RequestBody TacheVerificationDto dto,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {

        return ResponseEntity.ok(
                tacheService.verifierRendu(tacheId, dto, userConnecter)
        );
    }
}
