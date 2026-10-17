package wassa.mp.startup.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import wassa.mp.startup.dto.*;
import wassa.mp.startup.service.InvestisseurService;

import java.util.List;
@RestController
@RequestMapping("/api/investisseur")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")

public class InvestisseurController {

    private final InvestisseurService profilService;
    private final InvestisseurService registrationService;

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
}