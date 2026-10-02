package wassa.mp.startup.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wassa.mp.startup.Enumeration.TypeEtapes;

import java.util.List;
@Entity
@AllArgsConstructor @NoArgsConstructor @Data
public class Etape {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Enumerated(EnumType.STRING)
    private TypeEtapes type;
    @OneToMany(mappedBy = "etape")
    private List<ProjetEtape> projetEtapes;
    @OneToMany
    private List<Question> questions;
}
