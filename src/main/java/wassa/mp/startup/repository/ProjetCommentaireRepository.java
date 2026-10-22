package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wassa.mp.startup.model.ProjetCommentaire;

import java.util.List;

public interface ProjetCommentaireRepository extends JpaRepository<ProjetCommentaire, Integer> {

    /** Les commentaires d'un projet, du plus récent au plus ancien. */
    List<ProjetCommentaire> findByProjetIdOrderByDateCreationDesc(int projetId);
}