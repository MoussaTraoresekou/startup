
package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TacheCreationDto {

    private String titre;
    private String description;
    private LocalDate dateEcheance;
    private int membreResponsableId;
}
