package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wassa.mp.startup.Enumeration.EnumCandidature;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidatureResponseDto {
    private int id;
    private EnumCandidature statuCandidature;
    private String motivation;
    private LocalDateTime dateCreation;
    private int collaboratorId;
    private String collaborateurNom;
    private String collaborateurPrenom;
    private String collaborateurEmail;
    private String collaborateurTelephone;
    private String collaborateurSpecialite;
    private String collaborateurBiographie;
    private String collaborateurLienPortfolio;

    private int offresId;
    private String offresTitre;
}
