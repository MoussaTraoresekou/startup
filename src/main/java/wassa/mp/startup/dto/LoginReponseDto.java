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


}