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
import wassa.mp.startup.repository.CandidatureRepository;
import wassa.mp.startup.repository.MentorProjetRepository;
import wassa.mp.startup.repository.ProjetRepository;
import wassa.mp.startup.repository.SecteurActiviteRepository;

import java.util.ArrayList;
import java.util.List;

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
    public List<ProjetResponseDto> getAllProjets(@AuthenticationPrincipal CustumUserDetail userConnecter) {
          return projetRepository.findByPorteur(userConnecter.getUser().getId())
                  .stream().map(
                          projet -> new ProjetResponseDto(projet.getId(), projet.getTitre(), projet.getDescription(), projet.getPith_url(), projet.getQuota_propose(), projet.getSecteurActivite().getNom(),userConnecter.getUser().getNomComplet(),projet.getDate_creation())
                  ).toList();
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

    public ProjetResponseDto getProjetByid(int id,@AuthenticationPrincipal CustumUserDetail userConnecter){
        Projet projet=projetRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Projet n'existe pas")
        );

        if(projet.getPorteur().getId()!=userConnecter.getUser().getId()){
            throw new NonAutoriseException("Vous n'êtes pas autorisé à modifier ce projet");
        }
        return new ProjetResponseDto(projet.getId(),projet.getTitre(), projet.getDescription(), projet.getPith_url(), projet.getQuota_propose(),projet.getSecteurActivite().getNom(),userConnecter.getUser().getNomComplet(),projet.getDate_creation());
    }
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
                        "COLLABORATEUR",
                        cand.getOffres().getTitre()
                ));
            }
        }

        return equipe;
    }



}
