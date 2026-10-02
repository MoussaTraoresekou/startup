package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wassa.mp.startup.model.ProjetEtape;

import java.util.List;

public interface ProjetEtapeRepository extends JpaRepository<ProjetEtape, Integer> {
    public List<ProjetEtape> findByProjetId(Integer projetId);
}
