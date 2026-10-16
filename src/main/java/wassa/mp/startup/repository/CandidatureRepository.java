package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import wassa.mp.startup.Enumeration.EnumCandidature;
import wassa.mp.startup.model.Candidature;

import java.util.List;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Integer> {

    List<Candidature> findByPorteurId(int collaborateurId);

    List<Candidature> findByOffresId(int offresId);

    List<Candidature> findByOffresIdAndStatuCandidature(int offresId, EnumCandidature statuCandidature);

    List<Candidature> findByOffresProjetIdAndStatuCandidature(int projetId, EnumCandidature enumCandidature);

    @Query("SELECT c FROM Candidature c WHERE c.offres.projet.id = :projetId AND c.statuCandidature = wassa.mp.startup.Enumeration.EnumCandidature.EN_ATTENTE ORDER BY c.id DESC")
    List<Candidature> findByOffresProjetId(@Param("projetId") int projetId);
    List<Candidature> findByOffresProjetIdAndStatuCandidatureOrderByIdDesc(int projetId, EnumCandidature statut);

    long countByPorteurIdAndStatuCandidature(int porteurId, EnumCandidature statut);
    boolean existsByPorteurIdAndOffresId(int porteurId, int offresId);




}
