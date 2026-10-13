package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wassa.mp.startup.model.Collaborateur;

import java.util.List;

@Repository
public interface CollaborateurRepository extends JpaRepository<Collaborateur, Integer> {

    List<Collaborateur> findByDisponibleTrue();

    List<Collaborateur> findByDisponibleTrueAndSpecialiteContainingIgnoreCaseOrBiographieContainingIgnoreCase(
            String keywordSpecialite,
            String keywordBiographie
    );
}
