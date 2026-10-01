package wassa.mp.startup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.dto.ProjetRequestDto;
import wassa.mp.startup.dto.ProjetResponseDto;
import wassa.mp.startup.exception.NonAutoriseException;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.Porteur;
import wassa.mp.startup.model.Projet;
import wassa.mp.startup.repository.ProjetRepository;

@Service
public class ProjetService {
    @Autowired
    private ProjetRepository projetRepository;
    public Projet creaProjet(ProjetRequestDto projetRequestDto,  @AuthenticationPrincipal CustumUserDetail userConnecter){
         Projet projet=new Projet();
         projet.setDescription(projet.getDescription());
         projet.setPith_url(projetRequestDto.getPith_url());
         projet.setTitre(projetRequestDto.getTitre());
         projet.setQuota_propose(projetRequestDto.getQuota_propose());
         projet.setPorteur((Porteur) userConnecter.getUser());
         return projetRepository.save(projet);
    }
    public ProjetResponseDto getProjetByid(int id,@AuthenticationPrincipal CustumUserDetail userConnecter){
        Projet projet=projetRepository.getById(id);
        if(projet==null){
             throw new ResourceNotFoundException("ce projet n'existe pas");
        }
        if(projet.getPorteur().getRole()!=userConnecter.getUser().getRole()){
            throw new NonAutoriseException("Vous n'êtes pas autorisé à modifier ce projet");
        }
        return new ProjetResponseDto(projet.getId(),projet.getTitre(), projet.getDescription(), projet.getPith_url(), projet.getQuota_propose());
    }
}
