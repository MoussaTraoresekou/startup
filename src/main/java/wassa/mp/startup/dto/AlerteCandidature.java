package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlerteCandidature {
    private int candidatureId;
    private int projetId;
    private String projetTitre;
    private int collaborateurId;
    private String collaborateurNomComplet;
    private String message;
    private LocalDateTime horodatage = LocalDateTime.now();
}
