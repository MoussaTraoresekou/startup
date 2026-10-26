package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    // Miniature générée pour le pitch vidéo
    private String pith_thumbnail_url;

    // Type du pitch : VIDEO ou PDF
    private String pith_type;

    // Constructeur à 8 arguments conservé
    public ProjetResponseDto(
            int id,
            String titre,
            String description,
            String pith_url,
            String quota_propose,
            String secteur,
            String porteurNomComplet,
            LocalDate date_creation
    ) {
        this(
                id, titre, description, pith_url,
                quota_propose, secteur, porteurNomComplet,
                date_creation, 0L, null, null
        );
    }

    // Constructeur à 9 arguments conservé
    public ProjetResponseDto(
            int id,
            String titre,
            String description,
            String pith_url,
            String quota_propose,
            String secteur,
            String porteurNomComplet,
            LocalDate date_creation,
            long nombreLikes
    ) {
        this(
                id, titre, description, pith_url,
                quota_propose, secteur, porteurNomComplet,
                date_creation, nombreLikes, null, null
        );
    }
}