package wassa.mp.startup.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wassa.mp.startup.Enumeration.StatutDemandeMentoratEnum;
import java.time.LocalDateTime;
@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class MentorProjet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Enumerated(EnumType.STRING)
    private StatutDemandeMentoratEnum statut;
    private LocalDateTime dateDemande;
    private LocalDateTime dateTraitement;
    @ManyToOne
    private Mentor mentor;
    @ManyToOne
    private Projet projet;
}
