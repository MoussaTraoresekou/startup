package wassa.mp.startup.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wassa.mp.startup.dto.SecteurActiviteResponseDto;

import wassa.mp.startup.service.SecteurActiviteService;

import java.util.List;

@RestController
@RequestMapping("/api/secteur")
@SecurityRequirement(name = "bearerAuth")
public class SecteurActiviteController {
    @Autowired
    private SecteurActiviteService secteurActiviteService;
    @GetMapping
    public List<SecteurActiviteResponseDto> getSecteurActivites() {
      return secteurActiviteService.getSecteurActivites();
    }
}
