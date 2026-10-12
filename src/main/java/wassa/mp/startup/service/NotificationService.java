
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
    private NotificationSseService sseService;

    @Transactional
    public void notifierUtilisateur(
            User destinataire,
            AlerteMentoratDto alerteDto) {

        // 1. Créer la notification persistante
        Notification notification = new Notification();

        notification.setMessage(alerteDto.getMessage());
        notification.setLu(false);
        notification.setDateCreation(LocalDateTime.now());
        notification.setUser(destinataire);

        // 2. Enregistrer la notification en base de données
        Notification notificationEnregistree =
                notificationRepository.save(notification);

        // 3. Construire le DTO correspondant à la notification enregistrée
        NotificationResponseDto notificationDto =
                new NotificationResponseDto(
                        notificationEnregistree.getId(),
                        notificationEnregistree.getMessage(),
                        notificationEnregistree.isLu(),
                        notificationEnregistree.getDateCreation()
                );

        // 4. Envoyer la notification persistante au destinataire via SSE
        sseService.envoyerNotificationEnTempsReel(
                destinataire.getId(),
                notificationDto
        );
    }
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> obtenirHistoriqueUtilisateur(
            int utilisateurId) {

        List<Notification> notifications =
                notificationRepository
                        .findByUtilisateurIdOrderByDateCreationDesc(
                                utilisateurId
                        );

        return notifications.stream()
                .map(notification -> new NotificationResponseDto(
                        notification.getId(),
                        notification.getMessage(),
                        notification.isLu(),
                        notification.getDateCreation()
                ))
                .toList();
    }
    @Transactional
    public void marquerToutCommeLu(int utilisateurId) {

        List<Notification> notifications =
                notificationRepository
                        .findByUtilisateurIdOrderByDateCreationDesc(
                                utilisateurId
                        );

        for (Notification notification : notifications) {
            if (!notification.isLu()) {
                notification.setLu(true);
            }
        }

        notificationRepository.saveAll(notifications);
    }
}
