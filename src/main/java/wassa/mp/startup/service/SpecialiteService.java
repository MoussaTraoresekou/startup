package wassa.mp.startup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wassa.mp.startup.dto.SecteurActiviteResponseDto;
import wassa.mp.startup.dto.SpecialiteResponseDto;
import wassa.mp.startup.repository.SecteurActiviteRepository;
import wassa.mp.startup.repository.SpecialiteRepository;

import java.util.List;

@Service
public class SpecialiteService {
    @Autowired
    private SpecialiteRepository specialiteRepository;
    public List<SpecialiteResponseDto> getAllSpecialite() {
        return specialiteRepository.findAll().stream().map(
                specialite -> new SpecialiteResponseDto(specialite.getId(), specialite.getLibelle())
        ).toList();
    }

}
