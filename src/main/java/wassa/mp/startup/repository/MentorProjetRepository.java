package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wassa.mp.startup.model.MentorProjet;

import java.util.Optional;

public interface MentorProjetRepository extends JpaRepository<MentorProjet,Integer> {
    Optional<MentorProjet> findByMentorIdAndProjetId(int mentorId, int projetId);

}
