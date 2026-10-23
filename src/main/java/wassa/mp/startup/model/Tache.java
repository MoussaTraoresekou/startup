package wassa.mp.startup.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wassa.mp.startup.Enumeration.StatutTache;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Data @NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tache")
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
    @Column(nullable = false)
    private StatutTache statut;

    // Projet auquel appartient la tâche
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "projet_id", nullable = false)
    private Projet projet;

    // Porteur qui crée, attribue et vérifie la tâche
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "porteur_id", nullable = false)
    private Porteur porteur;

    // Membre responsable de la réalisation
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membre_responsable_id", nullable = false)
    private MembreProjet membreResponsable;

    // Rendu actuel du collaborateur
    @Column(columnDefinition = "TEXT")
    private String renduDescription;

    @Column(length = 1000)
    private String renduUrl;

    private LocalDateTime dateSoumission;

    // Retour du porteur lors de la vérification
    @Column(columnDefinition = "TEXT")
    private String commentaireVerification;

    private LocalDateTime dateVerification;


    @PrePersist
    public void avantInsertion() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }

        if (statut == null) {
            statut = StatutTache.A_FAIRE;
        }


    }

}

