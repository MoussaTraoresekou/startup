package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wassa.mp.startup.Enumeration.EnumOffre;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OffresResponseDto {
    private int id;
    private String titre;
    private String description;
    private String profilRecherche;
    private LocalDateTime datePublication;
    private EnumOffre statuOffre;
    private int projetId;
    private String projetTitre;
    private String porteurPrenom;
    private String porteurNom;
}
