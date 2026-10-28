
package wassa.mp.startup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;
import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.Enumeration.StatutMembre;
import wassa.mp.startup.Enumeration.StatutTache;
import wassa.mp.startup.dto.TacheCreationDto;
import wassa.mp.startup.dto.TacheRenduDto;
import wassa.mp.startup.dto.TacheResponseDto;
import wassa.mp.startup.dto.TacheVerificationDto;
import wassa.mp.startup.exception.NonAutoriseException;
import wassa.mp.startup.exception.OperationInterditeException;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.MembreProjet;
import wassa.mp.startup.model.Projet;
import wassa.mp.startup.model.Tache;
import wassa.mp.startup.model.User;
import wassa.mp.startup.repository.MembreProjetRepository;
import wassa.mp.startup.repository.ProjetRepository;
import wassa.mp.startup.repository.TacheRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TacheService {

    @Autowired
    private TacheRepository tacheRepository;

    @Autowired
    private ProjetRepository projetRepository;

    @Autowired
    private MembreProjetRepository membreProjetRepository;
    @Autowired
    private StockageFichierService stockageFichierService;
    @Transactional
    public TacheResponseDto creerTache(
            int projetId,
            TacheCreationDto dto,
            CustumUserDetail userConnecter
    ) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Le projet demandé n'existe pas."
                        )
                );

        if (projet.getPorteur().getId()
                != userConnecter.getUser().getId()) {
            throw new NonAutoriseException(
                    "Seul le porteur du projet peut créer une tâche."
            );
        }

        MembreProjet membre = membreProjetRepository
                .findById(dto.getMembreResponsableId())
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Le membre responsable demandé n'existe pas."
                        )
                );

        if (membre.getProjet().getId() != projetId) {
            throw new OperationInterditeException(
                    "Le membre responsable n'appartient pas à ce projet."
            );
        }

        if (membre.getStatut() != StatutMembre.ACTIF) {
            throw new OperationInterditeException(
                    "Impossible d'attribuer une tâche à un membre inactif."
            );
        }

        Tache tache = new Tache();
        tache.setTitre(dto.getTitre());
        tache.setDescription(dto.getDescription());
        tache.setDateEcheance(dto.getDateEcheance());
        tache.setDateCreation(LocalDateTime.now());
        tache.setStatut(StatutTache.A_FAIRE);
        tache.setProjet(projet);
        tache.setMembreResponsable(membre);

        Tache sauvegardee = tacheRepository.save(tache);

        return convertirEnDto(sauvegardee);
    }
    /*
    @Transactional
    public TacheResponseDto soumettreRendu(
            int tacheId,
            TacheRenduDto dto,
            CustumUserDetail userConnecter
    ) {
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La tâche demandée n'existe pas."
                ));

        if (userConnecter == null || userConnecter.getUser() == null) {
            throw new NonAutoriseException(
                    "Vous devez être connecté pour soumettre un rendu."
            );
        }

        if (tache.getMembreResponsable().getUser().getId()
                != userConnecter.getUser().getId()) {
            throw new NonAutoriseException(
                    "Seul le responsable de cette tâche peut soumettre son rendu."
            );
        }

        if (tache.getStatut() != StatutTache.A_FAIRE
                && tache.getStatut() != StatutTache.A_CORRIGER) {
            throw new OperationInterditeException(
                    "Cette tâche ne peut pas être soumise dans son état actuel."
            );
        }

        if (dto == null
                || dto.getRenduDescription() == null
                || dto.getRenduDescription().isBlank()) {
            throw new OperationInterditeException(
                    "La description du rendu est obligatoire."
            );
        }

        tache.setRenduDescription(dto.getRenduDescription().trim());

        tache.setRenduUrl(
                dto.getRenduUrl() == null || dto.getRenduUrl().isBlank()
                        ? null
                        : dto.getRenduUrl().trim()
        );

        tache.setDateSoumission(LocalDateTime.now());
        tache.setStatut(StatutTache.SOUMIS);

        return convertirEnDto(tacheRepository.save(tache));
    }

     */
    @Transactional
    public TacheResponseDto soumettreRendu(
            int tacheId,
            String renduDescription,
            MultipartFile fichier,
            CustumUserDetail userConnecter
    ) {
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La tâche demandée n'existe pas."
                ));

        // Vérifier la connexion
        if (userConnecter == null || userConnecter.getUser() == null) {
            throw new NonAutoriseException(
                    "Vous devez être connecté pour soumettre un rendu."
            );
        }

        // Seul le responsable de la tâche peut soumettre le rendu
        if (tache.getMembreResponsable().getUser().getId()
                != userConnecter.getUser().getId()) {
            throw new NonAutoriseException(
                    "Seul le responsable de cette tâche peut soumettre son rendu."
            );
        }

        // Vérifier le statut de la tâche
        if (tache.getStatut() != StatutTache.A_FAIRE
                && tache.getStatut() != StatutTache.A_CORRIGER) {
            throw new OperationInterditeException(
                    "Cette tâche ne peut pas être soumise dans son état actuel."
            );
        }

        // Vérifier la description
        if (renduDescription == null || renduDescription.isBlank()) {
            throw new OperationInterditeException(
                    "La description du rendu est obligatoire."
            );
        }

        // Vérifier le fichier
        if (fichier == null || fichier.isEmpty()) {
            throw new OperationInterditeException(
                    "Veuillez sélectionner un fichier pour le rendu."
            );
        }

        // Enregistrer le fichier dans le stockage local
        String nomFichier = stockageFichierService.enregistrerRendu(fichier);

        try {
            tache.setRenduDescription(renduDescription.trim());
            tache.setRenduUrl(nomFichier);
            tache.setDateSoumission(LocalDateTime.now());
            tache.setStatut(StatutTache.SOUMIS);

            Tache tacheSauvegardee = tacheRepository.save(tache);

            return convertirEnDto(tacheSauvegardee);

        } catch (RuntimeException e) {
            // Éviter de conserver le nouveau fichier si l'enregistrement échoue
            try {
                stockageFichierService.supprimerPitch(nomFichier);
            } catch (RuntimeException erreurSuppression) {
                // Ne pas masquer l'erreur initiale
            }

            throw e;
        }
    }

    private TacheResponseDto convertirEnDto(Tache tache) {
        User utilisateur = tache.getMembreResponsable().getUser();

        return new TacheResponseDto(
                tache.getId(),
                tache.getTitre(),
                tache.getDescription(),
                tache.getProjet().getId(),
                tache.getProjet().getTitre(),
                tache.getMembreResponsable().getId(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                tache.getDateCreation(),
                tache.getDateEcheance(),
                tache.getStatut(),
                tache.getRenduDescription(),
                tache.getRenduUrl(),
                tache.getDateSoumission(),
                tache.getCommentaireVerification(),
                tache.getDateVerification()
        );
    }
    @Transactional
    public TacheResponseDto verifierRendu(
            int tacheId,
            TacheVerificationDto dto,
            CustumUserDetail userConnecter
    ) {
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La tâche demandée n'existe pas."
                ));

        verifierPorteurProjet(tache, userConnecter);

        if (tache.getStatut() != StatutTache.SOUMIS) {
            throw new OperationInterditeException(
                    "Seule une tâche soumise peut être vérifiée."
            );
        }

        if (dto == null || dto.getStatut() == null) {
            throw new OperationInterditeException(
                    "Le statut de vérification est obligatoire."
            );
        }

        if (dto.getStatut() != StatutTache.VALIDEE
                && dto.getStatut() != StatutTache.A_CORRIGER) {
            throw new OperationInterditeException(
                    "Le statut doit être VALIDEE ou A_CORRIGER."
            );
        }

        if (dto.getStatut() == StatutTache.A_CORRIGER
                && (dto.getCommentaireVerification() == null
                || dto.getCommentaireVerification().isBlank())) {
            throw new OperationInterditeException(
                    "Le commentaire est obligatoire pour demander une correction."
            );
        }

        tache.setStatut(dto.getStatut());
        tache.setCommentaireVerification(
                dto.getCommentaireVerification() == null
                        || dto.getCommentaireVerification().isBlank()
                        ? null
                        : dto.getCommentaireVerification().trim()
        );
        tache.setDateVerification(LocalDateTime.now());

        return convertirEnDto(tacheRepository.save(tache));
    }
    private void verifierPorteurProjet(
            Tache tache,
            CustumUserDetail userConnecter
    ) {
        if (userConnecter == null || userConnecter.getUser() == null) {
            throw new NonAutoriseException(
                    "Vous devez être connecté pour effectuer cette action."
            );
        }

        int userId = userConnecter.getUser().getId();
        int porteurId = tache.getProjet().getPorteur().getId();

        if (userId != porteurId) {
            throw new NonAutoriseException(
                    "Seul le porteur du projet peut vérifier ce rendu."
            );
        }
    }
    @Transactional(readOnly = true)
    public List<TacheResponseDto> obtenirTachesProjet(
            int projetId,
            CustumUserDetail userConnecter
    ) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Le projet demandé n'existe pas."
                ));

        if (userConnecter == null || userConnecter.getUser() == null) {
            throw new NonAutoriseException(
                    "Vous devez être connecté pour consulter les tâches."
            );
        }

        if (projet.getPorteur().getId()
                != userConnecter.getUser().getId()) {
            throw new NonAutoriseException(
                    "Seul le porteur du projet peut consulter ses tâches."
            );
        }

        return tacheRepository.findByProjet_Id(projetId)
                .stream()
                .map(this::convertirEnDto)
                .toList();
    }
    @Transactional(readOnly = true)
    public List<TacheResponseDto> obtenirMesTaches(
            CustumUserDetail userConnecter
    ) {
        if (userConnecter == null || userConnecter.getUser() == null) {
            throw new NonAutoriseException(
                    "Vous devez être connecté pour consulter vos tâches."
            );
        }

        int userId = userConnecter.getUser().getId();

        return tacheRepository
                .findByMembreResponsable_User_IdOrderByDateCreationDesc(userId)
                .stream()
                .map(this::convertirEnDto)
                .toList();
    }
    @Transactional(readOnly = true)
    public void verifierAccesRendu(
            String nomFichier,
            CustumUserDetail userConnecter
    ) {
        if (userConnecter == null || userConnecter.getUser() == null) {
            throw new NonAutoriseException(
                    "Vous devez être connecté pour consulter ce rendu."
            );
        }

        Tache tache = tacheRepository.findByRenduUrl(nomFichier)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucun rendu ne correspond à ce fichier."
                ));

        int userId = userConnecter.getUser().getId();

        boolean estPorteur =
                tache.getProjet().getPorteur().getId() == userId;

        boolean estResponsable =
                tache.getMembreResponsable().getUser().getId() == userId;

        if (!estPorteur && !estResponsable) {
            throw new NonAutoriseException(
                    "Vous n'êtes pas autorisé à consulter ce rendu."
            );
        }
    }

}
