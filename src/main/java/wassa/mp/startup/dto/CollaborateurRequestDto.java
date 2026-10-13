package wassa.mp.startup.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollaborateurRequestDto {
    private String nom;
    private String prenom;

    private String telephone;
    private String email;

    private String password;

    private String specialite;

    private String biographie;

    private String lienPortfolio;
}
