
package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wassa.mp.startup.Enumeration.StatutTache;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TacheVerificationDto {

    private StatutTache statut;
    private String commentaireVerification;
}
