package wassa.mp.startup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.dto.AlerteMentoratDto;
import wassa.mp.startup.dto.DemandeMentoratDto; // Contient projetId et description
import wassa.mp.startup.dto.InvitationMentorDto;
import wassa.mp.startup.exception.OperationInterditeException;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.*;
import wassa.mp.startup.Enumeration.StatutDemandeMentoratEnum;
import wassa.mp.startup.repository.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MentorProjetService {

    @Autowired
    private MentorProjetRepository mentorProjetRepository;

    @Autowired
    private ProjetRepository projetRepository;

    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    private NotificationService notificationService; // Centralise l'enregistrement et le SSE

    /**
     * Un Mentor connecté soumet sa candidature pour accompagner un projet
     */
    @Transactional
    public void mentorProposeMentorat(DemandeMentoratDto dto, CustumUserDetail userConnecter) {

        // 1. Récupération du profil Mentor de l'utilisateur connecté (grâce à l'héritage)
        Mentor mentor = mentorRepository.findById(userConnecter.getUser().getId()).orElseThrow(
                () -> new ResourceNotFoundException("Profil mentor introuvable pour cet utilisateur.")
        );

        // 2. Récupération du projet ciblé
        Projet projet = projetRepository.findById(dto.getProjetId()).orElseThrow(
                () -> new ResourceNotFoundException("Le projet ciblé n'existe pas.")
        );

        // 3. Sécurité : On évite qu'un même mentor envoie plusieurs demandes en attente sur le même projet
        Optional<MentorProjet> relationExistante = mentorProjetRepository.findByMentorIdAndProjetId(mentor.getId(), projet.getId());
        if (relationExistante.isPresent()) {
            throw new OperationInterditeException("Une demande d'accompagnement est déjà en cours ou a déjà été traitée pour ce projet.");
        }

        // 4. Enregistrement du contrat d'association en base de données
        MentorProjet mentorProjet = new MentorProjet();
        mentorProjet.setMentor(mentor);
        mentorProjet.setProjet(projet);
        mentorProjet.setDescription(dto.getDescription());
        mentorProjet.setStatut(StatutDemandeMentoratEnum.EN_ATTENTE);
        mentorProjet.setDateDemande(LocalDateTime.now());

        MentorProjet savedRelation = mentorProjetRepository.save(mentorProjet);

        // 5. Préparation du DTO d'alerte flexible pour le temps réel
        String messageNotification = "Le mentor " + userConnecter.getUser().getPrenom() + " " + userConnecter.getUser().getNom()
                + " propose d'accompagner votre projet : " + projet.getTitre();

        AlerteMentoratDto alerte = new AlerteMentoratDto(
                savedRelation.getId(), // ID de l'association pour les futurs boutons Accepter/Refuser
                messageNotification,
                userConnecter.getUser().getPrenom() + " " + userConnecter.getUser().getNom(),
                "MENTOR", // L'auteur de l'action est un mentor
                projet.getTitre(),
                LocalDateTime.now()
        );

        // 6. Déclenchement automatique de la persistance de l'alerte et du push SSE
        // Le destinataire est le Porteur du projet (qui est une sous-classe ou lié à User)
        User destinataire = projet.getPorteur();

        notificationService.notifierUtilisateur(destinataire, alerte);
    }
    /**
     * Un Porteur connecté sollicite/invite un Mentor spécifique à rejoindre son projet
     */
    @Transactional
    public void porteurSolliciteMentor(InvitationMentorDto dto, CustumUserDetail userConnecter) {

        // 1. Récupération du projet ciblé
        Projet projet = projetRepository.findById(dto.getProjetId()).orElseThrow(
                () -> new ResourceNotFoundException("Le projet spécifié n'existe pas.")
        );

        // 2. SÉCURITÉ : On vérifie que le projet appartient bien au Porteur actuellement connecté
        if (projet.getPorteur().getId() != userConnecter.getUser().getId()) {
            throw new IllegalArgumentException("Vous n'êtes pas autorisé à lancer une invitation pour ce projet.");
        }

        // 3. Récupération du profil du Mentor invité
        Mentor mentor = mentorRepository.findById(dto.getMentorId()).orElseThrow(
                () -> new ResourceNotFoundException("Le mentor sélectionné n'existe pas.")
        );

        // 4. SÉCURITÉ : Éviter les doublons (si une relation ou demande existe déjà)
        Optional<MentorProjet> relationExistante = mentorProjetRepository.findByMentorIdAndProjetId(mentor.getId(), projet.getId());
        if (relationExistante.isPresent()) {
            throw new OperationInterditeException("Une relation ou une demande d'accompagnement est déjà en cours avec ce mentor.");
        }

        // 5. Enregistrement de la demande d'association en base de données (Statut EN_ATTENTE)
        MentorProjet mentorProjet = new MentorProjet();
        mentorProjet.setMentor(mentor);
        mentorProjet.setProjet(projet);
        mentorProjet.setDescription(dto.getMessageInvitation());
        mentorProjet.setStatut(StatutDemandeMentoratEnum.EN_ATTENTE);
        mentorProjet.setDateDemande(LocalDateTime.now());

        MentorProjet savedRelation = mentorProjetRepository.save(mentorProjet);

        // 6. Préparation de l'objet d'alerte flexible pour le temps réel (SSE)
        String messageAlerte = "Le projet " + projet.getTitre() + " mené par " + userConnecter.getUser().getPrenom()
                + " vous sollicite pour devenir son mentor.";

        AlerteMentoratDto alerte = new AlerteMentoratDto(
                savedRelation.getId(), // ID de la ligne pour que le mentor puisse cliquer sur accepter/refuser
                messageAlerte,
                userConnecter.getUser().getPrenom() + " " + userConnecter.getUser().getNom(),
                "PORTEUR", // L'auteur de l'invitation est un porteur de projet
                projet.getTitre(),
                LocalDateTime.now()
        );

        // 7. ENVOI EN TEMPS RÉEL (SSE) 📡
        // La classe Mentor héritant de User, on extrait l'identifiant pour cibler son écran
        User destinataireMentor = mentor;

        notificationService.notifierUtilisateur(destinataireMentor, alerte);
    }
    /**
     * Accepter une demande de mentorat existante (Idempotent via PUT)
     */
    @Transactional
    public void accepterDemandeMentorat(int mentorProjetId, CustumUserDetail userConnecter) {
        // 1. Récupération de la demande
        MentorProjet mentorProjet = mentorProjetRepository.findById(mentorProjetId).orElseThrow(
                () -> new ResourceNotFoundException("Cette demande de mentorat n'existe pas.")
        );

        // 2. Sécurité : Vérifier que la demande est bien en attente
        if (mentorProjet.getStatut() != StatutDemandeMentoratEnum.EN_ATTENTE) {
            throw new OperationInterditeException("Cette demande a déjà été traitée (Statut actuel : " + mentorProjet.getStatut() + ").");
        }

        int idConnecter = userConnecter.getUser().getId();
        User destinataireAlerte = null;
        String messageConfirmation = "";

        // 3. Détermination de qui accepte et qui doit être notifié en retour
        if (mentorProjet.getProjet().getPorteur().getId() == idConnecter) {
            // CAS 1 : C'est le Porteur qui accepte le Mentor (La demande venait du Mentor)
            destinataireAlerte = mentorProjet.getMentor(); // Le mentor héritant de User
            messageConfirmation = "Félicitations ! Le projet " + mentorProjet.getProjet().getTitre()
                    + " a accepté votre proposition d'accompagnement.";

        } else if (mentorProjet.getMentor().getId() == idConnecter) {
            // CAS 2 : C'est le Mentor qui accepte le Projet (La demande venait du Porteur)
            destinataireAlerte = mentorProjet.getProjet().getPorteur();
            messageConfirmation = "Bonne nouvelle ! Le mentor " + userConnecter.getUser().getPrenom()
                    + " a accepté votre invitation pour le projet : " + mentorProjet.getProjet().getTitre();

        } else {
            // Sécurité : Un utilisateur tiers essaie de forcer l'acceptation
            throw new IllegalArgumentException("Vous n'êtes pas autorisé à traiter cette demande de mentorat.");
        }

        // 4. Mise à jour de la relation en base de données
        mentorProjet.setStatut(StatutDemandeMentoratEnum.ACCEPTEE);
        mentorProjet.setDateTraitement(LocalDateTime.now());
        mentorProjetRepository.save(mentorProjet);

        // 5. Envoi de la notification de confirmation au demandeur initial
        AlerteMentoratDto alerte = new AlerteMentoratDto(
                mentorProjet.getId(),
                messageConfirmation,
                userConnecter.getUser().getPrenom() + " " + userConnecter.getUser().getNom(),
                mentorProjet.getProjet().getPorteur().getId() == idConnecter ? "PORTEUR" : "MENTOR", // Qui a validé
                mentorProjet.getProjet().getTitre(),
                LocalDateTime.now()
        );

        notificationService.notifierUtilisateur(destinataireAlerte, alerte);
    }
    /**
     * Refuser une demande de mentorat existante (PUT)
     */
    @Transactional
    public void refuserDemandeMentorat(int mentorProjetId, CustumUserDetail userConnecter) {
        // 1. Récupération de la demande
        MentorProjet mentorProjet = mentorProjetRepository.findById(mentorProjetId).orElseThrow(
                () -> new ResourceNotFoundException("Cette demande de mentorat n'existe pas.")
        );

        // 2. Sécurité : Vérifier que la demande est bien en attente
        if (mentorProjet.getStatut() != StatutDemandeMentoratEnum.EN_ATTENTE) {
            throw new OperationInterditeException("Cette demande a déjà été traitée (Statut actuel : " + mentorProjet.getStatut() + ").");
        }

        int idConnecter = userConnecter.getUser().getId();
        User destinataireAlerte = null;
        String messageRefus = "";

        // 3. Détermination de qui refuse et qui doit être notifié en retour
        if (mentorProjet.getProjet().getPorteur().getId() == idConnecter) {
            // CAS 1 : Le Porteur refuse la proposition du Mentor
            destinataireAlerte = mentorProjet.getMentor();
            messageRefus = "Le projet " + mentorProjet.getProjet().getTitre() + " a décliné votre proposition d'accompagnement.";

        } else if (mentorProjet.getMentor().getId() == idConnecter) {
            // CAS 2 : Le Mentor refuse l'invitation du Porteur
            destinataireAlerte = mentorProjet.getProjet().getPorteur();
            messageRefus = "Le mentor " + userConnecter.getUser().getPrenom() + " a décliné votre invitation pour le projet " + mentorProjet.getProjet().getTitre() + ".";

        } else {
            throw new IllegalArgumentException("Vous n'êtes pas autorisé à traiter cette demande.");
        }

        // 4. Mise à jour en base de données
        mentorProjet.setStatut(StatutDemandeMentoratEnum.REFUSEE);
        mentorProjet.setDateTraitement(LocalDateTime.now());
        mentorProjetRepository.save(mentorProjet);

        // 5. Envoi de l'alerte de refus via le service de notification (Base + SSE)
        AlerteMentoratDto alerte = new AlerteMentoratDto(
                mentorProjet.getId(),
                messageRefus,
                userConnecter.getUser().getPrenom() + " " + userConnecter.getUser().getNom(),
                mentorProjet.getProjet().getPorteur().getId() == idConnecter ? "PORTEUR" : "MENTOR",
                mentorProjet.getProjet().getTitre(),
                LocalDateTime.now()
        );

        notificationService.notifierUtilisateur(destinataireAlerte, alerte);
    }



}
