package wassa.mp.startup.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.Enumeration.EnumCandidature;
import wassa.mp.startup.dto.MembreEquipeDto;
import wassa.mp.startup.dto.ProjetRequestDto;
import wassa.mp.startup.dto.ProjetResponseDto;
import wassa.mp.startup.exception.NonAutoriseException;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.Candidature;
import wassa.mp.startup.model.Porteur;
import wassa.mp.startup.model.Projet;
import wassa.mp.startup.repository.CandidatureRepository;
import wassa.mp.startup.repository.MentorProjetRepository;
import wassa.mp.startup.repository.ProjetRepository;
import wassa.mp.startup.repository.SecteurActiviteRepository;

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
    public Projet creaProjet(ProjetRequestDto projetRequestDto,  @AuthenticationPrincipal CustumUserDetail userConnecter){
         Projet projet=new Projet();
         projet.setDescription(projetRequestDto.getDescription());
         projet.setPith_url(projetRequestDto.getPith_url());
         projet.setTitre(projetRequestDto.getTitre());
         projet.setQuota_propose(projetRequestDto.getQuota_propose());
         projet.setPorteur((Porteur) userConnecter.getUser());
         projet.setSecteurActivite(secteurActiviteRepository.findById(projetRequestDto.getSecteur_id()).get());
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
    public List<MembreEquipeDto> calculerEquipeProjetComplete(int projetId) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new wassa.mp.startup.exception.ResourceNotFoundException("Projet introuvable"));

        List<MembreEquipeDto> equipe = new java.util.ArrayList<>();

        // 1. Le Fondateur d'Origine (Le Porteur)
        equipe.add(new MembreEquipeDto(
                projet.getPorteur().getId(),
                projet.getPorteur().getNom(),
                projet.getPorteur().getPrenom(),
                "PORTEUR",
                "Fondateur Principal"
        ));

        // 2. Les Mentors (Gestion multi-mentors avec boucle sur la liste)
        List<Object> liaisonsMentors = mentorProjetRepository.findAllByProjetIdAndStatut(projetId, wassa.mp.startup.Enumeration.StatutDemandeMentoratEnum.ACCEPTEE);
        for (Object obj : liaisonsMentors) {
            wassa.mp.startup.model.MentorProjet liaison = (wassa.mp.startup.model.MentorProjet) obj;
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
                    cand.getCollaborateur().getId(),
                    cand.getCollaborateur().getNom(),
                    cand.getCollaborateur().getPrenom(),
                    "COLLABORATEUR",
                    cand.getCollaborateur().getSpecialite()
            ));
        }

        return equipe;
    }


}
