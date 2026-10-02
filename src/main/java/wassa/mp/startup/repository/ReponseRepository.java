package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import wassa.mp.startup.model.Reponse;

import java.util.List;

public interface ReponseRepository extends JpaRepository<Reponse, Integer> {
    @Query("select r from Reponse r where r.projetEtape.id=:projet_etape_id")
    List<Reponse> findByReponseByProjetEtape(@Param("projet_etape_id") int projet_etape_id);
}
