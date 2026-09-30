package wassa.mp.startup.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wassa.mp.startup.Enumeration.TypeEtapes;

import java.util.List;

@Entity
@AllArgsConstructor @NoArgsConstructor @Data
public class Etapes {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Enumerated(EnumType.STRING)
    private TypeEtapes type;
    @OneToMany(cascade = CascadeType.ALL,mappedBy = "etape")
    private List<ProjetEtape> projetEtapes;
}
