package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor @AllArgsConstructor
public class ConversationDTO {

    private int id;
    private int projetId;
    private String projetTitre;
    private int investisseurId;
    private String investisseurNom;
    private int porteurId;
    private String porteurNom;
    private String dernierMessage;
    private LocalDateTime dateCreation;
    private LocalDateTime dateDernierMessage;


}
