package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class MentorEnAttenteResponseDto {
    private int id;
    private String prenom;
    private String nom;
    private String email;
    private String telephone;
    private String cvUrl;
    private String diplomeUrl;
}
