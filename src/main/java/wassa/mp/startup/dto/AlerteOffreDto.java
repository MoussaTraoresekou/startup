package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlerteOffreDto {
    private int offreId;
    private int projetId;
    private String projetTitre;
    private String message;
    private LocalDateTime dateCreation;
}
