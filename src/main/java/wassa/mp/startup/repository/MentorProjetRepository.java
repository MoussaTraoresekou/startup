package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wassa.mp.startup.Enumeration.StatutDemandeMentoratEnum;
import wassa.mp.startup.model.MentorProjet;

import java.util.List;
import java.util.Optional;

public interface MentorProjetRepository extends JpaRepository<MentorProjet,Integer> {
    Optional<MentorProjet> findByMentorIdAndProjetId(int mentorId, int projetId);
    List<MentorProjet> findByMentorId(int mentorId);


    List<MentorProjet> findByMentorIdAndStatut(int mentorId, StatutDemandeMentoratEnum statutDemandeMentoratEnum);
}
