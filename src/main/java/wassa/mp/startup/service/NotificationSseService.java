package wassa.mp.startup.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationSseService {

    // Cette map garde en mémoire les connexions des utilisateurs connectés.
    // Clé = ID de l'utilisateur (User.id), Valeur = Le canal de communication ouvert (SseEmitter)
    private final Map<Integer, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * Crée et stocke une connexion en direct pour un utilisateur
     */
    public SseEmitter creerConnexion(int utilisateurId) {
        // Crée un canal qui reste ouvert (timeout de 30 minutes : 1800000 ms)
        SseEmitter emitter = new SseEmitter(1800000L);

        this.emitters.put(utilisateurId, emitter);

        // Nettoyage automatique de la mémoire si l'utilisateur ferme son onglet ou se déconnecte
        emitter.onCompletion(() -> this.emitters.remove(utilisateurId));
        emitter.onTimeout(() -> this.emitters.remove(utilisateurId));
        emitter.onError((e) -> this.emitters.remove(utilisateurId));

        return emitter;
    }

    /**
     * Envoie l'alerte DTO directement sur l'écran de l'utilisateur s'il est en ligne
     */
    public void envoyerNotificationEnTempsReel(int utilisateurId, Object alerteDto) {
        SseEmitter emitter = this.emitters.get(utilisateurId);
        if (emitter != null) {
            try {
                // Envoi immédiat des données formatées en JSON au Frontend
                emitter.send(SseEmitter.event()
                        .name("NOTIFICATION_ALERTE")
                        .data(alerteDto));
            } catch (IOException e) {
                // Si le canal est mort, on le supprime de la mémoire
                this.emitters.remove(utilisateurId);
            }
        }
    }
}
