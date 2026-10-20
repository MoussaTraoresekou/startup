package wassa.mp.startup.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import wassa.mp.startup.model.Conversation;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {

    Optional<Conversation> findByInvestisseurIdAndProjetId(int investisseurId, int projetId);

    List<Conversation> findByInvestisseurIdOrderByLastMessageAtDesc(int investisseurId);

    List<Conversation> findByPorteurIdOrderByLastMessageAtDesc(int porteurId);
}
