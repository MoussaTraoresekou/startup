package wassa.mp.startup.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Collaborateur extends User {
    private String specialite;

    @Column(columnDefinition = "TEXT")
    private String biographie;

    private String lienPortfolio;

    private boolean disponible = true;

    @OneToMany(mappedBy = "collaborateur", cascade = CascadeType.ALL)
    private List<Candidature> candidatures;
}
