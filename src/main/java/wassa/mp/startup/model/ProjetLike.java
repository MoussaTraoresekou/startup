package wassa.mp.startup.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "projet_like",
        uniqueConstraints = @UniqueConstraint(columnNames = {"investisseur_id", "projet_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjetLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false)
    private Investisseur investisseur;

    @ManyToOne(optional = false)
    private Projet projet;

    private LocalDateTime dateLike = LocalDateTime.now();
}