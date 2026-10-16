package wassa.mp.startup.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wassa.mp.startup.dto.SpecialiteResponseDto;
import wassa.mp.startup.service.SpecialiteService;

import java.util.List;
@RestController
@RequestMapping("/api/specialites")
@SecurityRequirement(name = "bearerAuth")

public class SpecialiteControlleur {
    @Autowired
    private SpecialiteService specialiteService;
    @GetMapping
    public List<SpecialiteResponseDto> getSecteurActivites() {
        return specialiteService.getAllSpecialite();
    }
}
