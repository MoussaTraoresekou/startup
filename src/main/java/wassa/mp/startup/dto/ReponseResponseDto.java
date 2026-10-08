package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor @Data
public class ReponseResponseDto {
    private int id;
    private String nom_projet;
    private String etape_concernee;
    private String question_etape;
    private String reponse;
}
