package wassa.mp.startup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.Enumeration.StatutEtapeEnum;
import wassa.mp.startup.dto.ProjetEtapeResponseDto;
import wassa.mp.startup.dto.ProjetEtapesRequestDto;
import wassa.mp.startup.exception.NonAutoriseException;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.Projet;
import wassa.mp.startup.model.ProjetEtape;
import wassa.mp.startup.repository.EtapeRepository;
import wassa.mp.startup.repository.ProjetEtapeRepository;
import wassa.mp.startup.repository.ProjetRepository;

import java.util.List;

@Service
public class ProjetEtapeService {
    @Autowired
    private ProjetEtapeRepository projetEtapeRepository;
    @Autowired
    private ProjetRepository projetRepository;
    @Autowired
    private EtapeRepository etapeRepository;
    public List<ProjetEtapeResponseDto> projetEtapeResponseDtoList(int projetId,@AuthenticationPrincipal CustumUserDetail userConnecter) {
        Projet projet=projetRepository.findById(projetId).orElseThrow(
                ()-> new ResourceNotFoundException("Projet n'existe pas")
        );
        if(projet.getPorteur().getId()!=userConnecter.getUser().getId()){
            throw new NonAutoriseException("non autorisé");
        }
        return projetEtapeRepository.findByProjetId(projetId).stream().map(
                 projetEtape -> new ProjetEtapeResponseDto(
                         projetEtape.getId(),projetEtape.getStatutEtape(),projetEtape.getDateSoumission(),
                         projetEtape.getDateValidation(),projetEtape.getCommentaireMentor(),projetEtape.getEtape().getType().toString(),null
                 )
        ).toList();
    }
    public ProjetEtape commencerEtapes(ProjetEtapesRequestDto projetEtapesRequestDto,@AuthenticationPrincipal CustumUserDetail userConnecter) {
        Projet projet=projetRepository.findById(projetEtapesRequestDto.getProjetId()).orElseThrow(
                ()-> new ResourceNotFoundException("Projet n'existe pas")
        );
        if(projet.getPorteur().getId()!=userConnecter.getUser().getId()){
            throw new NonAutoriseException("non autorisé");
        }
        ProjetEtape projetEtape=new ProjetEtape();
        projetEtape.setStatutEtape(StatutEtapeEnum.EN_COUR);
        projetEtape.setProjet(projet);
        projetEtape.setEtape(etapeRepository.findById(projetEtapesRequestDto.getEtape_id()).get());
        return projetEtapeRepository.save(projetEtape);

    }
}
