package wassa.mp.startup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wassa.mp.startup.Enumeration.EnumCandidature;
import wassa.mp.startup.dto.AlerteCandidature;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.Candidature;
import wassa.mp.startup.model.Collaborateur;
import wassa.mp.startup.repository.CandidatureRepository;
import wassa.mp.startup.repository.CollaborateurRepository;

@Service
public class CandidatureService {
    @Autowired
    private CandidatureRepository candidatureRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private CollaborateurRepository collaborateurRepository;

    public void validerCandidature(int id) {
        Candidature candidature = candidatureRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Candidature n'existe pas")
        );

        candidature.setStatuCandidature(EnumCandidature.ACCEPTEE);

        Collaborateur collab = candidature.getCollaborateur();
        collab.setDisponible(false);
        collaborateurRepository.save(collab);

        Candidature saved = candidatureRepository.save(candidature);

        String msg = "Félicitations " + collab.getPrenom() + " ! Votre candidature pour le projet "
                + saved.getOffres().getProjet().getTitre() + " a été acceptée. Vous faites maintenant partie de l'équipe.";

        AlerteCandidature alerteDto = new AlerteCandidature(
                saved.getId(),
                saved.getOffres().getProjet().getId(),
                saved.getOffres().getProjet().getTitre(),
                collab.getId(),
                collab.getNomComplet(),
                msg,
                java.time.LocalDateTime.now()
        );

        notificationService.notifierUtilisateur(collab, alerteDto);
    }

    public void refuserCandidature(int id) {
        Candidature candidature = candidatureRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Candidature n'existe pas")
        );

        candidature.setStatuCandidature(EnumCandidature.REFUSEE);
        Candidature saved = candidatureRepository.save(candidature);

        String msg = "Votre candidature pour le poste de " + saved.getOffres().getTitre()
                + " au sein du projet " + saved.getOffres().getProjet().getTitre() + " n'a pas été retenue.";

        AlerteCandidature alerteDto = new AlerteCandidature(
                saved.getId(),
                saved.getOffres().getProjet().getId(),
                saved.getOffres().getProjet().getTitre(),
                saved.getCollaborateur().getId(),
                saved.getCollaborateur().getNomComplet(),
                msg,
                java.time.LocalDateTime.now()
        );

        notificationService.notifierUtilisateur(saved.getCollaborateur(), alerteDto);
    }

}
