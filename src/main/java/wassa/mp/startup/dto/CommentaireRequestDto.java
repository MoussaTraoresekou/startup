package wassa.mp.startup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentaireRequestDto {

    @NotBlank(message = "Le commentaire ne peut pas être vide.")
    @Size(max = 1500, message = "Le commentaire ne peut pas dépasser 1500 caractères.")
    private String contenu;
}