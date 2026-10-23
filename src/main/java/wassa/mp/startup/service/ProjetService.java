package wassa.mp.startup.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.Enumeration.EnumCandidature;
import wassa.mp.startup.Enumeration.StatutDemandeMentoratEnum;
import wassa.mp.startup.dto.MembreEquipeDto;
import wassa.mp.startup.dto.ProjetRequestDto;
import wassa.mp.startup.dto.ProjetResponseDto;
import wassa.mp.startup.exception.NonAutoriseException;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.Candidature;
import wassa.mp.startup.model.MentorProjet;
import wassa.mp.startup.model.Porteur;
import wassa.mp.startup.model.Projet;
import wassa.mp.startup.repository.*;

import java.util.*;

@Service
public class ProjetService {
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
    /*
    public List<ProjetResponseDto> getAllProjets(@AuthenticationPrincipal CustumUserDetail userConnecter) {
          return projetRepository.findByPorteur(userConnecter.getUser().getId())
                  .stream().map(
                          projet -> new ProjetResponseDto(projet.getId(), projet.getTitre(), projet.getDescription(), projet.getPith_url(), projet.getQuota_propose(), projet.getSecteurActivite().getNom(),userConnecter.getUser().getNomComplet(),projet.getDate_creation())
                  ).toList();
    }

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

    // Tous les projets, pour un investisseur qui n'a pas choisi de secteurs.
    @Transactional
    public List<ProjetResponseDto> obtenirTousLesProjets() {
        List<Projet> projets = projetRepository.findAll();
        Map<Integer, Long> likes = compterLikes(projets);

        return projets.stream()
                .map(projet -> versDto(projet, nomPorteur(projet), likes))
                .toList();
    }
    @Transactional
    public Projet creaProjet(ProjetRequestDto projetRequestDto, @AuthenticationPrincipal CustumUserDetail userConnecter) {
        Porteur porteurActuel = (Porteur) userConnecter.getUser();

        long nombreProjetsExistants = projetRepository.countByPorteurId(porteurActuel.getId());
        if (nombreProjetsExistants >= 2) {
            throw new IllegalArgumentException("Limite atteinte : Vous ne pouvez pas créer plus de 2 projets dans l'incubateur.");
        }

        Projet projet = new Projet();
        projet.setDescription(projetRequestDto.getDescription());
        projet.setPith_url(projetRequestDto.getPith_url());
        projet.setTitre(projetRequestDto.getTitre());
        projet.setQuota_propose(projetRequestDto.getQuota_propose());
        projet.setPorteur(porteurActuel);
        projet.setSecteurActivite(secteurActiviteRepository.findById(projetRequestDto.getSecteur_id())
                .orElseThrow(() -> new ResourceNotFoundException("Secteur d'activité introuvable")));

        return projetRepository.save(projet);
    }


    @Transactional
    public ProjetResponseDto getProjetByid(
            int id,
            @AuthenticationPrincipal CustumUserDetail userConnecter) {

        Projet projet = projetRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Projet n'existe pas")
                );

        if (projet.getPorteur().getId() != userConnecter.getUser().getId()) {
            throw new NonAutoriseException(
                    "Vous n'êtes pas autorisé à consulter ce projet"
            );
        }

        return versDtoAvecLikes(projet);
    }
    /*

    public ProjetResponseDto modifierProjet(int id,@AuthenticationPrincipal CustumUserDetail userConnecter,ProjetRequestDto projetRequestDto){
        Projet projet=projetRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Projet n'existe pas")
        );
        if(projet.getPorteur().getId()!=userConnecter.getUser().getId()){
            throw new NonAutoriseException("pas le droit");
        }
        projet.setPith_url(projetRequestDto.getPith_url());
        projet.setQuota_propose(projetRequestDto.getQuota_propose());
        projet.setDescription(projetRequestDto.getDescription());
        projet.setTitre(projetRequestDto.getTitre());
        projet.setQuota_propose(projetRequestDto.getQuota_propose());
        projet.setSecteurActivite(secteurActiviteRepository.findById(projetRequestDto.getSecteur_id()).get());
        Projet projetModifier=projetRepository.save(projet);
        return new ProjetResponseDto(projetModifier.getId(),projetModifier.getTitre(), projetModifier.getDescription(), projetModifier.getPith_url(), projetModifier.getQuota_propose(),projetModifier.getSecteurActivite().getNom(),userConnecter.getUser().getNomComplet(),projet.getDate_creation());
    }

     */

    @Transactional
    public ProjetResponseDto modifierProjet(
            int id,
            @AuthenticationPrincipal CustumUserDetail userConnecter,
            ProjetRequestDto projetRequestDto) {

        Projet projet = projetRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Projet n'existe pas")
                );

        if (projet.getPorteur().getId() != userConnecter.getUser().getId()) {
            throw new NonAutoriseException("Vous n'êtes pas autorisé à modifier ce projet");
        }

        projet.setPith_url(projetRequestDto.getPith_url());
        projet.setQuota_propose(projetRequestDto.getQuota_propose());
        projet.setDescription(projetRequestDto.getDescription());
        projet.setTitre(projetRequestDto.getTitre());

        projet.setSecteurActivite(
                secteurActiviteRepository.findById(projetRequestDto.getSecteur_id())
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Secteur d'activité introuvable"
                                )
                        )
        );

        Projet projetModifier = projetRepository.save(projet);

        return versDtoAvecLikes(projetModifier);
    }

    /*
    //cette methode retourne les membres d'un projet
    public List<MembreEquipeDto> calculerEquipeProjetComplete(int projetId) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable"));

        List<MembreEquipeDto> equipe = new ArrayList<>();

        // 1. Le Fondateur d'Origine (Le Porteur)
        equipe.add(new MembreEquipeDto(
                projet.getPorteur().getId(),
                projet.getPorteur().getNom(),
                projet.getPorteur().getPrenom(),
                "PORTEUR",
                "Fondateur Principal"
        ));

        // 2. Les Mentors (Gestion multi-mentors avec boucle sur la liste)
        List<Object> liaisonsMentors = mentorProjetRepository.findAllByProjetIdAndStatut(projetId, StatutDemandeMentoratEnum.ACCEPTEE);
        for (Object obj : liaisonsMentors) {
            MentorProjet liaison = (MentorProjet) obj;
            equipe.add(new MembreEquipeDto(
                    liaison.getMentor().getId(),
                    liaison.getMentor().getNom(),
                    liaison.getMentor().getPrenom(),
                    "MENTOR",
                    "Mentor Accompagnateur"
            ));
        }



        // 3. Les Collaborateurs issus des Appels (Candidatures ACCEPTEES)
        List<Candidature> postulationsValides = candidatureRepository.findByOffresProjetIdAndStatuCandidature(projetId, EnumCandidature.ACCEPTEE);
        for (Candidature cand : postulationsValides) {
            equipe.add(new MembreEquipeDto(
                    cand.getPorteur().getId(),
                    cand.getPorteur().getNom(),
                    cand.getPorteur().getPrenom(),
                    "COLLABORATEUR",
                    cand.getPorteur().getSpecialite().getLibelle()
            ));
        }
        return equipe;
    }

     */
    public List<MembreEquipeDto> calculerEquipeProjetComplete(int projetId) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable"));

        List<MembreEquipeDto> equipe = new ArrayList<>();

        equipe.add(new MembreEquipeDto(
                projet.getPorteur().getId(),
                projet.getPorteur().getNom(),
                projet.getPorteur().getPrenom(),
                "PORTEUR",
                "Fondateur Principal"
        ));

        List<Object> liaisonsMentors = mentorProjetRepository.findAllByProjetIdAndStatut(projetId, StatutDemandeMentoratEnum.ACCEPTEE);
        for (Object obj : liaisonsMentors) {
            MentorProjet liaison = (MentorProjet) obj;
            equipe.add(new MembreEquipeDto(
                    liaison.getMentor().getId(),
                    liaison.getMentor().getNom(),
                    liaison.getMentor().getPrenom(),
                    "MENTOR",
                    "Mentor Accompagnateur"
            ));
        }

        List<Candidature> candidaturesAcceptees = candidatureRepository.findByOffresProjetIdAndStatuCandidature(projetId, EnumCandidature.ACCEPTEE);
        for (Candidature cand : candidaturesAcceptees) {
            if (cand.getPorteur() != null) {
                equipe.add(new MembreEquipeDto(
                        cand.getPorteur().getId(),
                        cand.getPorteur().getNom(),
                        cand.getPorteur().getPrenom(),
                        cand.getPorteur().getSpecialite().getLibelle(),
                        cand.getOffres().getTitre()
                ));
            }
        }

        return equipe;
    }

/*
    @Transactional
    public List<ProjetResponseDto> obtenirProjetsParSecteurs(List<Integer> secteurIds) {
        if (secteurIds == null || secteurIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // 1. Récupération des projets liés à ces secteurs d'activité depuis votre repository
        List<Projet> projets = projetRepository.findBySecteurActiviteIdIn(secteurIds);

        // 2. Conversion (Mapping) des entités Projet vers la liste de ProjetResponseDto
        return projets.stream()
                .map(projet -> new ProjetResponseDto(
                        projet.getId(),
                        projet.getTitre(),
                        projet.getDescription(),
                        projet.getPith_url(), // ou projet.getPithUrl() selon vos attributs d'entité
                        projet.getQuota_propose(),
                        projet.getSecteurActivite() != null ? projet.getSecteurActivite().getNom() : "Général",
                        projet.getPorteur() != null ? projet.getPorteur().getNomComplet() : "Anonyme",
                        projet.getDate_creation() // Mappe votre LocalDate date_creation
                ))
                .toList();
    }

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

    // Compte les likes de plusieurs projets en une seule requête
    // (au lieu d'une requête par projet).
    private Map<Integer, Long> compterLikes(List<Projet> projets) {
        if (projets.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Integer> ids = projets.stream().map(Projet::getId).toList();
        Map<Integer, Long> compteurs = new HashMap<>();

        for (Object[] ligne : projetLikeRepository.compterParProjets(ids)) {
            compteurs.put(((Number) ligne[0]).intValue(), ((Number) ligne[1]).longValue());
        }

        return compteurs;
    }

    private String nomPorteur(Projet projet) {
        return projet.getPorteur() != null ? projet.getPorteur().getNomComplet() : "Anonyme";
    }

    private ProjetResponseDto versDto(Projet projet, String nomPorteur, Map<Integer, Long> likes) {
        ProjetResponseDto dto = new ProjetResponseDto(
                projet.getId(),
                projet.getTitre(),
                projet.getDescription(),
                projet.getPith_url(),
                projet.getQuota_propose(),
                projet.getSecteurActivite() != null ? projet.getSecteurActivite().getNom() : "Général",
                nomPorteur,
                projet.getDate_creation()
        );
        dto.setNombreLikes(likes.getOrDefault(projet.getId(), 0L));
        return dto;
    }

    private ProjetResponseDto versDtoAvecLikes(Projet projet) {
        Map<Integer, Long> likes = compterLikes(List.of(projet));
        return versDto(projet, nomPorteur(projet), likes);
    }


}
