package wassa.mp.startup.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wassa.mp.startup.Enumeration.RoleDansProjet;
import wassa.mp.startup.Enumeration.StatutMembre;

import java.time.LocalDateTime;

@Entity @Data @NoArgsConstructor
@AllArgsConstructor
@Table(name = "membre_projet",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_membre_projet_utilisateur",
                        columnNames = {"projet_id", "utilisateur_id"}
                )
        }
)

public class MembreProjet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "projet_id", nullable = false)
    private Projet projet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private User utilisateur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleDansProjet roleProjet;

    private String fonction;

    @Column(columnDefinition = "TEXT")
    private String responsabilites;

    @Column(nullable = false)
    private LocalDateTime dateIntegration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutMembre statut;
    @PrePersist
    public void avantInsertion() {
        if (dateIntegration == null) {
            dateIntegration = LocalDateTime.now();
        }

        if (statut == null) {
            statut = StatutMembre.ACTIF;
        }
    }

}
