package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import wassa.mp.startup.model.ProjetLike;

import java.util.List;

public interface ProjetLikeRepository extends JpaRepository<ProjetLike, Integer> {

    boolean existsByInvestisseurIdAndProjetId(int investisseurId, int projetId);

    void deleteByInvestisseurIdAndProjetId(int investisseurId, int projetId);

    long countByProjetId(int projetId);

    @Query("SELECT l.projet.id FROM ProjetLike l WHERE l.investisseur.id = :investisseurId")
    List<Integer> findProjetIdsByInvestisseur(@Param("investisseurId") int investisseurId);
    @Query("SELECT l FROM ProjetLike l WHERE l.investisseur.id = :investisseurId")
    List<ProjetLike> ListeProjetAimerParInv(@Param("investisseurId") int investisseurId);

}