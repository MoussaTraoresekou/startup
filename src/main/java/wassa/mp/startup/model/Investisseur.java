package wassa.mp.startup.model;

import jakarta.persistence.*;
import lombok.*;
import wassa.mp.startup.Enumeration.TypeInvestisseurEnum;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor @AllArgsConstructor
@ToString(callSuper = true, exclude = "secteursInteret")
public class Investisseur extends User {
    @Enumerated(EnumType.STRING)
    @Column(name = "type_investisseur")
    private TypeInvestisseurEnum typeInvestisseur;
    @Column(name = "profile_completed", nullable = false)
    private boolean profileCompleted = false;
    @ManyToMany
    @JoinTable(
            name = "investisseur_secteurs",
            joinColumns = @JoinColumn(name = "investisseur_id"),
            inverseJoinColumns = @JoinColumn(name = "secteur_id"))
    private Set<SecteurActivite> secteursInteret = new HashSet<>();
}