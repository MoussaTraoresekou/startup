package wassa.mp.startup.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import wassa.mp.startup.dto.MentorEnAttenteResponseDto;
import wassa.mp.startup.dto.MentorReponseDto;
import wassa.mp.startup.dto.QuestionRequestDto;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.Question;
import wassa.mp.startup.repository.EtapeRepository;
import wassa.mp.startup.repository.QuestionRepository;
import wassa.mp.startup.service.MentorService;
import wassa.mp.startup.service.StockageFichierService;

import java.nio.file.Files;
import java.nio.file.Path;
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
    @Autowired
    private StockageFichierService stockageFichierService;
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
    public ResponseEntity<MentorReponseDto> validerMentor(@PathVariable int id) {

        return ResponseEntity.ok( mentorService.validerDossierMentor(id));
    }

    @PutMapping("/mentors/{id}/refuser")
    public ResponseEntity<MentorReponseDto> refuserMentor(@PathVariable int id) {
        return ResponseEntity.ok(mentorService.refuserDossierMentor(id));
    }

    @GetMapping("/mentors/en-attente")
    public ResponseEntity<List<MentorEnAttenteResponseDto>> getMentorsEnAttente() {

        List<MentorEnAttenteResponseDto> liste = mentorService.listerMentorsEnAttente();

        return ResponseEntity.ok(liste);
    }
    @GetMapping("/mentors")
    public List<MentorReponseDto> getAllMentors() {
        return mentorService.getAllMentors();
    }

    @GetMapping("/documents/{nomFichier:.+}")
    public ResponseEntity<Resource> consulterDocument(
            @PathVariable String nomFichier) {

        Path chemin = stockageFichierService.obtenirChemin(nomFichier);

        if (!Files.exists(chemin) || !Files.isRegularFile(chemin)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Document introuvable."
            );
        }

        Resource fichier = new FileSystemResource(chemin);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + nomFichier + "\""
                )
                .body(fichier);
    }
}
