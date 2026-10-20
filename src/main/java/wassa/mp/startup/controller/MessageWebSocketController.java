
package wassa.mp.startup.controller;
import java.security.Principal;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.dto.EnvoyerMessageDTO;
import wassa.mp.startup.dto.MessageResponseDTO;
import wassa.mp.startup.service.MessageService;

@Controller
public class MessageWebSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageWebSocketController(
            MessageService messageService,
            SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Recevoir, enregistrer et diffuser un message.
     */
    @MessageMapping("/conversations/{conversationId}/envoyer")
    public void envoyer(@DestinationVariable int conversationId, @Payload EnvoyerMessageDTO dto, Principal principal) {
        if (!(principal instanceof Authentication authentication) || !(authentication.getPrincipal()
                instanceof CustumUserDetail userConnecte)) {
            throw new AccessDeniedException(
                    "Utilisateur non authentifié."
            );
        }

        // Le service vérifie les participants et enregistre en base.
        MessageResponseDTO messageEnregistre = messageService.envoyer(conversationId, dto, userConnecte);
        // Diffuser le message enregistré aux abonnés de cette conversation.
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + conversationId,
                messageEnregistre
        );
    }
}
