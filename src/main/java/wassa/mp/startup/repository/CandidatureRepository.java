package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wassa.mp.startup.Enumeration.EnumCandidature;
import wassa.mp.startup.model.Candidature;

import java.util.List;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Integer> {

    List<Candidature> findByCollaborateurId(int collaborateurId);

    List<Candidature> findByOffresId(int offresId);

    List<Candidature> findByOffresIdAndStatuCandidature(int offresId, EnumCandidature statuCandidature);

    List<Candidature> findByOffresProjetIdAndStatuCandidature(int projetId, EnumCandidature enumCandidature);
    List<Candidature> findByOffresProjetId(int projetId);





}
