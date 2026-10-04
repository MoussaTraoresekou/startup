package wassa.mp.startup.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.dto.DemandeMentoratDto;
import wassa.mp.startup.dto.MentorRegisterRequestDto;
import wassa.mp.startup.service.MentorProjetService;
import wassa.mp.startup.service.MentorService;

@RestController
@RequestMapping("/api/mentor")
@SecurityRequirement(name = "bearerAuth")
public class MentorController {

    @Autowired
    private MentorProjetService mentorProjetService;
    @Autowired
    private MentorService mentorService;

    /**
     * Endpoint permettant à un Mentor connecté de proposer son accompagnement à un projet.
     * URL : POST http://localhost:8080/api/mentor/proposer-accompagnement
     */
    @PostMapping("/proposer-accompagnement")
    public ResponseEntity<String> proposerAccompagnement(
            @Valid @RequestBody DemandeMentoratDto demandeMentoratDto,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {

        // Appel de la méthode de service que nous venons de coder ensemble
        mentorProjetService.mentorProposeMentorat(demandeMentoratDto, userConnecter);

        // Retourne un statut 201 CREATED avec un message de succès
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Votre proposition d'accompagnement a été envoyée et notifiée au porteur de projet.");
    }
    @PutMapping("/mentor-projets/{id}/accepter")
    public ResponseEntity<String> accepterDemande(@PathVariable int id, @AuthenticationPrincipal CustumUserDetail userConnecter) {

        mentorProjetService.accepterDemandeMentorat(id, userConnecter);

        return ResponseEntity.ok("La demande de mentorat a été acceptée avec succès.");
    }
    /**
     * Le Mentor refuse une invitation venant d'un Porteur
     * URL : PUT /api/mentor/mentor-projets/{id}/refuser
     */
    @PutMapping("/mentor-projets/{id}/refuser")
    public ResponseEntity<String> mentorRefuserInvitation(
            @PathVariable int id,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {
        mentorProjetService.refuserDemandeMentorat(id, userConnecter);
        return ResponseEntity.ok("L'invitation de mentorat a été déclinée.");
    }
    @PostMapping("/register")
    public ResponseEntity<String> inscrireNouveauMentor(@Valid @RequestBody MentorRegisterRequestDto dto) {
        // Appel de la logique métier que nous avons écrite ensemble
        mentorService.inscrireMentor(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Votre inscription a été soumise avec succès. Votre dossier (CV et diplôme) est en cours d'examen par nos administrateurs.");
    }

}
