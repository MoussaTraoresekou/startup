
package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wassa.mp.startup.Enumeration.StatutTache;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TacheResponseDto {

    private int id;
    private String titre;
    private String description;

    private int projetId;
    private String projetTitre;

    private int membreResponsableId;
    private String membreResponsableNom;
    private String membreResponsablePrenom;

    private LocalDateTime dateCreation;
    private LocalDate dateEcheance;

    private StatutTache statut;

    private String renduDescription;
    private String renduUrl;
    private LocalDateTime dateSoumission;

    private String commentaireVerification;
    private LocalDateTime dateVerification;
}
