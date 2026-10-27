
package wassa.mp.startup.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import wassa.mp.startup.service.StockageFichierService;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/fichiers")
public class FichierController {

    @Autowired
    private StockageFichierService stockageFichierService;

    @GetMapping("/{nomFichier:.+}")
    public ResponseEntity<Resource> consulterFichier(
            @PathVariable String nomFichier,
            @RequestHeader(
                    value = HttpHeaders.RANGE,
                    required = false
            ) String rangeHeader) {

        final Path chemin;

        try {
            chemin = stockageFichierService.obtenirChemin(nomFichier);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nom de fichier invalide."
            );
        }

        if (!Files.isRegularFile(chemin)
                || !Files.isReadable(chemin)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Fichier introuvable."
            );
        }

        try {
            long taille = Files.size(chemin);

            String extension = nomFichier.substring(
                    nomFichier.lastIndexOf('.') + 1
            ).toLowerCase(Locale.ROOT);

            MediaType typeMime = switch (extension) {
                case "pdf" -> MediaType.APPLICATION_PDF;
                case "mp4" -> MediaType.parseMediaType("video/mp4");
                case "jpg" -> MediaType.IMAGE_JPEG;
                default -> throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Type de fichier non autorisé."
                );
            };

            // Lecture complète du fichier.
            if (rangeHeader == null || rangeHeader.isBlank()) {
                return ResponseEntity.ok()
                        .contentType(typeMime)
                        .contentLength(taille)
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .header(
                                HttpHeaders.CONTENT_DISPOSITION,
                                "inline; filename=\"" + nomFichier + "\""
                        )
                        .body(new FileSystemResource(chemin));
            }

            // Lecture partielle pour la vidéo et les autres fichiers.
            final List<HttpRange> ranges;

            try {
                ranges = HttpRange.parseRanges(rangeHeader);
            } catch (IllegalArgumentException e) {
                return plageInvalide(taille);
            }

            // Une seule plage est prise en charge.
            if (ranges.size() != 1 || taille == 0) {
                return plageInvalide(taille);
            }

            HttpRange range = ranges.get(0);

            final long debut;
            final long fin;

            try {
                debut = range.getRangeStart(taille);
                fin = range.getRangeEnd(taille);
            } catch (IllegalArgumentException e) {
                return plageInvalide(taille);
            }

            if (debut < 0
                    || debut >= taille
                    || fin < debut
                    || fin >= taille) {
                return plageInvalide(taille);
            }

            long longueur = fin - debut + 1;

            Resource ressourcePartielle =
                    new RessourcePartielle(chemin, debut, longueur);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(typeMime)
                    .contentLength(longueur)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(
                            HttpHeaders.CONTENT_RANGE,
                            "bytes " + debut + "-" + fin + "/" + taille
                    )
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + nomFichier + "\""
                    )
                    .body(ressourcePartielle);

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Impossible de lire le fichier."
            );
        }
    }

    /**
     * Construit une réponse 416 avec la taille totale du fichier.
     */
    private ResponseEntity<Resource> plageInvalide(long taille) {
        return ResponseEntity
                .status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                .header(
                        HttpHeaders.CONTENT_RANGE,
                        "bytes */" + taille
                )
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .build();
    }

    /**
     * Ressource qui lit uniquement une portion déterminée du fichier.
     */
    private static class RessourcePartielle extends AbstractResource {

        private final Path chemin;
        private final long debut;
        private final long longueur;

        RessourcePartielle(
                Path chemin,
                long debut,
                long longueur) {
            this.chemin = chemin;
            this.debut = debut;
            this.longueur = longueur;
        }

        @Override
        public String getDescription() {
            return "Portion du fichier " + chemin.getFileName();
        }

        @Override
        public long contentLength() {
            return longueur;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            InputStream entree = Files.newInputStream(chemin);

            try {
                long restantPourSauter = debut;

                while (restantPourSauter > 0) {
                    long saute = entree.skip(restantPourSauter);

                    if (saute <= 0) {
                        if (entree.read() == -1) {
                            throw new IOException(
                                    "Impossible d'atteindre la position demandée."
                            );
                        }

                        saute = 1;
                    }

                    restantPourSauter -= saute;
                }

                return new FilterInputStream(entree) {

                    private long restant = longueur;

                    @Override
                    public int read() throws IOException {
                        if (restant <= 0) {
                            return -1;
                        }

                        int valeur = super.read();

                        if (valeur != -1) {
                            restant--;
                        }

                        return valeur;
                    }

                    @Override
                    public int read(
                            byte[] buffer,
                            int offset,
                            int tailleBuffer) throws IOException {

                        if (restant <= 0) {
                            return -1;
                        }

                        int limite = (int) Math.min(
                                tailleBuffer,
                                restant
                        );

                        int nombre = super.read(
                                buffer,
                                offset,
                                limite
                        );

                        if (nombre > 0) {
                            restant -= nombre;
                        }

                        return nombre;
                    }
                };

            } catch (IOException e) {
                try {
                    entree.close();
                } catch (IOException fermeture) {
                    e.addSuppressed(fermeture);
                }

                throw e;
            }
        }
    }
}
