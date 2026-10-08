package wassa.mp.startup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeMentoratDto {

    @NotNull(message = "L'ID du projet ciblé est obligatoire.")
    private Integer projetId;

    @NotBlank(message = "Le message ou la description de votre proposition est obligatoire.")
    private String description;
}
