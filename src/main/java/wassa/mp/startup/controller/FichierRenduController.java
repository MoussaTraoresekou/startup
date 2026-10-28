package wassa.mp.startup.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.service.StockageFichierService;
import wassa.mp.startup.service.TacheService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@RestController
@RequestMapping("/api/fichiers/rendu")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FichierRenduController {

    private final StockageFichierService stockageFichierService;
    private final TacheService tacheService;

    @GetMapping("/{nomFichier:.+}")
    public ResponseEntity<Resource> obtenirRendu(
            @PathVariable String nomFichier,
            @AuthenticationPrincipal CustumUserDetail userConnecter
    ) throws IOException {

        Path chemin = stockageFichierService.obtenirChemin(nomFichier);

        // Vérifier les droits d'accès au rendu
        tacheService.verifierAccesRendu(
                nomFichier,
                userConnecter
        );

        if (!Files.isRegularFile(chemin)) {
            return ResponseEntity.notFound().build();
        }

        String extension = nomFichier.substring(
                nomFichier.lastIndexOf('.') + 1
        ).toLowerCase(Locale.ROOT);

        MediaType mediaType = switch (extension) {
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "mp4" -> MediaType.valueOf("video/mp4");
            default -> throw new IllegalArgumentException(
                    "Type de fichier non autorisé."
            );
        };

        Resource ressource = new FileSystemResource(chemin);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(Files.size(chemin))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + nomFichier + "\""
                )
                .body(ressource);
    }
}