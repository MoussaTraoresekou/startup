package wassa.mp.startup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wassa.mp.startup.dto.AlerteMentoratDto;
import wassa.mp.startup.dto.NotificationResponseDto;
import wassa.mp.startup.model.Notification;
import wassa.mp.startup.model.User;
import wassa.mp.startup.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationSseService sseService; // Le flux en direct développé plus haut

    /**
     * Crée une notification en base de données ET l'envoie en temps réel si possible.
     */
    @Transactional
    public void notifierUtilisateur(User destinataire, AlerteMentoratDto alerteDto) {

        // 1. Enregistrement persistant dans la base de données
        Notification notification = new Notification();
        notification.setMessage(alerteDto.getMessage());
        notification.setLu(false);
        notification.setDateCreation(LocalDateTime.now());
        notification.setUser(destinataire);

        notificationRepository.save(notification);

        // 2. Propagation instantanée en temps réel (SSE)
        sseService.envoyerNotificationEnTempsReel(destinataire.getId(), alerteDto);
    }
    /**
     * Récupère l'historique des notifications formaté en DTO pour l'utilisateur connecté
     */
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> obtenirHistoriqueUtilisateur(int utilisateurId) {
        List<Notification> notifications = notificationRepository.findByUtilisateurIdOrderByDateCreationDesc(utilisateurId);

        // Conversion de la liste d'entités en liste de DTOs
        return notifications.stream().map(n -> new NotificationResponseDto(
                n.getId(),
                n.getMessage(),
                n.isLu(),
                n.getDateCreation()
        )).toList();
    }

    /**
     * Passe toutes les notifications non lues d'un utilisateur à l'état LU
     */
    @Transactional
    public void marquerToutCommeLu(int utilisateurId) {
        List<Notification> notifications = notificationRepository.findByUtilisateurIdOrderByDateCreationDesc(utilisateurId);
        for (Notification n : notifications) {
            if (!n.isLu()) {
                n.setLu(true);
            }
        }
        notificationRepository.saveAll(notifications);
    }

}
