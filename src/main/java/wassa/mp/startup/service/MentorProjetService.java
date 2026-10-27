
package wassa.mp.startup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.Enumeration.RoleDansProjet;
import wassa.mp.startup.Enumeration.StatutMembre;
import wassa.mp.startup.Enumeration.StatutDemandeMentoratEnum;
import wassa.mp.startup.dto.*;
import wassa.mp.startup.exception.OperationInterditeException;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.*;
import wassa.mp.startup.repository.*;

import java.time.LocalDateTime;
import java.util.List;
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
    private NotificationService notificationService;

    @Autowired
    private MembreProjetRepository membreProjetRepository;

    private MentorProjet obtenirOuReutiliserRelation(
            Mentor mentor,
            Projet projet,
            String description) {

        Optional<MentorProjet> relationExistante =
                mentorProjetRepository.findByMentorIdAndProjetId(
                        mentor.getId(),
                        projet.getId()
                );

        if (relationExistante.isPresent()) {
            MentorProjet relation = relationExistante.get();

            if (relation.getStatut()
                    == StatutDemandeMentoratEnum.EN_ATTENTE) {
                throw new OperationInterditeException(
                        "Une demande de mentorat est déjà en attente."
                );
            }

            if (relation.getStatut()
                    == StatutDemandeMentoratEnum.ACCEPTEE) {
                throw new OperationInterditeException(
                        "Ce mentor accompagne déjà ce projet."
                );
            }

            if (relation.getStatut()
                    == StatutDemandeMentoratEnum.REFUSEE) {
                relation.setDescription(description);
                relation.setStatut(
                        StatutDemandeMentoratEnum.EN_ATTENTE
                );
                relation.setDateDemande(LocalDateTime.now());
                relation.setDateTraitement(null);

                return mentorProjetRepository.save(relation);
            }

            throw new OperationInterditeException(
                    "Impossible de réutiliser cette relation."
            );
        }

        MentorProjet relation = new MentorProjet();
        relation.setMentor(mentor);
        relation.setProjet(projet);
        relation.setDescription(description);
        relation.setStatut(StatutDemandeMentoratEnum.EN_ATTENTE);
        relation.setDateDemande(LocalDateTime.now());

        return mentorProjetRepository.save(relation);
    }

    @Transactional
    public void mentorProposeMentorat(
            DemandeMentoratDto dto,
            CustumUserDetail userConnecter) {

        Mentor mentor = mentorRepository.findById(
                userConnecter.getUser().getId()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Profil mentor introuvable pour cet utilisateur."
                )
        );

        Projet projet = projetRepository.findById(dto.getProjetId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Le projet ciblé n'existe pas."
                        )
                );

        MentorProjet relation = obtenirOuReutiliserRelation(
                mentor,
                projet,
                dto.getDescription()
        );

        String message = "Le mentor "
                + userConnecter.getUser().getPrenom() + " "
                + userConnecter.getUser().getNom()
                + " propose d'accompagner votre projet : "
                + projet.getTitre();

        AlerteMentoratDto alerte = new AlerteMentoratDto(
                relation.getId(),
                message,
                userConnecter.getUser().getPrenom() + " "
                        + userConnecter.getUser().getNom(),
                "MENTOR",
                projet.getTitre(),
                LocalDateTime.now()
        );

        notificationService.notifierUtilisateur(
                projet.getPorteur(),
                alerte
        );
    }

    @Transactional
    public void porteurSolliciteMentor(
            InvitationMentorDto dto,
            CustumUserDetail userConnecter) {

        Projet projet = projetRepository.findById(dto.getProjetId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Le projet spécifié n'existe pas."
                        )
                );

        if (projet.getPorteur().getId()
                != userConnecter.getUser().getId()) {
            throw new OperationInterditeException(
                    "Vous n'êtes pas autorisé à inviter un mentor pour ce projet."
            );
        }

        Mentor mentor = mentorRepository.findById(dto.getMentorId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Le mentor sélectionné n'existe pas."
                        )
                );

        MentorProjet relation = obtenirOuReutiliserRelation(
                mentor,
                projet,
                dto.getMessageInvitation()
        );

        String message = "Le projet " + projet.getTitre()
                + " mené par "
                + userConnecter.getUser().getPrenom()
                + " vous sollicite pour devenir son mentor.";

        AlerteMentoratDto alerte = new AlerteMentoratDto(
                relation.getId(),
                message,
                userConnecter.getUser().getPrenom() + " "
                        + userConnecter.getUser().getNom(),
                "PORTEUR",
                projet.getTitre(),
                LocalDateTime.now()
        );

        notificationService.notifierUtilisateur(
                mentor,
                alerte
        );
    }

    @Transactional
    public void accepterDemandeMentorat(
            int mentorProjetId,
            CustumUserDetail userConnecter) {

        MentorProjet mentorProjet =
                mentorProjetRepository.findById(mentorProjetId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Cette demande de mentorat n'existe pas."
                                )
                        );

        if (mentorProjet.getStatut()
                != StatutDemandeMentoratEnum.EN_ATTENTE) {
            throw new OperationInterditeException(
                    "Cette demande a déjà été traitée. Statut actuel : "
                            + mentorProjet.getStatut()
            );
        }

        Projet projet = mentorProjet.getProjet();
        Mentor mentor = mentorProjet.getMentor();

        int idConnecter = userConnecter.getUser().getId();
        int idPorteur = projet.getPorteur().getId();
        int idMentor = mentor.getId();

        User destinataireAlerte;
        String messageConfirmation;
        String roleAuteur;

        if (idPorteur == idConnecter) {
            destinataireAlerte = mentor;
            messageConfirmation =
                    "Félicitations ! Le projet " + projet.getTitre()
                            + " a accepté votre proposition d'accompagnement.";
            roleAuteur = "PORTEUR";

        } else if (idMentor == idConnecter) {
            destinataireAlerte = projet.getPorteur();
            messageConfirmation =
                    "Bonne nouvelle ! Le mentor "
                            + userConnecter.getUser().getPrenom()
                            + " a accepté votre invitation pour le projet : "
                            + projet.getTitre();
            roleAuteur = "MENTOR";

        } else {
            throw new OperationInterditeException(
                    "Vous n'êtes pas autorisé à traiter cette demande."
            );
        }

        Optional<MembreProjet> membreExistant =
                membreProjetRepository.findByProjet_IdAndUser_Id(
                        projet.getId(),
                        mentor.getId()
                );

        if (membreExistant.isPresent()) {
            MembreProjet membre = membreExistant.get();
            membre.setRoleProjet(RoleDansProjet.MENTOR);
            membre.setStatut(StatutMembre.ACTIF);
            membreProjetRepository.save(membre);

        } else {
            MembreProjet membre = new MembreProjet();
            membre.setProjet(projet);
            membre.setUser(mentor);
            membre.setRoleProjet(RoleDansProjet.MENTOR);
            membre.setStatut(StatutMembre.ACTIF);
            membreProjetRepository.save(membre);
        }

        mentorProjet.setStatut(
                StatutDemandeMentoratEnum.ACCEPTEE
        );
        mentorProjet.setDateTraitement(LocalDateTime.now());
        mentorProjetRepository.save(mentorProjet);

        AlerteMentoratDto alerte = new AlerteMentoratDto(
                mentorProjet.getId(),
                messageConfirmation,
                userConnecter.getUser().getPrenom() + " "
                        + userConnecter.getUser().getNom(),
                roleAuteur,
                projet.getTitre(),
                LocalDateTime.now()
        );

        notificationService.notifierUtilisateur(
                destinataireAlerte,
                alerte
        );
    }

    @Transactional
    public void refuserDemandeMentorat(
            int mentorProjetId,
            CustumUserDetail userConnecter) {

        MentorProjet mentorProjet =
                mentorProjetRepository.findById(mentorProjetId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Cette demande de mentorat n'existe pas."
                                )
                        );

        if (mentorProjet.getStatut()
                != StatutDemandeMentoratEnum.EN_ATTENTE) {
            throw new OperationInterditeException(
                    "Cette demande a déjà été traitée. Statut actuel : "
                            + mentorProjet.getStatut()
            );
        }

        Projet projet = mentorProjet.getProjet();
        Mentor mentor = mentorProjet.getMentor();

        int idConnecter = userConnecter.getUser().getId();

        User destinataireAlerte;
        String messageRefus;
        String roleAuteur;

        if (projet.getPorteur().getId() == idConnecter) {
            destinataireAlerte = mentor;
            messageRefus = "Le projet " + projet.getTitre()
                    + " a décliné votre proposition d'accompagnement.";
            roleAuteur = "PORTEUR";

        } else if (mentor.getId() == idConnecter) {
            destinataireAlerte = projet.getPorteur();
            messageRefus = "Le mentor "
                    + userConnecter.getUser().getPrenom()
                    + " a décliné votre invitation pour le projet "
                    + projet.getTitre() + ".";
            roleAuteur = "MENTOR";

        } else {
            throw new OperationInterditeException(
                    "Vous n'êtes pas autorisé à traiter cette demande."
            );
        }

        mentorProjet.setStatut(
                StatutDemandeMentoratEnum.REFUSEE
        );
        mentorProjet.setDateTraitement(LocalDateTime.now());
        mentorProjetRepository.save(mentorProjet);

        AlerteMentoratDto alerte = new AlerteMentoratDto(
                mentorProjet.getId(),
                messageRefus,
                userConnecter.getUser().getPrenom() + " "
                        + userConnecter.getUser().getNom(),
                roleAuteur,
                projet.getTitre(),
                LocalDateTime.now()
        );

        notificationService.notifierUtilisateur(
                destinataireAlerte,
                alerte
        );
    }

    @Transactional(readOnly = true)
    public List<MentorDemandeResponseDto> obtenirDemandesPourMentor(
            int mentorId) {

        List<MentorProjet> demandes =
                mentorProjetRepository.findByMentorId(mentorId);

        return demandes.stream()
                .map(mp -> new MentorDemandeResponseDto(
                        mp.getId(),
                        mp.getProjet().getId(),
                        mp.getProjet().getTitre(),
                        mp.getProjet().getPorteur().getPrenom() + " "
                                + mp.getProjet().getPorteur().getNom(),
                        mp.getProjet().getSecteurActivite().getNom(),
                        mp.getDescription(),
                        mp.getDateDemande(),
                        mp.getStatut().toString()
                ))
                .toList();
    }
/*
    @Transactional(readOnly = true)
    public ProjetResponseDto obtenirDetailsFicheProjet(int projetId) {

        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Le projet demandé n'existe pas."
                        )
                );

        return new ProjetResponseDto(
                projet.getId(),
                projet.getTitre(),
                projet.getDescription(),
                projet.getPith_url(),
                projet.getQuota_propose(),
                projet.getSecteurActivite().getNom(),
                projet.getPorteur().getNomComplet(),
                projet.getDate_creation()
        );
    }


@Transactional(readOnly = true)
public ProjetResponseDto obtenirDetailsFicheProjet(
        int projetId,
        CustumUserDetail userConnecter) {

    Projet projet = projetRepository.findById(projetId)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Le projet demandé n'existe pas."
                    )
            );

    verifierMentorAccepte(projetId, userConnecter);

    return new ProjetResponseDto(
            projet.getId(),
            projet.getTitre(),
            projet.getDescription(),
            projet.getPith_url(),
            projet.getQuota_propose(),
            projet.getSecteurActivite().getNom(),
            projet.getPorteur().getNomComplet(),
            projet.getDate_creation()
    );
}


 */
@Transactional(readOnly = true)
public ProjetResponseDto obtenirDetailsFicheProjet(
        int projetId,
        CustumUserDetail userConnecter) {

    // 1. Vérifier que le projet existe
    Projet projet = projetRepository.findById(projetId)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Projet introuvable avec l'identifiant : " + projetId
            ));

    // 2. Vérifier que l'utilisateur est connecté
    if (userConnecter == null || userConnecter.getUser() == null) {
        throw new OperationInterditeException(
                "Vous devez être connecté pour consulter ce projet."
        );
    }

    int mentorId = userConnecter.getUser().getId();

    // 3. Vérifier qu'une invitation existe pour ce mentor et ce projet
    mentorProjetRepository
            .findByMentorIdAndProjetId(mentorId, projetId)
            .orElseThrow(() -> new OperationInterditeException(
                    "Vous devez avoir reçu une invitation pour consulter ce projet."
            ));

    // 4. Construire le DTO de réponse
    ProjetResponseDto response = new ProjetResponseDto(
            projet.getId(),
            projet.getTitre(),
            projet.getDescription(),
            projet.getPith_url(),
            projet.getQuota_propose(),
            projet.getSecteurActivite().getNom(),
            projet.getPorteur().getNomComplet(),
            projet.getDate_creation()
            //projet.getNombreLikes()
    );

    // 5. Ajouter les informations du pitch
    response.setPith_thumbnail_url(projet.getPith_thumbnail_url());
    response.setPith_type(projet.getPith_type());

    return response;
}

    @Transactional(readOnly = true)
    public List<ProjetResponseDto> obtenirStartupsAssigneesAuMentor(
            int mentorId) {

        List<MentorProjet> liaisons =
                mentorProjetRepository.findByMentorIdAndStatut(
                        mentorId,
                        StatutDemandeMentoratEnum.ACCEPTEE
                );

        return liaisons.stream()
                .map(mp -> new ProjetResponseDto(
                        mp.getProjet().getId(),
                        mp.getProjet().getTitre(),
                        mp.getProjet().getDescription(),
                        mp.getProjet().getPith_url(),
                        mp.getProjet().getQuota_propose(),
                        mp.getProjet().getSecteurActivite().getNom(),
                        mp.getProjet().getPorteur().getNomComplet(),
                        mp.getProjet().getDate_creation()
                ))
                .toList();
    }
    private void verifierMentorAccepte(
            int projetId,
            CustumUserDetail userConnecter) {

        if (userConnecter == null || userConnecter.getUser() == null) {
            throw new OperationInterditeException(
                    "Vous devez être connecté pour consulter ce projet."
            );
        }

        mentorProjetRepository
                .findByMentorIdAndProjetIdAndStatut(
                        userConnecter.getUser().getId(),
                        projetId,
                        StatutDemandeMentoratEnum.ACCEPTEE
                )
                .orElseThrow(() ->
                        new OperationInterditeException(
                                "Vous devez être un mentor accepté pour consulter ce projet."
                        )
                );
    }
}
