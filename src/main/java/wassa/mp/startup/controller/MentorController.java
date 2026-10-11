package wassa.mp.startup.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.dto.*;
import wassa.mp.startup.service.MentorProjetService;
import wassa.mp.startup.service.MentorService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mentor")
@SecurityRequirement(name = "bearerAuth")
public class MentorController {

    @Autowired
    private MentorProjetService mentorProjetService;

    @Autowired
    private MentorService mentorService;

    @PostMapping("/proposer-accompagnement")
    public ResponseEntity<String> proposerAccompagnement(
            @Valid @RequestBody DemandeMentoratDto demandeMentoratDto,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {
        mentorProjetService.mentorProposeMentorat(demandeMentoratDto, userConnecter);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Votre proposition d'accompagnement a été envoyée et notifiée au porteur de projet.");
    }

    @PutMapping("/mentor-projets/{id}/accepter")
    public ResponseEntity<Map<String, String>> accepterDemande(
            @PathVariable int id,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {
        mentorProjetService.accepterDemandeMentorat(id, userConnecter);
        return ResponseEntity.ok( Map.of("message","La demande de mentorat a été acceptée avec succès."));
    }

    @PutMapping("/mentor-projets/{id}/refuser")
    public ResponseEntity<Map<String, String>> mentorRefuserInvitation(
            @PathVariable int id,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {
        mentorProjetService.refuserDemandeMentorat(id, userConnecter);
        return ResponseEntity.ok( Map.of("message","L'invitation de mentorat a été déclinée."));
    }

    @PostMapping("/register")
    public ResponseEntity<MentorReponseDto> inscrireNouveauMentor(
            @RequestBody MentorRegisterRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mentorService.inscrireMentor(dto));
    }

    @GetMapping("/demandes")
    public ResponseEntity<List<MentorDemandeResponseDto>> obtenirDemandesMentor(
            @AuthenticationPrincipal CustumUserDetail userConnecter) {
        int mentorId = userConnecter.getUser().getId();
        List<MentorDemandeResponseDto> demandes = mentorProjetService.obtenirDemandesPourMentor(mentorId);
        return ResponseEntity.ok(demandes);
    }

    @GetMapping("/projet/{projetId}")
    public ResponseEntity<ProjetResponseDto> obtenirDetailsProjetPourMentor(
            @PathVariable int projetId) {
        ProjetResponseDto projetDetail = mentorProjetService.obtenirDetailsFicheProjet(projetId);
        return ResponseEntity.ok(projetDetail);
    }
}
