package wassa.mp.startup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wassa.mp.startup.dto.ReponseResponseDto;
import wassa.mp.startup.dto.SecteurActiviteResponseDto;
import wassa.mp.startup.repository.SecteurActiviteRepository;

import java.util.List;

@Service
public class SecteurActiviteService {
    @Autowired
    private SecteurActiviteRepository secteurActiviteRepository;
    public List<SecteurActiviteResponseDto> getSecteurActivites() {
        return secteurActiviteRepository.findAll().stream().map(
                secteurActivite -> new SecteurActiviteResponseDto(secteurActivite.getId(), secteurActivite.getNom())
        ).toList();
    }
}
