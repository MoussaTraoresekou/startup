
package wassa.mp.startup.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.Enumeration.EnumCandidature;
import wassa.mp.startup.Enumeration.StatutDemandeMentoratEnum;
import wassa.mp.startup.Enumeration.StatutMembre;
import wassa.mp.startup.dto.MembreProjetDto;
import wassa.mp.startup.dto.ProjetRequestDto;
import wassa.mp.startup.dto.ProjetResponseDto;
import wassa.mp.startup.exception.NonAutoriseException;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.*;
import wassa.mp.startup.repository.*;

import java.util.*;

@Service
public class ProjetService {

    private static final Logger logger =
            LoggerFactory.getLogger(ProjetService.class);

    @Autowired
    private ProjetRepository projetRepository;

    @Autowired
    private SecteurActiviteRepository secteurActiviteRepository;

    @Autowired
    private MentorProjetRepository mentorProjetRepository;

    @Autowired
    private CandidatureRepository candidatureRepository;

    @Autowired
    private ProjetLikeRepository projetLikeRepository;

    @Autowired
    private MembreProjetRepository membreProjetRepository;

    @Autowired
    private StockageFichierService stockageFichierService;

    /*
     * Ancienne version de getAllProjets conservée pour référence.
     *
     * public List<ProjetResponseDto> getAllProjets(
     *         @AuthenticationPrincipal CustumUserDetail userConnecter) {
     *
     *     return projetRepository.findByPorteur(userConnecter.getUser().getId())
     *             .stream()
     *             .map(projet -> new ProjetResponseDto(
     *                     projet.getId(),
     *                     projet.getTitre(),
     *                     projet.getDescription(),
     *                     projet.getPith_url(),
     *                     projet.getQuota_propose(),
     *                     projet.getSecteurActivite().getNom(),
     *                     userConnecter.getUser().getNomComplet(),
     *                     projet.getDate_creation()))
     *             .toList();
     * }
     */

    @Transactional
    public List<ProjetResponseDto> getAllProjets(
            @AuthenticationPrincipal CustumUserDetail userConnecter) {

        List<Projet> projets = projetRepository.findByPorteur(
                userConnecter.getUser().getId()
        );

        Map<Integer, Long> likes = compterLikes(projets);
        String nomPorteur = userConnecter.getUser().getNomComplet();

        return projets.stream()
                .map(projet -> versDto(projet, nomPorteur, likes))
                .toList();
    }

    /**
     * Retourne tous les projets lorsqu'un investisseur
     * n'a choisi aucun secteur.
     */
    @Transactional
    public List<ProjetResponseDto> obtenirTousLesProjets() {

        List<Projet> projets = projetRepository.findAll();
        Map<Integer, Long> likes = compterLikes(projets);

        return projets.stream()
                .map(projet -> versDto(projet, nomPorteur(projet), likes))
                .toList();
    }

    /*
     * Ancienne méthode de création conservée pour référence.
     * Elle n'est plus utilisée, car le pitch est maintenant reçu
     * sous forme de MultipartFile.
     *
     * @Transactional
     * public Projet creaProjet(
     *         ProjetRequestDto projetRequestDto,
     *         @AuthenticationPrincipal CustumUserDetail userConnecter) {
     *
     *     Porteur porteurActuel = (Porteur) userConnecter.getUser();
     *
     *     long nombreProjetsExistants =
     *             projetRepository.countByPorteurId(porteurActuel.getId());
     *
     *     if (nombreProjetsExistants >= 2) {
     *         throw new IllegalArgumentException(
     *                 "Limite atteinte : Vous ne pouvez pas créer plus de 2 projets dans l'incubateur.");
     *     }
     *
     *     Projet projet = new Projet();
     *     projet.setDescription(projetRequestDto.getDescription());
     *     projet.setPith_url(projetRequestDto.getPith_url());
     *     projet.setTitre(projetRequestDto.getTitre());
     *     projet.setQuota_propose(projetRequestDto.getQuota_propose());
     *     projet.setPorteur(porteurActuel);
     *
     *     projet.setSecteurActivite(
     *             secteurActiviteRepository.findById(
     *                     projetRequestDto.getSecteur_id())
     *                     .orElseThrow(() ->
     *                             new ResourceNotFoundException(
     *                                     "Secteur d'activité introuvable"))
     *     );
     *
     *     return projetRepository.save(projet);
     * }
     */

    /**
     * Crée un projet et enregistre éventuellement son pitch localement.
     */
    @Transactional
    public Projet creaProjet(
            ProjetRequestDto projetRequestDto,
            MultipartFile fichierPitch,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {

        Porteur porteurActuel = (Porteur) userConnecter.getUser();

        long nombreProjetsExistants =
                projetRepository.countByPorteurId(porteurActuel.getId());

        if (nombreProjetsExistants >= 2) {
            throw new IllegalArgumentException(
                    "Limite atteinte : Vous ne pouvez pas créer plus de 2 projets dans l'incubateur."
            );
        }

        Projet projet = new Projet();

        projet.setDescription(projetRequestDto.getDescription());
        projet.setTitre(projetRequestDto.getTitre());
        projet.setQuota_propose(projetRequestDto.getQuota_propose());
        projet.setPorteur(porteurActuel);

        projet.setSecteurActivite(
                secteurActiviteRepository.findById(
                        projetRequestDto.getSecteur_id()
                ).orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Secteur d'activité introuvable"
                        )
                )
        );

        if (fichierPitch != null && !fichierPitch.isEmpty()) {

            String nouveauNom =
                    stockageFichierService.enregistrerPitch(fichierPitch);

            projet.setPith_url(nouveauNom);

            // Si la transaction échoue, supprimer le nouveau fichier.
            programmerNettoyageApresTransaction(nouveauNom, null);

        } else {
            // Le pitch est facultatif.
            projet.setPith_url(null);
        }

        return projetRepository.save(projet);
    }

    /**
     * Retourne les détails d'un projet appartenant au porteur connecté.
     */
    @Transactional
    public ProjetResponseDto getProjetByid(
            int id,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {

        Projet projet = projetRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Projet n'existe pas"
                        )
                );

        if (projet.getPorteur().getId()
                != userConnecter.getUser().getId()) {

            throw new NonAutoriseException(
                    "Vous n'êtes pas autorisé à consulter ce projet"
            );
        }

        return versDtoAvecLikes(projet);
    }

    /*
     * Anciennes versions de modifierProjet conservées pour référence.
     * La méthode active ci-dessous accepte désormais MultipartFile.
     */

    /**
     * Modifie un projet et remplace son pitch uniquement lorsqu'un
     * nouveau fichier est fourni. Sans nouveau fichier, le pitch
     * existant est conservé.
     */
    @Transactional
    public ProjetResponseDto modifierProjet(
            int id,
            @AuthenticationPrincipal CustumUserDetail userConnecter,
            ProjetRequestDto projetRequestDto,
            MultipartFile fichierPitch) {

        Projet projet = projetRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Projet n'existe pas"
                        )
                );

        if (projet.getPorteur().getId()
                != userConnecter.getUser().getId()) {

            throw new NonAutoriseException(
                    "Vous n'êtes pas autorisé à modifier ce projet"
            );
        }

        projet.setQuota_propose(projetRequestDto.getQuota_propose());
        projet.setDescription(projetRequestDto.getDescription());
        projet.setTitre(projetRequestDto.getTitre());

        projet.setSecteurActivite(
                secteurActiviteRepository.findById(
                        projetRequestDto.getSecteur_id()
                ).orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Secteur d'activité introuvable"
                        )
                )
        );

        // Garder le nom actuel pour pouvoir le supprimer après validation.
        String ancienNom = projet.getPith_url();

        if (fichierPitch != null && !fichierPitch.isEmpty()) {

            String nouveauNom =
                    stockageFichierService.enregistrerPitch(fichierPitch);

            projet.setPith_url(nouveauNom);

            // Transaction validée : supprimer l'ancien fichier.
            // Transaction annulée : supprimer le nouveau fichier.
            programmerNettoyageApresTransaction(
                    nouveauNom,
                    ancienNom
            );
        }

        Projet projetModifier = projetRepository.save(projet);

        return versDtoAvecLikes(projetModifier);
    }

    /*
     * Anciennes versions de calculerEquipeProjetComplete conservées
     * dans l'historique du projet. La version active utilise
     * MembreProjet et les membres dont le statut est ACTIF.
     */

    /**
     * Retourne l'équipe complète d'un projet.
     * Seul le porteur du projet peut consulter cette équipe.
     */
    @Transactional
    public List<MembreProjetDto> calculerEquipeProjetComplete(
            int projetId,
            CustumUserDetail userConnecter) {

        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Projet introuvable"
                        )
                );

        if (projet.getPorteur().getId()
                != userConnecter.getUser().getId()) {

            throw new NonAutoriseException(
                    "Vous n'êtes pas autorisé à consulter l'équipe de ce projet."
            );
        }

        List<MembreProjet> membres =
                membreProjetRepository.findByProjet_IdAndStatut(
                        projetId,
                        StatutMembre.ACTIF
                );

        return membres.stream()
                .map(membre -> new MembreProjetDto(
                        membre.getId(),
                        membre.getProjet().getId(),
                        membre.getUser().getId(),
                        membre.getUser().getNom(),
                        membre.getUser().getPrenom(),
                        membre.getUser().getRole() != null
                                ? membre.getUser().getRole().name()
                                : null,
                        null, // User ne possède pas de spécialité ici.
                        membre.getRoleProjet(),
                        membre.getFonction(),
                        membre.getResponsabilites(),
                        membre.getDateIntegration(),
                        membre.getStatut()
                ))
                .toList();
    }

    /**
     * Retourne les projets appartenant aux secteurs sélectionnés.
     */
    @Transactional
    public List<ProjetResponseDto> obtenirProjetsParSecteurs(
            List<Integer> secteurIds) {

        if (secteurIds == null || secteurIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Projet> projets =
                projetRepository.findBySecteurActiviteIdIn(secteurIds);

        Map<Integer, Long> likes = compterLikes(projets);

        return projets.stream()
                .map(projet -> versDto(projet, nomPorteur(projet), likes))
                .toList();
    }

    /**
     * Compte les likes de plusieurs projets en une seule opération.
     */
    private Map<Integer, Long> compterLikes(List<Projet> projets) {

        if (projets.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Integer> ids =
                projets.stream()
                        .map(Projet::getId)
                        .toList();

        Map<Integer, Long> compteurs = new HashMap<>();

        for (Object[] ligne : projetLikeRepository.compterParProjets(ids)) {
            compteurs.put(
                    ((Number) ligne[0]).intValue(),
                    ((Number) ligne[1]).longValue()
            );
        }

        return compteurs;
    }

    /**
     * Retourne le nom du porteur, ou Anonyme si aucun porteur n'est associé.
     */
    private String nomPorteur(Projet projet) {
        return projet.getPorteur() != null
                ? projet.getPorteur().getNomComplet()
                : "Anonyme";
    }

    /**
     * Convertit un projet en DTO et ajoute son nombre de likes.
     */
    private ProjetResponseDto versDto(
            Projet projet,
            String nomPorteur,
            Map<Integer, Long> likes) {

        ProjetResponseDto dto = new ProjetResponseDto(
                projet.getId(),
                projet.getTitre(),
                projet.getDescription(),
                projet.getPith_url(),
                projet.getQuota_propose(),
                projet.getSecteurActivite() != null
                        ? projet.getSecteurActivite().getNom()
                        : "Général",
                nomPorteur,
                projet.getDate_creation()
        );

        dto.setNombreLikes(
                likes.getOrDefault(projet.getId(), 0L)
        );

        return dto;
    }

    /**
     * Convertit un seul projet en DTO avec son nombre de likes.
     */
    private ProjetResponseDto versDtoAvecLikes(Projet projet) {

        Map<Integer, Long> likes = compterLikes(List.of(projet));

        return versDto(projet, nomPorteur(projet), likes);
    }

    /**
     * Programme la suppression des fichiers après la fin de la transaction.
     *
     * Si la transaction réussit, l'ancien pitch est supprimé.
     * Si elle échoue, le nouveau pitch est supprimé.
     */
    private void programmerNettoyageApresTransaction(
            String nouveauNom,
            String ancienNom) {

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            // Éviter de laisser un nouveau fichier sans gestion de nettoyage.
            if (nouveauNom != null && !nouveauNom.isBlank()) {
                stockageFichierService.supprimerPitch(nouveauNom);
            }

            throw new IllegalStateException(
                    "Aucune synchronisation transactionnelle active."
            );
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCompletion(int statut) {

                        try {
                            if (statut == STATUS_COMMITTED) {

                                // La transaction est validée :
                                // on supprime l'ancien fichier remplacé.
                                if (ancienNom != null
                                        && !ancienNom.isBlank()
                                        && !ancienNom.equals(nouveauNom)) {

                                    stockageFichierService.supprimerPitch(
                                            ancienNom
                                    );
                                }

                            } else {

                                // La transaction a échoué :
                                // on supprime le nouveau fichier.
                                if (nouveauNom != null
                                        && !nouveauNom.isBlank()) {

                                    stockageFichierService.supprimerPitch(
                                            nouveauNom
                                    );
                                }
                            }

                        } catch (RuntimeException e) {
                            logger.error(
                                    "Erreur lors du nettoyage des fichiers du projet.",
                                    e
                            );
                        }
                    }
                }
        );
    }
}
