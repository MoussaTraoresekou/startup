package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import wassa.mp.startup.model.Offres;
import wassa.mp.startup.Enumeration.EnumOffre;

import java.util.List;

@Repository
public interface OffresRepository extends JpaRepository<Offres, Integer> {

    List<Offres> findByStatuOffre(EnumOffre statuOffre);

    List<Offres> findByProjetId(int projetId);
    @Query("SELECT o FROM Offres o WHERE o.statuOffre = :statut " +
            "AND o.projet.porteur.id != :porteurId " +
            "AND o.specialite.id = :specialiteId")
    List<Offres> findMatchesPourLePorteur(@Param("statut") EnumOffre statut, @Param("porteurId") int porteurId, @Param("specialiteId") int specialiteId);
}
