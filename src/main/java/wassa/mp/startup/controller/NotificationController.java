package wassa.mp.startup.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.dto.NotificationResponseDto;
import wassa.mp.startup.service.NotificationService;
import wassa.mp.startup.service.NotificationSseService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private NotificationSseService sseService;
    @GetMapping("/historique")
    public ResponseEntity<List<NotificationResponseDto>> getMonHistorique(
            @AuthenticationPrincipal CustumUserDetail userConnecter) {

        List<NotificationResponseDto> historique = notificationService.obtenirHistoriqueUtilisateur(userConnecter.getUser().getId());
        return ResponseEntity.ok(historique);
    }

    @PutMapping("/marquer-lu")
    public ResponseEntity<String> toutMarquerCommeLu(@AuthenticationPrincipal CustumUserDetail userConnecter) {
        notificationService.marquerToutCommeLu(userConnecter.getUser().getId());
        return ResponseEntity.ok("Toutes vos notifications ont été marquées comme lues.");
    }
    @GetMapping(value = "/flux", produces = "text/event-stream")
    public SseEmitter sabonnerAuFlux(
            @AuthenticationPrincipal CustumUserDetail userConnecter) {

        if (userConnecter == null) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Utilisateur non authentifié"
            );
        }

        int userId = userConnecter.getUser().getId();

        return sseService.creerConnexion(userId);
    }
}
