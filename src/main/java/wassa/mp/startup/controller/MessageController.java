package wassa.mp.startup.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.dto.EnvoyerMessageDTO;
import wassa.mp.startup.dto.MessageResponseDTO;
import wassa.mp.startup.service.MessageService;

import java.util.List;

@RestController
@RequestMapping("/api/conversations/{conversationId}/messages")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class MessageController {

    private final MessageService messageService;

    /**
     * Consulter l'historique des messages d'une conversation.
     */
    @GetMapping
    public List<MessageResponseDTO> historique(
            @PathVariable int conversationId,
            @AuthenticationPrincipal CustumUserDetail userConnecte) {

        return messageService.historique(
                conversationId,
                userConnecte
        );
    }

    /**
     * Envoyer un message dans une conversation.
     */
    @PostMapping
    public MessageResponseDTO envoyer(
            @PathVariable int conversationId,
            @RequestBody EnvoyerMessageDTO dto,
            @AuthenticationPrincipal CustumUserDetail userConnecte) {

        return messageService.envoyer(
                conversationId,
                dto,
                userConnecte
        );
    }
}

