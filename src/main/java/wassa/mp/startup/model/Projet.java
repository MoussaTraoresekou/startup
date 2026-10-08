package wassa.mp.startup.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@AllArgsConstructor @NoArgsConstructor @Data
public class Projet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String titre;
    private String description;
    private String pith_url;
    private String quota_propose;
    private LocalDate date_creation=LocalDate.now();
    @ManyToOne
    private Porteur porteur;
    @OneToMany(mappedBy = "projet")
    private List<ProjetEtape>projetEtapes;
    @ManyToOne
    private SecteurActivite secteurActivite;


}
