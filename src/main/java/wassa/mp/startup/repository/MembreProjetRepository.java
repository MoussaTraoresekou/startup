        package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wassa.mp.startup.Enumeration.StatutMembre;
import wassa.mp.startup.model.MembreProjet;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembreProjetRepository
        extends JpaRepository<MembreProjet, Integer> {

    // Récupérer tous les membres d'un projet
    List<MembreProjet> findByProjet_Id(int projetId);

    // Récupérer les membres d'un projet selon leur statut
    List<MembreProjet> findByProjet_IdAndStatut(
            int projetId,
            StatutMembre statut
    );

    // Retrouver la participation d'un utilisateur à un projet
    Optional<MembreProjet> findByProjet_IdAndUser_Id(
            int projetId,
            int utilisateurId
    );

    // Vérifier si un utilisateur participe à un projet
    boolean existsByProjet_IdAndUser_Id(
            int projetId,
            int utilisateurId
    );

    // Vérifier si un utilisateur est membre actif d'un projet
    boolean existsByProjet_IdAndUser_IdAndStatut(
            int projetId,
            int utilisateurId,
            StatutMembre statut
    );
}
