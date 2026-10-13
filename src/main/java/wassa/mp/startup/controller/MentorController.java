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
import wassa.mp.startup.service.ProjetEtapeService;

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
    @Autowired
    ProjetEtapeService projetEtapeService;

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
    @GetMapping("/projet-etapes/{id}/consulter-livrable")
    public ResponseEntity<LivrableEtapeDto> voirLivrableVirtuelInJson(@PathVariable int id) {
        return ResponseEntity.ok(projetEtapeService.consulterLivrableEnJson(id));
    }
    @PutMapping("/projet-etapes/valider")
    public ResponseEntity<String> validerEtapeOfficielle(@Valid @RequestBody ActionEtapeDto dto) {
        projetEtapeService.validerEtapeParMentor(dto);
        return ResponseEntity.ok("L'étape a été marquée comme validée. Le porteur de projet a été notifié.");
    }
    @PutMapping("/projet-etapes/renvoyer-correction")
    public ResponseEntity<String> renvoyerEtapeCorrection(@Valid @RequestBody ActionEtapeDto dto) {
        projetEtapeService.renvoyerEtapePourCorrection(dto);
        return ResponseEntity.ok("L'étape a été renvoyée pour modification avec votre note de cadrage.");
    }
    @GetMapping("/mes-startups")
    public ResponseEntity<List<ProjetResponseDto>> obtenirMesStartups(
            @AuthenticationPrincipal CustumUserDetail userConnecter) {
        List<ProjetResponseDto> list = mentorProjetService.obtenirStartupsAssigneesAuMentor(userConnecter.getUser().getId());
        return ResponseEntity.ok(list);
    }
    @GetMapping("/projets/{id}/etapes")
    public List<ProjetEtapeResponseDto> getEtapesProjet(@PathVariable int id, @AuthenticationPrincipal CustumUserDetail userConnecter){
        return  projetEtapeService.projetEtapeResponseDtoListPourMentor(id,userConnecter);
    }


}
