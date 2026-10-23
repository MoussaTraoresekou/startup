package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class ProjetResponseDto {
    private int id;
    private String titre;
    private String description;
    private String pith_url;
    private String quota_propose;
    private String secteur;
    private String porteurNomComplet;
    private LocalDate date_creation;
    private long nombreLikes;

    // Constructeur à 8 arguments conservé : tout le code existant qui crée
    // un ProjetResponseDto continue de compiler (nombreLikes vaut alors 0).
    public ProjetResponseDto(int id, String titre, String description, String pith_url,
                             String quota_propose, String secteur,
                             String porteurNomComplet, LocalDate date_creation) {
        this(id, titre, description, pith_url, quota_propose, secteur,
                porteurNomComplet, date_creation, 0L);
    }
}