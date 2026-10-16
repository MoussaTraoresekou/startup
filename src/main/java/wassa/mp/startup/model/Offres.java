package wassa.mp.startup.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wassa.mp.startup.Enumeration.EnumOffre;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Offres {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String profilRecherche;

    private LocalDateTime datePublication = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private EnumOffre statuOffre = EnumOffre.OUVERTE;

    @ManyToOne
    @JoinColumn(name = "projet_id")
    private Projet projet;

    @OneToMany(mappedBy = "offres", cascade = CascadeType.ALL)
    private List<Candidature> candidatures;
    @ManyToOne
    @JoinColumn(name = "specialite_id")
    private Specialite specialite;
}
