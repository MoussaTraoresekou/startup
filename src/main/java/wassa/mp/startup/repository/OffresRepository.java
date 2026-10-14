package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wassa.mp.startup.model.Offres;
import wassa.mp.startup.Enumeration.EnumOffre;

import java.util.List;

@Repository
public interface OffresRepository extends JpaRepository<Offres, Integer> {

    List<Offres> findByStatuOffre(EnumOffre statuOffre);

    List<Offres> findByProjetId(int projetId);
}
