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
import wassa.mp.startup.model.Candidature;
import wassa.mp.startup.model.Projet;
import wassa.mp.startup.model.ProjetEtape;
import wassa.mp.startup.model.Reponse;
import wassa.mp.startup.service.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/porteur")
@SecurityRequirement(name = "bearerAuth")
public class PorteurController {

    @Autowired
    private PorteurService porteurService;

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

    @Autowired
    private SecteurActiviteService secteurActiviteService;

    @Autowired
    private MentorService mentorService;

    @Autowired
    private OffresService offresService;
    @Autowired
    private CandidatureService candidatureService;

    @PostMapping("/register")
    public ResponseEntity<PorteurResponseDto> createPorteur(@RequestBody PorteurRequestDto porteurRequestDto) {
        System.out.println(porteurRequestDto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(porteurService.registerPorteur(porteurRequestDto));
    }

    @PostMapping("/projet")
    public ResponseEntity<ProjetResponseDto> CreerProjet(@RequestBody ProjetRequestDto p, @AuthenticationPrincipal CustumUserDetail userConnecter) {
        System.out.println("moussa");
        System.out.println(userConnecter.getUser().getEmail());
        Projet projet = projetService.creaProjet(p, userConnecter);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProjetResponseDto(projet.getId(), projet.getTitre(), projet.getDescription(), projet.getPith_url(), projet.getQuota_propose(), projet.getSecteurActivite().getNom(), userConnecter.getUser().getNomComplet(), projet.getDate_creation()));
    }

    @GetMapping("/projets/{id}")
    public ProjetResponseDto getProjetByid(@PathVariable int id, @AuthenticationPrincipal CustumUserDetail userConnecter) {
        return projetService.getProjetByid(id, userConnecter);
    }

    @GetMapping("/projets")
    public List<ProjetResponseDto> getProjetsByPorteurId(@AuthenticationPrincipal CustumUserDetail userConnecter) {
        return projetService.getAllProjets(userConnecter);
    }

    @PutMapping("/projets/{id}")
    public ProjetResponseDto modifierProjet(@PathVariable int id, @RequestBody ProjetRequestDto projetRequestDto, @AuthenticationPrincipal CustumUserDetail userConnecter) {
        return projetService.modifierProjet(id, userConnecter, projetRequestDto);
    }

    @GetMapping("/projets/{id}/etapes")
    public List<ProjetEtapeResponseDto> getEtapesProjet(@PathVariable int id, @AuthenticationPrincipal CustumUserDetail userConnecter) {
        return projetEtapeService.projetEtapeResponseDtoList(id, userConnecter);
    }

    @PutMapping("/projet-etapes/commencer")
    public ResponseEntity<ProjetEtapeResponseDto> CommencerEtapes(@AuthenticationPrincipal CustumUserDetail userConnecter, @RequestBody ProjetEtapesRequestDto projetEtapesRequestDto) {
        ProjetEtape projetEtape = projetEtapeService.commencerEtapes(projetEtapesRequestDto, userConnecter);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ProjetEtapeResponseDto(projetEtape.getId(), projetEtape.getStatutEtape(), projetEtape.getDateSoumission(), projetEtape.getDateValidation(), projetEtape.getCommentaireMentor(), projetEtape.getEtape().getType().toString(), "")
        );
    }

    @GetMapping("/etapes/{id}/questions")
    public List<QuestionResponseDto> getQuestionByEtapeId(@PathVariable("id") int etapeId, @RequestParam("projetId") int projetId) {
        return questionService.getQuestionByEtape(etapeId, projetId);
    }

    @GetMapping("/projetEtape/{projet_etape_id}")
    public List<ReponseResponseDto> getReponseByEtapePorjet_id(@PathVariable int projet_etape_id) {
        return responseService.getReponseByEtapePorjet_id(projet_etape_id);
    }

    @PutMapping("/projetEtape/{projetEtapeId}/questions/{questionId}/repondre")
    public ResponseEntity<ReponseResponseDto> RepondreUnequestionEtape(@PathVariable int projetEtapeId, @PathVariable int questionId, @RequestBody ReponseRequestDto reponseRequestDto) {
        Reponse reponse = responseService.repondreunequestion(projetEtapeId, questionId, reponseRequestDto);
        return ResponseEntity.ok(
                new ReponseResponseDto(reponse.getId(), reponse.getProjetEtape().getProjet().getTitre(), reponse.getProjetEtape().getEtape().getType().toString(), reponse.getQuestion().getLibelle(), reponse.getReponse())
        );
    }

    @GetMapping("/projetEtape/{projetEtapeId}/telecharger-livrable")
    public ResponseEntity<byte[]> telechargerLivrable(
            @PathVariable int projetEtapeId,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {
        byte[] contenuFichier = projetEtapeService.genererDocumentLivrable(projetEtapeId, userConnecter);
        String nomFichier = "Livrable_Etape_" + projetEtapeId + ".txt";
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomFichier + "\"")
                .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
                .body(contenuFichier);
    }

    @PostMapping("/solliciter-mentor")
    public ResponseEntity<Map<String, String>> solliciterMentor(
            @Valid @RequestBody InvitationMentorDto invitationMentorDto,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {
        mentorProjetService.porteurSolliciteMentor(invitationMentorDto, userConnecter);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "L'invitation a été envoyée avec succès. Le mentor a été notifié en temps réel."));
    }

    @PutMapping("/mentor-projets/{id}/accepter")
    public ResponseEntity<Map<String, String>> accepterDemande(@PathVariable int id, @AuthenticationPrincipal CustumUserDetail userConnecter) {
        mentorProjetService.accepterDemandeMentorat(id, userConnecter);
        return ResponseEntity.ok(Map.of("message", "La demande de mentorat a été acceptée avec succès."));
    }

    @PutMapping("/mentor-projets/{id}/refuser")
    public ResponseEntity<Map<String, String>> porteurRefuserDemande(@PathVariable int id, @AuthenticationPrincipal CustumUserDetail userConnecter) {
        mentorProjetService.refuserDemandeMentorat(id, userConnecter);
        return ResponseEntity.ok(Map.of("message", "La demande de mentorat a été déclinée."));
    }

    @GetMapping("/mentors")
    public List<MentorReponseDto> getAllMentorsValides() {
        return mentorService.listerMentorsValides();
    }

    @PutMapping("/projet-etapes/{id}/soumettre")
    public ResponseEntity<Map<String, String>> soumettreEtape(
            @PathVariable int id,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {

        projetEtapeService.soumettreLivrableEtape(id, userConnecter);

        // Renvoyer un vrai objet JSON : {"message": "..."}
        Map<String, String> response = new HashMap<>();
        response.put("message", "Le questionnaire de l'étape a été compilé et soumis au mentor avec succès.");

        return ResponseEntity.ok(response);
    }


    @PostMapping("/offres/publier")
    public ResponseEntity<OffresResponseDto> publierOffre(
            @Valid @RequestBody OffresRequestDto dto,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(offresService.publierAppelCandidature(dto, userConnecter));
    }
/*
    @GetMapping("/offres/{id}/candidatures")
    public ResponseEntity<List<CandidatureResponseDto>> obtenirCandidatures(
            @PathVariable int id,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {
        return ResponseEntity.ok(offresService.obtenirCandidaturesPourUneOffre(id, userConnecter));
    }

 */
    @GetMapping("/projets/{id}/candidatures-recues")
    public ResponseEntity<List<CandidatureResponseDto>> obtenirCandidaturesDuProjet(
            @PathVariable int id,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {
        return ResponseEntity.ok(offresService.obtenirToutesLesCandidaturesDuProjet(id, userConnecter));
    }

    @GetMapping("/projets/{id}/equipe-complete")
    public ResponseEntity<List<MembreEquipeDto>> getEquipeProjetComplete(
            @PathVariable int id,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {
        return ResponseEntity.ok(projetService.calculerEquipeProjetComplete(id));
    }
    @PutMapping("/candidatures/{id}/accepter")
    public ResponseEntity<Map<String, String>> accepterCandidature(@PathVariable int id){
        candidatureService.validerCandidature(id);
        return ResponseEntity.ok(Map.of("message","candadature acceptée avec succes"));
    }
    @PutMapping("/candidatures/{id}/refuser")
    public ResponseEntity<Map<String, String>> refuserCandidature(@PathVariable int id){
        candidatureService.refuserCandidature(id);
        return ResponseEntity.ok(Map.of("message","candidature refusée avec succes"));
    }

    @PostMapping("/offres/postuler")
    public ResponseEntity<CandidatureResponseDto> postulerOffre(@Valid @RequestBody CandidatureRequestDto dto, @AuthenticationPrincipal CustumUserDetail userConnecter) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(offresService.postulerAUneOffre(dto, userConnecter));
    }

    @GetMapping("/offres")
    public ResponseEntity<List<OffresResponseDto>> obtenirOffresOuvertes() {
        return ResponseEntity.ok(offresService.obtenirToutesMesOffres());
    }
    @GetMapping("/offres/ouvertes")
    public ResponseEntity<List<OffresResponseDto>> obtenirOffresOuvertes(
            @AuthenticationPrincipal CustumUserDetail userConnecter) {
        List<OffresResponseDto> offres = offresService.obtenirToutesLesOffresOuvertes(userConnecter);

        return ResponseEntity.ok(offres);
    }



}
