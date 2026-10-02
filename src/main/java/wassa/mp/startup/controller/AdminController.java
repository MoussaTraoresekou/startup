package wassa.mp.startup.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.Id;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wassa.mp.startup.dto.QuestionRequestDto;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.Etape;
import wassa.mp.startup.model.Question;
import wassa.mp.startup.repository.EtapeRepository;
import wassa.mp.startup.repository.QuestionRepository;

@RestController
@RequestMapping("/api/admin")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
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
}
