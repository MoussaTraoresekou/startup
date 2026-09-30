package wassa.mp.startup.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wassa.mp.startup.Enumeration.StatutEtapeEnum;

import java.time.LocalDate;
import java.util.List;

@Entity @Data @NoArgsConstructor
@AllArgsConstructor
public class ProjetEtape {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Enumerated(EnumType.STRING)
    private StatutEtapeEnum StatutEtape;
    private LocalDate dateSoumission=LocalDate.now();
    private LocalDate dateValidation;
    private String commentaireMentor;
    @ManyToOne
    private Projet projet;
    @ManyToOne
    private Etape etape;
    @OneToOne
    private DocumentRendu documentRendu;
    @OneToMany(mappedBy = "projetEtape")
    private List<Reponse> reponse;

}
