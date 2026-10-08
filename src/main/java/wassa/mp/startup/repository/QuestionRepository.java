package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import wassa.mp.startup.model.Etape;
import wassa.mp.startup.model.Question;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
     @Query("select q from Question q where q.etape.id=:etape_id")
     public List<Question> findByEtape(@Param("etape_id") int etape_id);
}
