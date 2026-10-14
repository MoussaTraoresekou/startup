package wassa.mp.startup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OffresRequestDto {
    private String titre;
    private String description;
    private String profilRecherche;
    private Integer projetId;
}
