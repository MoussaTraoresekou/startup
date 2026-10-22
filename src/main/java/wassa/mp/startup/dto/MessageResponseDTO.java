package wassa.mp.startup.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data @NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDTO {

    private int id;
    private int conversationId;
    private int expediteurId;
    private String expediteurNom;
    private String contenu;
    private LocalDateTime dateEnvoi;
    private LocalDateTime dateLecture;


}
