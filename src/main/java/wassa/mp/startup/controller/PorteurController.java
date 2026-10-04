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
import wassa.mp.startup.service.*;

import java.util.List;

@RestController
@RequestMapping("/api/porteur")
@SecurityRequirement(name = "bearerAuth")
public class PorteurController {
    @Autowired
    private PorteurService  porteurService;
    @Autowired
    private ProjetService projetService;
    @Autowired
    private ProjetEtapeService projetEtapeService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private ResponseService responseService;
    @Autowired
    private MentorProjetService mentorProjetService;

    @PostMapping("/register")
    public ResponseEntity<PorteurResponseDto> createPorteur(@RequestBody PorteurRequestDto porteurRequestDto) {
        System.out.println(porteurRequestDto.getEmail());
         return  ResponseEntity.status(HttpStatus.CREATED).body(porteurService.registerPorteur(porteurRequestDto));
    }
    @PostMapping("/projet")
    public ResponseEntity<String> CreerProjet(@RequestBody ProjetRequestDto p,@AuthenticationPrincipal CustumUserDetail userConnecter){
        System.out.println("moussa");
        System.out.println(userConnecter.getUser().getEmail());
        projetService.creaProjet(p,userConnecter);
        return ResponseEntity.status(HttpStatus.CREATED).body("preojet cree avec succes!");
    }
    @GetMapping("/projets/{id}")
    public ProjetResponseDto getProjetByid(@PathVariable int id, @AuthenticationPrincipal CustumUserDetail userConnecter){
        return projetService.getProjetByid(id,userConnecter);
    }
    @GetMapping("/projets")
    public List<ProjetResponseDto> getProjetsByPorteurId(@AuthenticationPrincipal CustumUserDetail userConnecter){
        return projetService.getAllProjets(userConnecter);
    }
    @PutMapping("/projets/{id}")
    public ProjetResponseDto modifierProjet(@PathVariable int id, @RequestBody ProjetRequestDto projetRequestDto, @AuthenticationPrincipal CustumUserDetail userConnecter){
        return projetService.modifierProjet(id,userConnecter,projetRequestDto);
    }
    @GetMapping("/projets/{id}/etapes")
    public List<ProjetEtapeResponseDto> getEtapesProjet(@PathVariable int id, @AuthenticationPrincipal CustumUserDetail userConnecter){
        return  projetEtapeService.projetEtapeResponseDtoList(id,userConnecter);
    }
    @PostMapping("/projet-etapes/commencer")
    public ResponseEntity<String>CommencerEtapes(@AuthenticationPrincipal CustumUserDetail userConnecter,@RequestBody ProjetEtapesRequestDto projetEtapesRequestDto){
        projetEtapeService.commencerEtapes(projetEtapesRequestDto,userConnecter);
        return ResponseEntity.status(HttpStatus.CREATED).body("etape commencée  avec succes!");
    }
    @GetMapping("/etapes/{id}/questions")
    public List<QuestionResponseDto> getQuestionByEtapeId(@PathVariable int id){
        return questionService.getQuestionByEtape(id);

    }
    @GetMapping("/projetEtape/{projet_etape_id}")
    public List<ReponseResponseDto> getReponseByEtapePorjet_id(@PathVariable int projet_etape_id){
       return  responseService.getReponseByEtapePorjet_id(projet_etape_id);
    }
    @PutMapping("/projetEtape/{projetEtapeId}/questions/{questionId}/repondre")
    public ResponseEntity<String>RepondreUnequestionEtape(@PathVariable int projetEtapeId,@PathVariable int questionId,@RequestBody ReponseRequestDto reponseRequestDto){
        responseService.repondreunequestion(projetEtapeId,questionId,reponseRequestDto);
        return ResponseEntity.ok(
                "question repondu avec succes"
        );
    }
    @GetMapping("/projetEtape/{projetEtapeId}/telecharger-livrable")
    public ResponseEntity<byte[]> telechargerLivrable(
            @PathVariable int projetEtapeId,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {

        // 1. Appel du service pour fabriquer le flux de données du document
        byte[] contenuFichier = projetEtapeService.genererDocumentLivrable(projetEtapeId, userConnecter);

        // 2. Définition dynamique du nom du fichier au téléchargement
        String nomFichier = "Livrable_Etape_" + projetEtapeId + ".txt";

        // 3. Construction de la réponse HTTP avec les en-têtes d'attachement de fichier
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomFichier + "\"")
                .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
                .body(contenuFichier);
    }
    /**
     * Endpoint permettant au Porteur connecté d'inviter un Mentor spécifique à accompagner son projet.
     * URL : POST http://localhost:8080/api/porteur/solliciter-mentor
     */
    @PostMapping("/solliciter-mentor")
    public ResponseEntity<String> solliciterMentor(
            @Valid @RequestBody InvitationMentorDto invitationMentorDto,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {

        // Appel de la méthode de service pour le scénario B
        mentorProjetService.porteurSolliciteMentor(invitationMentorDto, userConnecter);

        // Retourne un statut 201 CREATED avec un message de succès
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("L'invitation a été envoyée avec succès. Le mentor a été notifié en temps réel.");
    }
    @PutMapping("/mentor-projets/{id}/accepter")
    public ResponseEntity<String> accepterDemande(@PathVariable int id, @AuthenticationPrincipal CustumUserDetail userConnecter) {

        mentorProjetService.accepterDemandeMentorat(id, userConnecter);

        return ResponseEntity.ok("La demande de mentorat a été acceptée avec succès.");
    }
    /**
     * Le Porteur refuse une demande venant d'un Mentor
     * URL : PUT /api/porteur/mentor-projets/{id}/refuser
     */
    @PutMapping("/mentor-projets/{id}/refuser")
    public ResponseEntity<String> porteurRefuserDemande(
            @PathVariable int id,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {
        mentorProjetService.refuserDemandeMentorat(id, userConnecter);
        return ResponseEntity.ok("La demande de mentorat a été déclinée.");
    }



}
