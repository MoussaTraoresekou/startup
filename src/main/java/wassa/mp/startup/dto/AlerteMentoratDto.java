package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class AlerteMentoratDto {
    private int mentorProjetId;       // L'ID  de classe d'association crée d d'association créé en base
    private String message;           // Le texte de l'alerte à afficher à l'écran
    private String nomExpediteur;     // Nom de celui qui fait la démarche (le porteur ou le mentor)
    private String typeAuteur;        // "MENTOR" si le mentor propose, "PORTEUR" si le porteur sollicite
    private String nomProjet;         // Le nom du projet concerné
    private LocalDateTime dateAlerte;
}
