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
import wassa.mp.startup.dto.MentorReponseDto;
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
    @PostMapping("/proposer-accompagnement")
    public ResponseEntity<String> proposerAccompagnement(
            @Valid @RequestBody DemandeMentoratDto demandeMentoratDto,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {

        mentorProjetService.mentorProposeMentorat(demandeMentoratDto, userConnecter);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Votre proposition d'accompagnement a été envoyée et notifiée au porteur de projet.");
    }
    @PutMapping("/mentor-projets/{id}/accepter")
    public ResponseEntity<String> accepterDemande(@PathVariable int id, @AuthenticationPrincipal CustumUserDetail userConnecter) {

        mentorProjetService.accepterDemandeMentorat(id, userConnecter);

        return ResponseEntity.ok("La demande de mentorat a été acceptée avec succès.");
    }
    @PutMapping("/mentor-projets/{id}/refuser")
    public ResponseEntity<String> mentorRefuserInvitation(
            @PathVariable int id,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {
        mentorProjetService.refuserDemandeMentorat(id, userConnecter);
        return ResponseEntity.ok("L'invitation de mentorat a été déclinée.");
    }
    @PostMapping("/register")
    public ResponseEntity<MentorReponseDto> inscrireNouveauMentor( @RequestBody MentorRegisterRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mentorService.inscrireMentor(dto));
    }

}
