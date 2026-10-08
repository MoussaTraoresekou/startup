package wassa.mp.startup.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wassa.mp.startup.dto.EtapeResponseDto;
import wassa.mp.startup.service.EtapeService;

import java.util.List;

@RestController
@RequestMapping("/api/etape")
@SecurityRequirement(name = "bearerAuth")
public class EtapeController {
    @Autowired
    private EtapeService etapeService;
    @GetMapping
    public List<EtapeResponseDto> findAllEtape() {
         return etapeService.getAllEtape();

    }
}
