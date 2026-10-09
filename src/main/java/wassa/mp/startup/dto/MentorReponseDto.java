package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data @NoArgsConstructor
@AllArgsConstructor
public class MentorReponseDto {
    private int id;
    private String nom;
    private String prenom;
    private String diplome_url;
    private String cv_url;
    private String statu;
    private String telephone;
    private String role;
    private String email;
    private LocalDate dateCreation;
    private String description;
}
