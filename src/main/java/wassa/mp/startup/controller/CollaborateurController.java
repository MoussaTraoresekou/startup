package wassa.mp.startup.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.dto.CandidatureRequestDto;
import wassa.mp.startup.dto.CandidatureResponseDto;
import wassa.mp.startup.dto.CollaborateurRequestDto;
import wassa.mp.startup.dto.CollaborateurResponseDto;
import wassa.mp.startup.dto.OffresResponseDto;
import wassa.mp.startup.service.CollaborateurService;
import wassa.mp.startup.service.OffresService;

import java.util.List;

@RestController
@RequestMapping("/api/collaborateur")
@SecurityRequirement(name = "bearerAuth")
public class CollaborateurController {

    @Autowired
    private CollaborateurService collaborateurService;

    @Autowired
    private OffresService offresService;

    @PostMapping("/register")
    public ResponseEntity<CollaborateurResponseDto> inscrireNouveauCollaborateur(
            @Valid @RequestBody CollaborateurRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(collaborateurService.inscrireCollaborateur(dto));
    }

    @PostMapping("/offres/postuler")
    public ResponseEntity<CandidatureResponseDto> postulerOffre(
            @Valid @RequestBody CandidatureRequestDto dto,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(offresService.postulerAUneOffre(dto, userConnecter));
    }

    @GetMapping("/offres/ouvertes")
    public ResponseEntity<List<OffresResponseDto>> obtenirOffresOuvertes() {
        return ResponseEntity.ok(offresService.obtenirToutesLesOffresOuvertes());
    }
}
