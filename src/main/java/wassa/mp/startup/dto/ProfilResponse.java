package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** État du profil renvoyé à l'app. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfilResponse {

    private boolean profileCompleted;
    private List<Integer> secteurIds;
}