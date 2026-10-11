        package wassa.mp.startup.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import wassa.mp.startup.dto.NotificationResponseDto;

@Service
public class NotificationSseService {

    private final Map<Integer, SseEmitter> emitters =
            new ConcurrentHashMap<>();

    public SseEmitter creerConnexion(int utilisateurId) {
        SseEmitter emitter = new SseEmitter(1800000L);

        SseEmitter ancien = emitters.put(utilisateurId, emitter);

        if (ancien != null) {
            ancien.complete();
        }

        emitter.onCompletion(() -> emitters.remove(utilisateurId, emitter));
        emitter.onTimeout(() -> {
            emitters.remove(utilisateurId, emitter);
            emitter.complete();
        });
        emitter.onError(e -> emitters.remove(utilisateurId, emitter));

        return emitter;
    }

    /**
     * Envoie une notification persistante au frontend.
     * La notification doit déjà avoir été enregistrée en base.
     */
    public void envoyerNotificationEnTempsReel(
            int utilisateurId,
            NotificationResponseDto notificationDto) {

        SseEmitter emitter = emitters.get(utilisateurId);

        if (emitter == null) {
            return;
        }

        try {
            emitter.send(
                    SseEmitter.event()
                            .name("NOTIFICATION_ALERTE")
                            .data(notificationDto)
            );
        } catch (IOException | IllegalStateException e) {
            emitters.remove(utilisateurId, emitter);
            emitter.completeWithError(e);
        }
    }
}
