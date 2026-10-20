package wassa.mp.startup.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.dto.*;
import wassa.mp.startup.model.Investisseur;
import wassa.mp.startup.service.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/investisseur")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")

public class InvestisseurController {

    private final InvestisseurService profilService;
    private final InvestisseurService registrationService;
    private final MentorProjetService mentorProjetService;
    private final ProjetService projetService;
    private final ProjetLikeService likeService;
    private final ProjetCommentaireService commentaireService;


    /**
     * Route publique d'inscription pour les investisseurs.
     * Cette route est configurée en permitAll() dans la SecurityConfig.
     */
    @PostMapping("/register")
    public ResponseEntity<InvestorResponseDto> registerInvestor(@Valid @RequestBody InvestorRegisterDTO investorRegisterDto) {

        InvestorResponseDto response = registrationService.registerInvestisseur(investorRegisterDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /** Secteurs disponibles pour l'écran de choix. */
    @GetMapping("/secteurs")
    public List<SecteurActiviteResponseDto> secteursDisponibles() {
        return profilService.listerSecteurs();
    }

    /** État actuel : profil complété ? secteurs déjà choisis ? */
    @GetMapping
    public ProfilResponse monProfil(Authentication authentication) {
        return profilService.getProfil(authentication.getName());
    }

    /** Valider le choix des secteurs. */
    @PutMapping("/secteurs")
    public ResponseEntity<ProfilResponse> enregistrerSecteurs(
            Authentication authentication,
            @Valid @RequestBody SecteurResquestDtoForInv request) {
        return ResponseEntity.ok(profilService.enregistrerSecteurs(
                authentication.getName(), request.getSecteurIds()));
    }

    /** Bouton « Passer ». */
    @PostMapping("/passer")
    public ResponseEntity<ProfilResponse> passer(Authentication authentication) {
        return ResponseEntity.ok(profilService.passer(authentication.getName()));
    }
    @GetMapping("/projet/{projetId}")
    public ResponseEntity<ProjetResponseDto> obtenirDetailsProjetPourMentor(
            @PathVariable int projetId) {
        ProjetResponseDto projetDetail = mentorProjetService.obtenirDetailsFicheProjet(projetId);
        return ResponseEntity.ok(projetDetail);
    }
    @GetMapping("/flux-projets")
    public ResponseEntity<List<ProjetResponseDto>> obtenirFluxProjets(@AuthenticationPrincipal CustumUserDetail userConnecter) {
        ProfilResponse investisseur = profilService.getProfil(userConnecter.getUser().getEmail());

        List<ProjetResponseDto> projets;

        // 2. Si l'investisseur a configuré ses secteurs, on filtre en base de données
        if (investisseur.isProfileCompleted() && !investisseur.getSecteurIds().isEmpty()) {
            projets = projetService.obtenirProjetsParSecteurs(investisseur.getSecteurIds());
        } else {
            // 3. S'il a cliqué sur "Passer" ou n'a rien coché, on affiche TOUT
            projets = projetService.getAllProjets(userConnecter);
        }

        return ResponseEntity.ok(projets);
    }
    /** Aimer / ne plus aimer un projet (bascule). */
    @PostMapping("/projet/{projetId}/like")
    public ResponseEntity<Map<String, Object>> basculerLike(
            @PathVariable int projetId,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {

        boolean liked = likeService.basculer(userConnecter.getUser().getEmail(), projetId);

        return ResponseEntity.ok(Map.of(
                "liked", liked,
                "nombreLikes", likeService.nombreLikes(projetId)
        ));
    }

    /** Identifiants des projets déjà aimés par l'investisseur connecté. */
    @GetMapping("/likes")
    public ResponseEntity<List<Integer>> mesLikes(@AuthenticationPrincipal CustumUserDetail userConnecter) {
        return ResponseEntity.ok(likeService.projetsLikes(userConnecter.getUser().getEmail()));
    }

    @PostMapping("/projet/{projetId}/commentaires")
    public ResponseEntity<CommentaireResponseDto> ajouterCommentaire(
            @PathVariable int projetId,
            @Valid @RequestBody CommentaireRequestDto request,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {

        CommentaireResponseDto commentaire = commentaireService.ajouter(
                userConnecter.getUser().getEmail(),
                projetId,
                request.getContenu()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(commentaire);
    }

    @GetMapping("/projet/{projetId}/commentaires")
    public ResponseEntity<List<CommentaireResponseDto>> listerCommentaires(
            @PathVariable int projetId,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {

        List<CommentaireResponseDto> commentaires = commentaireService.lister(
                userConnecter.getUser().getEmail(),
                projetId
        );

        return ResponseEntity.ok(commentaires);
    }

}