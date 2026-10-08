package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wassa.mp.startup.model.ProjetEtape;

import java.util.List;
import java.util.Optional;

public interface ProjetEtapeRepository extends JpaRepository<ProjetEtape, Integer> {
    public List<ProjetEtape> findByProjetId(Integer projetId);
    Optional<ProjetEtape> findByProjetIdAndEtapeId(int projetId, int etapeId);
}
