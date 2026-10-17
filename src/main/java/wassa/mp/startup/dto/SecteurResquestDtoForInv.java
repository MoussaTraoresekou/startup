package wassa.mp.startup.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data @NoArgsConstructor @AllArgsConstructor
public class SecteurResquestDtoForInv {
    @NotEmpty(message = "Choisissez au moins un secteur")
    @Size(max = 30, message = "Trop de secteurs sélectionnés")
    private List<Integer> secteurIds;
}
