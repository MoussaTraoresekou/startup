package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
@AllArgsConstructor
public class PorteurRequestDto {
    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private String password;
}
