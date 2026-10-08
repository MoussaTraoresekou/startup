package wassa.mp.startup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class InvitationMentorDto {
    @NotNull(message = "L'ID du projet est obligatoire.")
    private Integer projetId;

    @NotNull(message = "L'ID du mentor sélectionné est obligatoire.")
    private Integer mentorId;

    @NotBlank(message = "Le message d'invitation est obligatoire.")
    private String messageInvitation;
}
