package wassa.mp.startup.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import wassa.mp.startup.model.Message;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByConversationIdOrderByCreatedAtAsc(int conversationId);
    Optional<Message> findFirstByConversationIdOrderByCreatedAtDesc(int conversationId);
}