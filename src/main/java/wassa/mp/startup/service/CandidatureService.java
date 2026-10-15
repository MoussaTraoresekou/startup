package wassa.mp.startup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wassa.mp.startup.Enumeration.EnumCandidature;
import wassa.mp.startup.dto.AlerteCandidature;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.Candidature;
import wassa.mp.startup.model.Porteur;
import wassa.mp.startup.repository.CandidatureRepository;
import wassa.mp.startup.repository.PorteurRepository;

@Service
public class CandidatureService {
    @Autowired
    private CandidatureRepository candidatureRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private PorteurRepository porteurRepository;
/*
    @Transactional
    public void validerCandidature(int id) {
        Candidature candidature = candidatureRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Candidature n'existe pas")
        );
        candidature.setStatuCandidature(EnumCandidature.ACCEPTEE);
        Porteur porteur = candidature.getPorteur();
        porteur.setDisponible(false);
        porteurRepository.save(porteur);

        Candidature saved = candidatureRepository.save(candidature);

        String msg = "Félicitations " + porteur.getPrenom() + " ! Votre candidature pour le projet "
                + saved.getOffres().getProjet().getTitre() + " a été acceptée. Vous faites maintenant partie de l'équipe.";

        AlerteCandidature alerteDto = new AlerteCandidature(
                saved.getId(),
                saved.getOffres().getProjet().getId(),
                saved.getOffres().getProjet().getTitre(),
                porteur.getId(),
                porteur.getNomComplet(),
                msg,
                java.time.LocalDateTime.now()
        );

        notificationService.notifierUtilisateur(porteur, alerteDto);
    }

 */
@Transactional
public void validerCandidature(int id) {
    Candidature candidature = candidatureRepository.findById(id).orElseThrow(
            () -> new ResourceNotFoundException("Candidature n'existe pas")
    );

    candidature.setStatuCandidature(EnumCandidature.ACCEPTEE);
    Candidature saved = candidatureRepository.save(candidature);

    String msg = "Félicitations " + saved.getPorteur().getPrenom() + " ! Votre candidature pour le projet "
            + saved.getOffres().getProjet().getTitre() + " a été acceptée.";

    AlerteCandidature alerteDto = new AlerteCandidature(
            saved.getId(),
            saved.getOffres().getProjet().getId(),
            saved.getOffres().getProjet().getTitre(),
            saved.getPorteur().getId(),
            saved.getPorteur().getNomComplet(),
            msg,
            java.time.LocalDateTime.now()
    );

    notificationService.notifierUtilisateur(saved.getPorteur(), alerteDto);
}

    @Transactional
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
                saved.getPorteur().getId(),
                saved.getPorteur().getNomComplet(),
                msg,
                java.time.LocalDateTime.now()
        );
        notificationService.notifierUtilisateur(saved.getPorteur(), alerteDto);
    }

}
