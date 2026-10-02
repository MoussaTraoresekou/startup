package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wassa.mp.startup.model.Etape;

public interface EtapeRepository extends JpaRepository<Etape,Integer> {

}
