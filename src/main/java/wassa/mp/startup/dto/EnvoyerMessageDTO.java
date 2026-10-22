package wassa.mp.startup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
@AllArgsConstructor
public class EnvoyerMessageDTO {
    @NotBlank(message = "Le contenu du message est obligatoire")
    @Size(max = 5000, message = "Le message ne doit pas dépasser 5000 caractères")
    private String contenu;


}
