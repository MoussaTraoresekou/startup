package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentaireResponseDto {
    private int id;
    private String contenu;
    private String auteurNomComplet;
    private LocalDateTime dateCreation;
    private boolean monCommentaire;
}