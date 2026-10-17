package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wassa.mp.startup.Enumeration.TypeInvestisseurEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvestorResponseDto {
    private int id;
    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private TypeInvestisseurEnum typeInvestisseur;
    private boolean profileCompleted;
}
