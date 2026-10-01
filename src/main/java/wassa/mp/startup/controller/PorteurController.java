package wassa.mp.startup.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.dto.PorteurRequestDto;
import wassa.mp.startup.dto.PorteurResponseDto;
import wassa.mp.startup.dto.ProjetRequestDto;
import wassa.mp.startup.dto.ProjetResponseDto;
import wassa.mp.startup.service.PorteurService;
import wassa.mp.startup.service.ProjetService;

@RestController
@RequestMapping("/api/porteur")
public class PorteurController {
    @Autowired
    private PorteurService  porteurService;
    @Autowired
    private ProjetService projetService;
    @PostMapping("/register")
    public PorteurResponseDto createPorteur(@RequestBody PorteurRequestDto porteurRequestDto) {
        System.out.println(porteurRequestDto.getEmail());
         return porteurService.registerPorteur(porteurRequestDto);
    }
    @PostMapping("/projet")
    public ResponseEntity<String> CreerProjet(@RequestBody ProjetRequestDto p,@AuthenticationPrincipal CustumUserDetail userConnecter){
        projetService.creaProjet(p,userConnecter);
        return ResponseEntity.status(HttpStatus.CREATED).body("preojet cree avec succes!");


    }
    @GetMapping("/projets/{id}")
    public ProjetResponseDto getProjetByid(@PathVariable int id, @AuthenticationPrincipal CustumUserDetail userConnecter){

        return projetService.getProjetByid(id,userConnecter);


    }
}
