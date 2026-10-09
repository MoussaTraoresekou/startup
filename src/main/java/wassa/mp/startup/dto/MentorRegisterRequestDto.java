package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class MentorRegisterRequestDto {
    private String prenom;
    private String nom;
    private String email;
    private String motDePass;
    private String telephone;
    private String cvUrl;
    private String diplomeUrl;
    private String description;
}
