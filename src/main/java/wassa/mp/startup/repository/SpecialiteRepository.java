package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wassa.mp.startup.model.Specialite;

import java.util.Optional;

@Repository
public interface SpecialiteRepository extends JpaRepository<Specialite, Integer> {

    Optional<Specialite> findByLibelleIgnoreCase(String libelle);
}
