package wassa.mp.startup.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.dto.*;
import wassa.mp.startup.service.*;

import java.util.List;

@RestController
@RequestMapping("/api/porteur")
@SecurityRequirement(name = "bearerAuth")
public class PorteurController {
    @Autowired
    private PorteurService  porteurService;
    @Autowired
    private ProjetService projetService;
    @Autowired
    private ProjetEtapeService projetEtapeService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private ResponseService responseService;
    @PostMapping("/register")
    public PorteurResponseDto createPorteur(@RequestBody PorteurRequestDto porteurRequestDto) {
        System.out.println(porteurRequestDto.getEmail());
         return porteurService.registerPorteur(porteurRequestDto);
    }
    @PostMapping("/projet")
    public ResponseEntity<String> CreerProjet(@RequestBody ProjetRequestDto p,@AuthenticationPrincipal CustumUserDetail userConnecter){
        System.out.println("moussa");
        System.out.println(userConnecter.getUser().getEmail());
        projetService.creaProjet(p,userConnecter);
        return ResponseEntity.status(HttpStatus.CREATED).body("preojet cree avec succes!");
    }
    @GetMapping("/projets/{id}")
    public ProjetResponseDto getProjetByid(@PathVariable int id, @AuthenticationPrincipal CustumUserDetail userConnecter){
        return projetService.getProjetByid(id,userConnecter);
    }
    @GetMapping("/projets")
    public List<ProjetResponseDto> getProjetsByPorteurId(@AuthenticationPrincipal CustumUserDetail userConnecter){
        return projetService.getAllProjets(userConnecter);
    }
    @PutMapping("/projets/{id}")
    public ProjetResponseDto modifierProjet(@PathVariable int id, @RequestBody ProjetRequestDto projetRequestDto, @AuthenticationPrincipal CustumUserDetail userConnecter){
        return projetService.modifierProjet(id,userConnecter,projetRequestDto);
    }
    @GetMapping("/projets/{id}/etapes")
    public List<ProjetEtapeResponseDto> getEtapesProjet(@PathVariable int id, @AuthenticationPrincipal CustumUserDetail userConnecter){
        return  projetEtapeService.projetEtapeResponseDtoList(id,userConnecter);
    }
    @PostMapping("/projets/etapes")
    public ResponseEntity<String>CommencerEtapes(@AuthenticationPrincipal CustumUserDetail userConnecter,@RequestBody ProjetEtapesRequestDto projetEtapesRequestDto){
        projetEtapeService.commencerEtapes(projetEtapesRequestDto,userConnecter);
        return ResponseEntity.status(HttpStatus.CREATED).body("etape commencée  avec succes!");
    }
    @GetMapping("/etapes/{id}/questions")
    public List<QuestionResponseDto> getQuestionByEtapeId(@PathVariable int id){
        return questionService.getQuestionByEtape(id);

    }
    @GetMapping("/projet/projetEtape/{projet_etape_id}")
    public List<ReponseResponseDto> getReponseByEtapePorjet_id(@PathVariable int projet_etape_id){
       return  responseService.getReponseByEtapePorjet_id(projet_etape_id);
    }
}
