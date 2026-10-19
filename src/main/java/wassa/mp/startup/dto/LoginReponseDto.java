package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class LoginReponseDto {

    private int id;
    private String token;
    private String role;
    private String nom;
    private String prenom;
    private String email;

    /**
     * Toujours true pour les rôles autres qu'investisseur.
     * Pour un investisseur : false tant qu'il n'a ni validé ses secteurs
     * ni cliqué sur « Passer ».
     */
    private boolean profileCompleted = true;

    /** Ancien constructeur (sans profileCompleted) : conservé pour ne rien casser. */
    public LoginReponseDto(int id, String token, String role,
                           String nom, String prenom, String email) {
        this(id, token, role, nom, prenom, email, true);
    }
}