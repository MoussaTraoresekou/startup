package wassa.mp.startup.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@AllArgsConstructor @NoArgsConstructor @Data
public class Porteur extends User{
    private String lienPortfolio;
    @Column(columnDefinition = "TEXT")
    private String biographie;
    @OneToMany(mappedBy = "porteur")
    private List<Projet> projets;
    @ManyToOne
    private Specialite specialite;
    private boolean disponible = true;

}
