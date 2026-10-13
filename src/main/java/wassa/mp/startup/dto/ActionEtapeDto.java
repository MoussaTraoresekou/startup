package wassa.mp.startup.dto;

import jakarta.validation.constraints.NotBlank;

public class ActionEtapeDto {

    private int projetEtapeId;

    @NotBlank(message = "Le commentaire ou motif est obligatoire.")
    private String commentaireMentor;

    public ActionEtapeDto() {}

    public ActionEtapeDto(int projetEtapeId, String commentaireMentor) {
        this.projetEtapeId = projetEtapeId;
        this.commentaireMentor = commentaireMentor;
    }

    public int getProjetEtapeId() {
        return projetEtapeId;
    }

    public void setProjetEtapeId(int projetEtapeId) {
        this.projetEtapeId = projetEtapeId;
    }

    public String getCommentaireMentor() {
        return commentaireMentor;
    }

    public void setCommentaireMentor(String commentaireMentor) {
        this.commentaireMentor = commentaireMentor;
    }
}
