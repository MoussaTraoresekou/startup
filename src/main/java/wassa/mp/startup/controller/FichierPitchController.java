
package wassa.mp.startup.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wassa.mp.startup.service.StockageFichierService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/fichiers/pitch")
@RequiredArgsConstructor
public class FichierPitchController {

    private final StockageFichierService stockageFichierService;

    @GetMapping("/{nomFichier:.+}")
    public ResponseEntity<Resource> obtenirFichier(
            @PathVariable String nomFichier) throws IOException {

        Path chemin = stockageFichierService.obtenirChemin(nomFichier);

        if (!Files.isRegularFile(chemin)) {
            return ResponseEntity.notFound().build();
        }

        String extension = nomFichier.substring(
                nomFichier.lastIndexOf('.') + 1
        ).toLowerCase();

        MediaType mediaType = switch (extension) {
            case "jpg" -> MediaType.IMAGE_JPEG;
            case "mp4" -> MediaType.valueOf("video/mp4");
            case "pdf" -> MediaType.APPLICATION_PDF;
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
