package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wassa.mp.startup.Enumeration.StatutTache;
import wassa.mp.startup.model.Tache;

import java.util.List;

@Repository
public interface TacheRepository extends JpaRepository<Tache, Integer> {

    // Toutes les tâches d'un projet
    List<Tache> findByProjet_Id(int projetId);

    // Tâches d'un projet selon leur statut
    List<Tache> findByProjet_IdAndStatut(
            int projetId,
            StatutTache statut
    );

    // Toutes les tâches attribuées à une participation précise
    List<Tache> findByMembreResponsable_Id(int membreResponsableId);

    // Tâches d'un membre dans un projet précis
    List<Tache> findByProjet_IdAndMembreResponsable_Id(
            int projetId,
            int membreResponsableId
    );

    // Tâches d'un projet à vérifier ou à corriger, selon le statut
    List<Tache> findByProjet_IdAndStatutIn(
            int projetId,
            List<StatutTache> statuts
    );

    // Vérifier si un membre possède une tâche dans un projet
    boolean existsByIdAndProjet_IdAndMembreResponsable_Id(
            int tacheId,
            int projetId,
            int membreResponsableId
    );
}
