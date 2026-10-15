package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wassa.mp.startup.model.Porteur;

import java.util.List;

public interface PorteurRepository extends JpaRepository<Porteur, Integer> {
    List<Porteur> findByDisponibleTrue();

    List<Porteur> findByDisponibleTrueAndSpecialiteContainingIgnoreCaseOrBiographieContainingIgnoreCase(
            String keywordSpecialite,
            String keywordBiographie
    );

}
