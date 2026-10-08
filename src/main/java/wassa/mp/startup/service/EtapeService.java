package wassa.mp.startup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wassa.mp.startup.dto.EtapeResponseDto;
import wassa.mp.startup.repository.EtapeRepository;

import java.util.List;

@Service
public class EtapeService {
    @Autowired
    private EtapeRepository etapeRepository;
    public List<EtapeResponseDto> getAllEtape() {
          return etapeRepository.findAll().stream().map(
                  etape -> new EtapeResponseDto(etape.getId(), etape.getDescription(),etape.getType().toString())
          ).toList();
    }
}
