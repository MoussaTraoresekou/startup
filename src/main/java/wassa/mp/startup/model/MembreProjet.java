
package wassa.mp.startup.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wassa.mp.startup.Enumeration.RoleDansProjet;
import wassa.mp.startup.Enumeration.StatutMembre;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "membre_projet",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_projet_user",
                        columnNames = {"projet_id", "user_id"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembreProjet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "projet_id", nullable = false)
    private Projet projet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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
