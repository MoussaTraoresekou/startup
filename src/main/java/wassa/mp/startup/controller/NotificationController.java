package wassa.mp.startup.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.dto.NotificationResponseDto;
import wassa.mp.startup.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * Récupère l'historique complet des alertes de l'utilisateur connecté
     * URL : GET http://localhost:8080/api/notifications/historique
     */
    @GetMapping("/historique")
    public ResponseEntity<List<NotificationResponseDto>> getMonHistorique(
            @AuthenticationPrincipal CustumUserDetail userConnecter) {

        List<NotificationResponseDto> historique = notificationService.obtenirHistoriqueUtilisateur(userConnecter.getUser().getId());
        return ResponseEntity.ok(historique);
    }

    /**
     * Marque toutes les notifications comme lues (quand l'utilisateur ouvre sa cloche de notifications)
     * URL : PUT http://localhost:8080/api/notifications/marquer-lu
     */
    @PutMapping("/marquer-lu")
    public ResponseEntity<String> toutMarquerCommeLu(@AuthenticationPrincipal CustumUserDetail userConnecter) {
        notificationService.marquerToutCommeLu(userConnecter.getUser().getId());
        return ResponseEntity.ok("Toutes vos notifications ont été marquées comme lues.");
    }
}
