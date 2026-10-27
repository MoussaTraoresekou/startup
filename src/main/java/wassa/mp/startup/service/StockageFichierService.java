
package wassa.mp.startup.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class StockageFichierService {

    private static final long TAILLE_MAX = 50L * 1024 * 1024;
    @Value("${app.storage.location}")

    private final Path repertoireStockage;
    @Value("${app.storage.ffmpeg-path:ffmpeg}")
    private final String cheminFfmpeg;

    public StockageFichierService(
            @Value("${app.storage.location}") String emplacement,
            @Value("${app.storage.ffmpeg-path:ffmpeg}") String cheminFfmpeg) {

        this.repertoireStockage = Paths.get(emplacement)
                .toAbsolutePath()
                .normalize();

        this.cheminFfmpeg = cheminFfmpeg;
    }

    public String enregistrerPitch(MultipartFile fichier) {
        return enregistrerPitchAvecDetails(fichier).nomFichier();
    }

    public ResultatStockagePitch enregistrerPitchAvecDetails(
            MultipartFile fichier) {

        if (fichier == null || fichier.isEmpty()) {
            throw new IllegalArgumentException(
                    "Veuillez sélectionner un fichier.");
        }

        if (fichier.getSize() > TAILLE_MAX) {
            throw new IllegalArgumentException(
                    "Le fichier ne doit pas dépasser 50 Mo.");
        }

        String nomOriginal = fichier.getOriginalFilename();

        if (nomOriginal == null || nomOriginal.isBlank()) {
            throw new IllegalArgumentException(
                    "Nom de fichier invalide.");
        }

        String nomNettoye = nomOriginal.replace("\\", "/");
        nomNettoye = nomNettoye.substring(
                nomNettoye.lastIndexOf('/') + 1);

        int positionPoint = nomNettoye.lastIndexOf('.');

        if (positionPoint <= 0) {
            throw new IllegalArgumentException(
                    "Extension de fichier absente ou invalide.");
        }

        String extension = nomNettoye
                .substring(positionPoint + 1)
                .toLowerCase(Locale.ROOT);

        if (!extension.equals("pdf") && !extension.equals("mp4")) {
            throw new IllegalArgumentException(
                    "Seuls les fichiers PDF et MP4 sont acceptés.");
        }

        verifierTypeMime(fichier, extension);

        String nomUnique = UUID.randomUUID() + "." + extension;
        Path destination = obtenirChemin(nomUnique);

        String nomMiniature = null;
        String type = extension.equals("pdf") ? "PDF" : "VIDEO";

        try {
            Files.createDirectories(repertoireStockage);

            try (InputStream entree = fichier.getInputStream();
                 OutputStream sortie = Files.newOutputStream(
                         destination,
                         StandardOpenOption.CREATE_NEW,
                         StandardOpenOption.WRITE)) {

                entree.transferTo(sortie);
            }

            if (extension.equals("pdf")) {
                verifierSignaturePdf(destination);
            } else {
                genererMiniature(destination);

                String base = nomUnique.substring(
                        0, nomUnique.lastIndexOf('.'));

                nomMiniature = base + ".jpg";
            }

            return new ResultatStockagePitch(
                    nomUnique,
                    nomMiniature,
                    type);

        } catch (IOException | RuntimeException e) {
            supprimerSilencieusement(destination);

            String base = nomUnique.substring(
                    0, nomUnique.lastIndexOf('.'));

            supprimerSilencieusement(
                    repertoireStockage.resolve(base + ".jpg"));

            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            }

            throw new IllegalStateException(
                    "Impossible d'enregistrer ou de vérifier le fichier.",
                    e);
        }
    }

    private void verifierTypeMime(
            MultipartFile fichier,
            String extension) {

        String typeMime = fichier.getContentType();

        if (typeMime == null) {
            throw new IllegalArgumentException(
                    "Le type du fichier ne peut pas être vérifié.");
        }

        boolean typeValide =
                extension.equals("pdf")
                        && typeMime.equalsIgnoreCase("application/pdf")
                        || extension.equals("mp4")
                        && (typeMime.equalsIgnoreCase("video/mp4")
                        || typeMime.equalsIgnoreCase(
                        "application/octet-stream"));

        if (!typeValide) {
            throw new IllegalArgumentException(
                    "Le type du fichier ne correspond pas à son extension.");
        }
    }

    private void verifierSignaturePdf(Path fichier) throws IOException {
        byte[] signature = new byte[5];

        try (InputStream entree = Files.newInputStream(fichier)) {
            int lus = entree.read(signature);

            if (lus != 5
                    || signature[0] != '%'
                    || signature[1] != 'P'
                    || signature[2] != 'D'
                    || signature[3] != 'F'
                    || signature[4] != '-') {

                throw new IOException(
                        "Le contenu du fichier n'est pas un PDF valide.");
            }
        }
    }

    private void genererMiniature(Path video) throws IOException {
        String nom = video.getFileName().toString();
        String base = nom.substring(0, nom.lastIndexOf('.'));

        Path miniature = repertoireStockage.resolve(base + ".jpg");

        ProcessBuilder commande = new ProcessBuilder(
                cheminFfmpeg,
                "-hide_banner",
                "-loglevel", "error",
                "-nostdin",
                "-y",
                "-i", video.toString(),
                "-frames:v", "1",
                "-vf", "scale=640:-1",
                miniature.toString()
        );

        commande.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        commande.redirectError(ProcessBuilder.Redirect.DISCARD);

        Process processus = commande.start();

        try {
            boolean termine = processus.waitFor(30, TimeUnit.SECONDS);

            if (!termine) {
                processus.destroyForcibly();

                throw new IOException(
                        "Délai dépassé lors de la création de la miniature.");
            }

            if (processus.exitValue() != 0
                    || !Files.isRegularFile(miniature)
                    || Files.size(miniature) == 0) {

                throw new IOException(
                        "FFmpeg n'a pas pu générer la miniature.");
            }

        } catch (InterruptedException e) {
            processus.destroyForcibly();
            Thread.currentThread().interrupt();

            throw new IOException(
                    "Génération de miniature interrompue.", e);
        }
    }

    public Path obtenirChemin(String nomFichier) {
        if (nomFichier == null
                || !nomFichier.matches(
                "[a-fA-F0-9-]{36}\\.(pdf|mp4|jpg)")) {

            throw new IllegalArgumentException(
                    "Nom de fichier invalide.");
        }

        Path chemin = repertoireStockage
                .resolve(nomFichier)
                .normalize();

        if (!chemin.startsWith(repertoireStockage)) {
            throw new IllegalArgumentException(
                    "Chemin de fichier invalide.");
        }

        return chemin;
    }

    private void supprimerSilencieusement(Path chemin) {
        try {
            Files.deleteIfExists(chemin);
        } catch (IOException ignored) {
        }
    }

    public String getCheminFfmpeg() {
        return cheminFfmpeg;
    }

    public void supprimerPitch(String nomFichier) {
        if (nomFichier == null || nomFichier.isBlank()) {
            return;
        }

        if (!nomFichier.matches(
                "[a-fA-F0-9-]{36}\\.(pdf|mp4)")) {

            throw new IllegalArgumentException(
                    "Nom de fichier de pitch invalide.");
        }

        Path chemin = obtenirChemin(nomFichier);

        try {
            Files.deleteIfExists(chemin);

            if (nomFichier.toLowerCase(Locale.ROOT).endsWith(".mp4")) {
                String base = nomFichier.substring(
                        0, nomFichier.lastIndexOf('.'));

                Path miniature = obtenirChemin(base + ".jpg");
                Files.deleteIfExists(miniature);
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Impossible de supprimer l'ancien pitch.", e);
        }
    }
}
