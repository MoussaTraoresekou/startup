package wassa.mp.startup.controller;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.dto.ConversationDTO;
import wassa.mp.startup.service.ConversationService;
import java.util.List;
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * Ouvrir une conversation avec le porteur d'un projet.
     * Si une conversation existe déjà pour cet investisseur
     * et ce projet, elle doit être réutilisée.
     */
    @PostMapping("/projet/{projetId}")
    public ConversationDTO ouvrirConversation(@PathVariable int projetId, @AuthenticationPrincipal CustumUserDetail userConnecte) {
        return conversationService.ouvrirConversation(projetId, userConnecte);
    }

    /**
     * Récupérer les conversations de l'utilisateur connecté.
     * Fonctionne pour un investisseur comme pour un porteur.
     */
    @GetMapping("/mes-conversations")
    public List<ConversationDTO> mesConversations(@AuthenticationPrincipal CustumUserDetail userConnecte) {

        return conversationService.mesConversations(userConnecte);
    }
}

