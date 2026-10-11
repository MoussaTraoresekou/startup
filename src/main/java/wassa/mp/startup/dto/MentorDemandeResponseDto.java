package wassa.mp.startup.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MentorDemandeResponseDto {
    private int id;                 // ID de la ligne pivot (mentor_projet)
    private int projetId;           // ID du projet associé pour le bouton "Consulter" 🚀
    private String projetTitre;
    private String porteurNom;
    private String secteur;
    private String messageInvitation;
    private LocalDateTime dateDemande;
    private String statut;

    public MentorDemandeResponseDto(int id, int projetId, String projetTitre, String porteurNom,
                                    String secteur, String messageInvitation, LocalDateTime dateDemande,String statut) {
        this.id = id;
        this.projetId = projetId;
        this.projetTitre = projetTitre;
        this.porteurNom = porteurNom;
        this.secteur = secteur;
        this.messageInvitation = messageInvitation;
        this.dateDemande = dateDemande;
        this.statut=statut;
    }

}
