package wassa.mp.startup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wassa.mp.startup.dto.ReponseResponseDto;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.ProjetEtape;
import wassa.mp.startup.repository.ProjetEtapeRepository;
import wassa.mp.startup.repository.ReponseRepository;

import java.util.List;

@Service
public class ResponseService {
    @Autowired
    private ReponseRepository responseService;
    @Autowired
    private ProjetEtapeRepository projetEtapeRepository;
    public List<ReponseResponseDto> getReponseByEtapePorjet_id(int etape_id) {
        ProjetEtape projetEtape=projetEtapeRepository.findById(etape_id).orElseThrow(
                ()->new ResourceNotFoundException("Etape"+etape_id+"not found")
        );
        return responseService.findByReponseByProjetEtape(projetEtape.getId()).stream().map(
                reponse -> new ReponseResponseDto(reponse.getId(),reponse.getProjetEtape().getProjet().getTitre(),reponse.getProjetEtape().getEtape().getType().toString(),reponse.getQuestion().getLibelle(),reponse.getReponse())
        ).toList();

    }
}
