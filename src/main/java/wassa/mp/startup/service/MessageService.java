package wassa.mp.startup.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.dto.EnvoyerMessageDTO;
import wassa.mp.startup.dto.MessageResponseDTO;
import wassa.mp.startup.model.Conversation;
import wassa.mp.startup.model.Message;
import wassa.mp.startup.model.User;
import wassa.mp.startup.repository.ConversationRepository;
import wassa.mp.startup.repository.MessageRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationService conversationService;

    public MessageService(MessageRepository messageRepository, ConversationRepository conversationRepository, ConversationService conversationService) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.conversationService = conversationService;
    }

    /**
     * Récupérer l'historique des messages d'une conversation.
     */
    @Transactional(readOnly = true)
    public List<MessageResponseDTO> historique(int conversationId, CustumUserDetail userConnecte) {

        Conversation conversation = conversationService.verifierAcces(conversationId, userConnecte);
        return messageRepository
                .findByConversationIdOrderByCreatedAtAsc(
                        conversation.getId())
                .stream()
                .map(this::convertirEnDTO)
                .toList();
    }

    /**
     * Envoyer un message dans une conversation.
     */
    @Transactional
    public MessageResponseDTO envoyer(int conversationId, EnvoyerMessageDTO dto, CustumUserDetail userConnecte) {

        // Vérifier que l'utilisateur participe à la conversation.
        Conversation conversation =
                conversationService.verifierAcces(
                        conversationId,
                        userConnecte);

        if (dto == null || dto.getContenu() == null || dto.getContenu().isBlank()) {

            throw new IllegalArgumentException("Le contenu du message est obligatoire");
        }

        String contenu = dto.getContenu().trim();

        User expediteur = userConnecte.getUser();

        Message message = new Message();

        message.setConversation(conversation);
        message.setSender(expediteur);
        message.setContenu(contenu);
        message.setCreatedAt(LocalDateTime.now());

        Message messageEnregistre =
                messageRepository.save(message);

        // Mettre à jour la date du dernier message.
        conversation.setLastMessageAt(
                messageEnregistre.getCreatedAt());

        conversationRepository.save(conversation);

        return convertirEnDTO(messageEnregistre);
    }

    /**
     * Convertir une entité Message en MessageDTO.
     */
    private MessageResponseDTO convertirEnDTO(Message message) {

        MessageResponseDTO dto = new MessageResponseDTO();

        dto.setId(message.getId());
        dto.setConversationId(
                message.getConversation().getId());

        dto.setExpediteurId(
                message.getSender().getId());

        dto.setExpediteurNom(
                message.getSender().getNomComplet());

        dto.setContenu(message.getContenu());
        dto.setDateEnvoi(message.getCreatedAt());
        dto.setDateLecture(message.getReadAt());

        return dto;
    }
}
