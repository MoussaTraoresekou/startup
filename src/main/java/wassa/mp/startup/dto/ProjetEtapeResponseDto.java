package wassa.mp.startup.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wassa.mp.startup.Enumeration.StatutEtapeEnum;
import java.time.LocalDate;

@NoArgsConstructor @AllArgsConstructor @Data
public class ProjetEtapeResponseDto {
    private int id;
    private StatutEtapeEnum StatutEtape;
    private LocalDate dateSoumission;
    private LocalDate dateValidation;
    private String commentaireMentor;
    private String etape;
    private String document_url;
}
