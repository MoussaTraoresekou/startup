package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wassa.mp.startup.Enumeration.TypeEtapes;
import wassa.mp.startup.model.Etape;

import java.util.Collection;

public interface EtapeRepository extends JpaRepository<Etape,Integer> {

    Collection<Object> findByType(TypeEtapes type);
}
