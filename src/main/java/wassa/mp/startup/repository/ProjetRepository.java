package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wassa.mp.startup.model.Projet;
public interface ProjetRepository extends JpaRepository<Projet, Integer> {
}
