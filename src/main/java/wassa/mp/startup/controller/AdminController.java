package wassa.mp.startup.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.Id;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wassa.mp.startup.dto.MentorEnAttenteResponseDto;
import wassa.mp.startup.dto.QuestionRequestDto;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.Etape;
import wassa.mp.startup.model.Question;
import wassa.mp.startup.repository.EtapeRepository;
import wassa.mp.startup.repository.QuestionRepository;
import wassa.mp.startup.service.MentorService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    @Autowired
    private MentorService mentorService;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private EtapeRepository etapeRepository;
    @PostMapping("/question")
    public ResponseEntity<String> ajouter(@RequestBody QuestionRequestDto questionRequestDto) {
        Question question = new Question();
        question.setLibelle(questionRequestDto.getLibelle());
        question.setEtape(etapeRepository.findById(questionRequestDto.getEtapeId()).orElseThrow(
                ()->new ResourceNotFoundException("cette etape n'existe pas")
        ));
        questionRepository.save(question);
        return ResponseEntity.status(HttpStatus.CREATED).body("question inseree avec succes");
    }
    @PutMapping("/mentors/{id}/valider")
    public ResponseEntity<String> validerMentor(@PathVariable int id) {

        // Appel de la logique métier de validation
        mentorService.validerDossierMentor(id);

        return ResponseEntity.ok("Le dossier du mentor a été validé avec succès. Son compte est désormais actif.");
    }
    /**
     * Endpoint permettant à l'administrateur de refuser un mentor.
     * URL : PUT http://localhost:8080/api/admin/mentors/{id}/refuser
     */
    @PutMapping("/mentors/{id}/refuser")
    public ResponseEntity<String> refuserMentor(@PathVariable int id) {

        // Appel de la logique métier de refus
        mentorService.refuserDossierMentor(id);

        return ResponseEntity.ok("Le dossier du mentor a été refusé. Son accès à la plateforme est désormais bloqué.");
    }
    /**
     * Endpoint permettant à l'administrateur de l'incubateur de lister tous les dossiers mentors en attente.
     * URL : GET http://localhost:8080/api/admin/mentors/en-attente
     */
    @GetMapping("/mentors/en-attente")
    public ResponseEntity<List<MentorEnAttenteResponseDto>> getMentorsEnAttente() {

        List<MentorEnAttenteResponseDto> liste = mentorService.listerMentorsEnAttente();

        return ResponseEntity.ok(liste);
    }


}
