package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembreEquipeDto {
    private int id;
    private String nom;
    private String prenom;
    private String roleEcosysteme;
    private String specialite;
}
