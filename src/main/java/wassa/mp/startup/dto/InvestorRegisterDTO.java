package wassa.mp.startup.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wassa.mp.startup.Enumeration.TypeInvestisseurEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvestorRegisterDTO {
    private String nom;
    private String prenom;
    private String email;

    private String telephone;

    private String password;

    private TypeInvestisseurEnum typeInvestisseur;
}
