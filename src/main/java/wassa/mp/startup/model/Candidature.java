package wassa.mp.startup.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wassa.mp.startup.Enumeration.EnumCandidature;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Candidature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Enumerated(EnumType.STRING)
    private EnumCandidature statuCandidature = EnumCandidature.EN_ATTENTE;
    @Column(columnDefinition = "TEXT")
    private String motivation;
    private LocalDateTime dateCreation = LocalDateTime.now();
    @ManyToOne
    @JoinColumn(name = "porteur_id")
    private Porteur porteur;
    @ManyToOne
    @JoinColumn(name = "offres_id")
    private Offres offres;
}
