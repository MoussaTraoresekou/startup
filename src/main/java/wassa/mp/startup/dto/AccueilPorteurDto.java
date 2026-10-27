package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor
@NoArgsConstructor
public class AccueilPorteurDto {
    private String specialite;
    private int nbrProjetRejoint;
}
