package wassa.mp.startup.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wassa.mp.startup.dto.CollaborateurRequestDto;
import wassa.mp.startup.dto.CollaborateurResponseDto;
import wassa.mp.startup.service.CollaborateurService;

@RestController
@RequestMapping("/api/collaborateur")
@SecurityRequirement(name = "bearerAuth")
public class CollaborateurController {

    @Autowired
    private CollaborateurService collaborateurService;

    @PostMapping("/register")
    public ResponseEntity<CollaborateurResponseDto> inscrireNouveauCollaborateur(@RequestBody CollaborateurRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(collaborateurService.inscrireCollaborateur(dto));
    }
}
