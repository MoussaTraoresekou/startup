package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @NoArgsConstructor
@AllArgsConstructor
public class AlertTacheDto {
    private String message;           // Le texte de l'alerte à afficher à l'écran
    private String nomExpediteur;     // Nom de celui qui fait la démarche (le porteur ou le mentor)
    private String nomProjet;         // Le nom du projet concerné
    private LocalDateTime dateAlerte;
}
