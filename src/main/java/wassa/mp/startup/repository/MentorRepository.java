package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wassa.mp.startup.Enumeration.StatuEnumMentor;
import wassa.mp.startup.model.Mentor;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentorRepository extends JpaRepository<Mentor, Integer> {
    Optional<Mentor> findById(int id);
    // Récupère la liste de tous les mentors possédant un statut spécifique
    List<Mentor> findByStatuEnumMentor(StatuEnumMentor statut);
}
