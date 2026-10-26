
package wassa.mp.startup.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wassa.mp.startup.Enumeration.StatutTache;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 150)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    private LocalDate dateEcheance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTache statut = StatutTache.A_FAIRE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "projet_id", nullable = false)
    private Projet projet;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membre_responsable_id", nullable = false)
    private MembreProjet membreResponsable;

    // Rendu actuel du collaborateur
    @Column(columnDefinition = "TEXT")
    private String renduDescription;

    @Column(length = 1000)
    private String renduUrl;

    private LocalDateTime dateSoumission;
    @Column(columnDefinition = "TEXT")
    private String commentaireVerification;

    private LocalDateTime dateVerification;

    @PrePersist
    public void avantInsertion() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }
}
