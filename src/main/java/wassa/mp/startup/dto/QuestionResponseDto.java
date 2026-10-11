package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class QuestionResponseDto {
    private int id;
    private String libelle;
    private String etape_nom;
    private String reponse_donnee;

}
