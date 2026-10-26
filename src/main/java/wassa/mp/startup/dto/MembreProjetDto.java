package wassa.mp.startup.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wassa.mp.startup.Enumeration.RoleDansProjet;
import wassa.mp.startup.Enumeration.StatutMembre;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembreProjetDto {

    private int id; // Identifiant de la participation au projet
    private int projetId;
    private int utilisateurId;

    private String nom;
    private String prenom;
    private String roleEcosysteme;
    private String specialite;

    private RoleDansProjet roleProjet;
    private String fonction;
    private String responsabilites;

    private LocalDateTime dateIntegration;
    private StatutMembre statut;
}
