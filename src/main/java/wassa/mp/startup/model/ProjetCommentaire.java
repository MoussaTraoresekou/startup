package wassa.mp.startup.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Un commentaire laissé par un investisseur sur un projet.
 * Investisseur 1 ---- N ProjetCommentaire N ---- 1 Projet
 * (contrairement au like, un investisseur peut commenter plusieurs fois).
 */
@Entity
@Table(name = "projet_commentaire")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjetCommentaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 1500)
    private String contenu;

    private LocalDateTime dateCreation = LocalDateTime.now();

    @ManyToOne(optional = false)
    private Investisseur investisseur;

    @ManyToOne(optional = false)
    private Projet projet;
}