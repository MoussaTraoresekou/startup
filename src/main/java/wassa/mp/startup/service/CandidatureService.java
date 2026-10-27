package wassa.mp.startup.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import wassa.mp.startup.Enumeration.EnumCandidature;
import wassa.mp.startup.Enumeration.EnumOffre;
import wassa.mp.startup.Enumeration.RoleDansProjet;
import wassa.mp.startup.Enumeration.StatutMembre;
import wassa.mp.startup.dto.AlerteCandidature;
import wassa.mp.startup.exception.OperationInterditeException;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.Candidature;
import wassa.mp.startup.model.MembreProjet;
import wassa.mp.startup.model.Offres;
import wassa.mp.startup.repository.CandidatureRepository;
import wassa.mp.startup.repository.MembreProjetRepository;
import wassa.mp.startup.repository.OffresRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final NotificationService notificationService;
    private final OffresRepository offresRepository;
    private final MembreProjetRepository membreProjetRepository;

    @Transactional
    public void validerCandidature(int id) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cette candidature n'existe pas."));

        if (candidature.getStatuCandidature() != null
                && candidature.getStatuCandidature() != EnumCandidature.EN_ATTENTE) {
            throw new OperationInterditeException(
                    "Cette candidature a déjà été traitée.");
        }

        Offres offre = offresRepository.findById(candidature.getOffres().getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cette offre n'existe pas."));

        if (offre.getStatuOffre() == EnumOffre.CLOTUREE) {
            throw new OperationInterditeException(
                    "Cette offre est déjà clôturée.");
        }

        candidature.setStatuCandidature(EnumCandidature.ACCEPTEE);
        candidatureRepository.save(candidature);

        int projetId = offre.getProjet().getId();
        int candidatId = candidature.getPorteur().getId();

        Optional<MembreProjet> membreExistant =
                membreProjetRepository.findByProjet_IdAndUser_Id(
                        projetId, candidatId);

        if (membreExistant.isPresent()) {
            MembreProjet membre = membreExistant.get();
            membre.setRoleProjet(RoleDansProjet.COLLABORATEUR);
            membre.setStatut(StatutMembre.ACTIF);
            membreProjetRepository.save(membre);
        } else {
            MembreProjet membre = new MembreProjet();
            membre.setProjet(offre.getProjet());
            membre.setUser(candidature.getPorteur());
            membre.setRoleProjet(RoleDansProjet.COLLABORATEUR);
            membre.setStatut(StatutMembre.ACTIF);
            membreProjetRepository.save(membre);
        }

        offre.setStatuOffre(EnumOffre.CLOTUREE);
        offresRepository.save(offre);

        String message = "Félicitations "
                + candidature.getPorteur().getPrenom()
                + " ! Votre candidature pour le projet "
                + offre.getProjet().getTitre()
                + " a été acceptée.";

        AlerteCandidature alerte = new AlerteCandidature(
                candidature.getId(),
                projetId,
                offre.getProjet().getTitre(),
                candidatId,
                candidature.getPorteur().getNomComplet(),
                message,
                LocalDateTime.now()
        );

        notificationService.notifierUtilisateur(
                candidature.getPorteur(), alerte);
    }

    @Transactional
    public void refuserCandidature(int id) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cette candidature n'existe pas."));

        if (candidature.getStatuCandidature() != null
                && candidature.getStatuCandidature() != EnumCandidature.EN_ATTENTE) {
            throw new OperationInterditeException(
                    "Cette candidature a déjà été traitée.");
        }

        candidature.setStatuCandidature(EnumCandidature.REFUSEE);
        candidatureRepository.save(candidature);

        String message = "Votre candidature pour le projet "
                + candidature.getOffres().getProjet().getTitre()
                + " a été refusée.";

        AlerteCandidature alerte = new AlerteCandidature(
                candidature.getId(),
                candidature.getOffres().getProjet().getId(),
                candidature.getOffres().getProjet().getTitre(),
                candidature.getPorteur().getId(),
                candidature.getPorteur().getNomComplet(),
                message,
                LocalDateTime.now()
        );

        notificationService.notifierUtilisateur(
                candidature.getPorteur(), alerte);
    }
}