package wassa.mp.startup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardPorteurDto {

    private int projetId;
    private String titre;

    private long investisseursInteresses;
    private long membresActifs;
    private long etapesValidees;
    private long etapesEnCours;

    private List<EtapeDto> etapes;
    private List<MembreDto> equipe;

    private RetourMentorDto dernierCommentaireMentor;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EtapeDto {
        private int id;
        private String nom;
        private String statut;
        private LocalDate dateSoumission;
        private LocalDate dateValidation;
        private String commentaireMentor;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MembreDto {
        private int id;
        private String nom;
        private String prenom;
        private String role;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetourMentorDto {
        private String commentaire;
        private String etape;
        private LocalDate date;
    }
}