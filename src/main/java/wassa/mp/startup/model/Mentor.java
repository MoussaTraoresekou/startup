package wassa.mp.startup.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wassa.mp.startup.Enumeration.StatuEnumMentor;

import java.util.List;
@Entity @NoArgsConstructor @AllArgsConstructor @Data
public class Mentor extends  User {
    private String cv_url;
    private String diplome_url;
    @Enumerated(EnumType.STRING)
    private StatuEnumMentor statuEnumMentor;
    // Un mentor peut avoir plusieurs demandes ou suivis de projets en cours
    @OneToMany(mappedBy = "mentor")
    private List<MentorProjet> mentorProjets;
}
